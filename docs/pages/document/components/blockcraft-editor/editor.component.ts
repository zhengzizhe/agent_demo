import { Component, EventEmitter, Inject, Injector, Input, Output, ViewChild, ViewContainerRef } from '@angular/core';
import {
    AttachmentExtensionPlugin,
    BLOCK_CREATOR_SERVICE_TOKEN,
    BlockControllerPlugin,
    BlockCraftDoc,
    BlockNodeType,
    BlockTransformerPlugin,
    BookmarkBlockExtensionPlugin,
    CalloutToolbarPlugin,
    ClipboardDataType,
    CodeInlineEditorBinding,
    ConsoleLogger,
    DividerExtensionPlugin,
    DOC_ADAPTER_SERVICE_TOKEN,
    DOC_FILE_SERVICE_TOKEN,
    DOC_LINK_PREVIEWER_SERVICE_TOKEN,
    DOC_MESSAGE_SERVICE_TOKEN,
    DocExportManager,
    DocLinkPreviewerService,
    EmbedFrameExtensionPlugin,
    FloatTextToolbarPlugin,
    IBlockSnapshot,
    ImgToolbarPlugin,
    InlineLinkExtension,
    OrderedBlockPlugin,
    ORIGIN_SKIP_SYNC,
    TableBlockBinding,
    TextMarkerPlugin
} from '@ccc/blockcraft';
import { CsesDocFileService } from './services/doc-file.service';
import { CsesDocMessageService } from './services/doc-message.service';
import { CsesBlockCreatorService } from './services/block-creator.service';
import { CasesDocLinkPreviewerService } from './services/link-previewer.service';
import { AdapterService } from './services/adapter.service';
import { MENTION_EMBED_CONVERTER, OLD_LINK_EMBED_CONVERTER, SchemaStore, TASK_EMBED_CONVERTER } from './const';
import { MentionPlugin } from './plugins';
import { IndexeddbPersistence } from 'y-indexeddb';
import { CsesContextService, CsesRenderService } from '@ccc/cses-common';
import { BlockCraftAwareness } from './awareness';
import * as Y from 'yjs';
import { WebsocketProvider } from 'y-websocket';
import { SyncStatus, SyncStatusPipe } from './syncStatus.pipe';
import { NzBackTopComponent } from 'ng-zorro-antd/back-top';
import { DOC_LINK_HOST, DocService, IDlUser, IDocDetail, RouterService } from '@/pages/docs/services';
import { DlSpinComponent } from '@/pages/docs/components';
import { NgIf } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AppContext } from '@ccc/core-common';
import { Subject } from 'rxjs';

let WS_URI = 'ws://ws-doc-pre.cses7.com';
// let WS_URI = 'ws://192.168.6.199:3235/collaborate';
// const WS_URI = 'ws://ws-doc.cses7.com';
// const WS_URI = 'ws://196.168.1.81:1234/collaborate';

@Component({
    selector: 'blockcraft-editor',
    template: `
        <dl-spin *ngIf="syncStatusCode === SyncStatus.connecting"></dl-spin>

        <div>
            <div style="height: 30px; cursor: text;" (mousedown)="onContainerMousedown($event, true)"></div>
            <ng-container #container></ng-container>
            <div style="height: 60px; cursor: text;" (mousedown)="onContainerMousedown($event, false)"></div>
        </div>
    `,
    styles: [`
        :host {
            display: block;
        }
    `],
    providers: [
        { provide: DOC_FILE_SERVICE_TOKEN, useClass: CsesDocFileService },
        { provide: DOC_MESSAGE_SERVICE_TOKEN, useClass: CsesDocMessageService },
        { provide: BLOCK_CREATOR_SERVICE_TOKEN, useClass: CsesBlockCreatorService },
        { provide: DOC_ADAPTER_SERVICE_TOKEN, useClass: AdapterService },
        { provide: DOC_LINK_PREVIEWER_SERVICE_TOKEN, useClass: CasesDocLinkPreviewerService }
    ],
    imports: [
        SyncStatusPipe,
        NzBackTopComponent,
        DlSpinComponent,
        NgIf
    ],
    standalone: true
})
export class BlockCraftEditor {
    @Input({ required: true })
    docDetail!: IDocDetail;

    @Input()
    isEditAllow = false;

    @Output()
    inlineUserChange = new EventEmitter<IDlUser[]>();

    @Output()
    syncStatusChange = new EventEmitter<SyncStatus>();

    @Output()
    afterInit = new EventEmitter<void>();

    @ViewChild('container', { static: true, read: ViewContainerRef }) container!: ViewContainerRef;

    private _syncStatusCode = SyncStatus.Syncing;
    set syncStatusCode(code: SyncStatus) {
        this._syncStatusCode = code;
        this.syncStatusChange.emit(code);
    }

    get syncStatusCode() {
        return this._syncStatusCode;
    }

    doc!: BlockCraft.Doc;
    provider: WebsocketProvider;
    persistence: IndexeddbPersistence;
    protected _editorAwareness: BlockCraftAwareness;

    constructor(
        @Inject(AppContext) public ctx: CsesContextService,
        private readonly router: RouterService,
        private activeRoute: ActivatedRoute,
        public readonly render: CsesRenderService,
        private docService: DocService,
        private injector: Injector
    ) {
        WS_URI = this.ctx.env['yrsSocket'] || WS_URI;
    }

    get docId() {
        return this.docDetail.id;
    }

    get indexeddbId() {
        return this.docId + '-v1';
    }

    ngOnInit() {
    }

    ngAfterViewInit() {
        this.createDoc();
    }

    ngOnDestroy() {
        console.log('---------------------doc destroy---------------------');
        if (this.doc.isInitialized) {
            try {
                if (!this.timer) clearTimeout(this.timer);
                this.syncDiffToRemote();
                this.storeJSON();
            } catch (e) {
                console.warn(e);
            }
        }

        this.doc?.destroy();
        this.doc?.yDoc?.destroy();
        this.provider && (this.provider.shouldConnect = false);
        this.provider?.disconnect();
        this.provider?.awareness.destroy();
        this.provider?.destroy();
        this.storeScrollTop();
    }

    onContainerMousedown(evt: MouseEvent, atStart = false) {
        if (!this.doc?.isInitialized || this.doc.isReadonly) return;
        if (evt.target === evt.currentTarget && evt.eventPhase === evt.AT_TARGET) {
            evt.preventDefault();
            evt.stopPropagation();

            if (atStart && this.doc.root.firstChildren?.flavour === 'paragraph') {
                this.doc.selection.setCursorAtBlock(this.doc.root.firstChildren, false);
                return;
            }
            if (!atStart && this.doc.root.lastChildren?.flavour === 'paragraph') {
                this.doc.selection.setCursorAtBlock(this.doc.root.lastChildren, false);
                return;
            }

            const p = this.doc.schemas.createSnapshot('paragraph', []);
            this.doc.crud.insertBlocks(this.doc.rootId, atStart ? 0 : this.doc.root.childrenLength, [p])
                .then(() => {
                    this.doc.selection.setCursorAtBlock(p.id, true);
                });
        }
    }

    async createDoc() {
        const yDoc = new Y.Doc({
            guid: this.docId,
            gc: false
        });

        this.creatBlockCraftDoc(yDoc);

        // const root = await this.docService.getBlockSnapshot(this.docId, this.docId);
        // if (!root) {
        //     await this.initDocAsEmpty();
        //     this.initDocExtra();
        //     this.syncStatusCode = SyncStatus.remoteSynced;
        //     return
        // } else {
        //     // const root1 = this.doc.schemas.createSnapshot(
        //     //     'root',
        //     //     [this.docId, [root]]
        //     // )
        //     await this.doc.initBySnapshot(root, this.container);
        //     this.initDocExtra();
        //     this.syncStatusCode = SyncStatus.remoteSynced;
        //     return
        // }

        const startTime = performance.now();
        await this.getLocal(yDoc);
        const localEndTime = performance.now();
        console.log('%cgetLocal ok-------------->', 'color: blue;', localEndTime - startTime);
        await this.enterRoom(yDoc);
        const remoteEndTime = performance.now();
        console.log('%cgetRemote ok---------------', 'color: blue;', remoteEndTime - localEndTime);

        this.syncStatusCode = SyncStatus.Syncing;

        this.creatBlockCraftDoc(yDoc);

        const initFnByY = async () => {
            const yRoot = this.doc.yBlockMap.get(this.docId);
            if (yRoot) {
                console.log('%cinit from ydoc ok----------', 'color: blue;', performance.now() - remoteEndTime);
                await this.doc.initByYBlock(yRoot, this.container);
                this.syncStatusCode = SyncStatus.remoteSynced;
                this.initDocExtra();
                this.initScrollTop();
            }
        };

        const yRoot = this.doc.yBlockMap.get(this.docId);

        // 第一步就拿到了全文数据
        if (this.doc.yBlockMap.size && yRoot) {
            await initFnByY();
            return;
        }

        const waitFn = async (v, _, doc, tr) => {
            if (v.byteLength < 10 || !this.doc.yBlockMap.size) return;
            yDoc.off('update', waitFn);
            if (this.doc.isInitialized || tr.local) return;
            await initFnByY();
        };
        yDoc.on('update', waitFn);

        // 检查JSON库里是否有文档
        const isExist = await this.docService.checkMigrateDataExist(this.docId);
        if (this.doc.isInitialized) return yDoc.off('update', waitFn);

        const initAsEmpty = async () => {
            if (this.doc.isInitialized) return;
            yDoc.off('update', waitFn);
            await this.initDocAsEmpty();
            await this.storeJSON();
            await this.docService.addSyncedFlag(this.docId, true);
            this.syncStatusCode = SyncStatus.remoteSynced;
            this.initDocExtra();
        };

        // 新文档
        if (!isExist.result && this.docDetail.createdAt > 1756966375714) {
            await initAsEmpty();
            return;
        }

        if (this.doc.isInitialized) return yDoc.off('update', waitFn);

        const initByJSON = async () => {
            if (this.doc.isInitialized) return;
            yDoc.off('update', waitFn);
            await this.initDocFromMigrateData();
            this.syncStatusCode = SyncStatus.remoteSynced;
            await this.docService.addSyncedFlag(this.docId, true);
            this.initDocExtra();
        };

        // 是否已同步到协同服务器
        this.docService.getSyncedFlag(this.docId).then(r => {
            if (r.result) {
            } else {
                // 协同服务器上没有数据，从json还原
                isExist.result ? initByJSON() : initAsEmpty();
            }
        }).catch(e => {
            isExist.result ? initByJSON() : initAsEmpty();
        });
    }

    creatBlockCraftDoc(yDoc: Y.Doc) {
        this.doc = new BlockCraftDoc({
            yDoc,
            docId: this.docId,
            schemas: SchemaStore,
            logger: new ConsoleLogger(),
            injector: this.injector,
            embeds: [['task', TASK_EMBED_CONVERTER], ['mention', MENTION_EMBED_CONVERTER], ['link', OLD_LINK_EMBED_CONVERTER]],
            plugins: [new OrderedBlockPlugin(), new TableBlockBinding(), new CodeInlineEditorBinding(), new TextMarkerPlugin(['mermaid-textarea']),
                new FloatTextToolbarPlugin(), new BlockTransformerPlugin(),
                new BlockControllerPlugin(
                    [
                        {
                            type: 'tool',
                            name: 'copyBlockLink',
                            value: true,
                            icon: 'bc_fuzhilianjie',
                            label: '复制段落链接'
                        }
                    ],
                    (item, block, doc) => {
                        switch (item.name) {
                            case 'copyBlockLink':
                                this.copyBlockLink(block);
                                return true;
                        }
                        return false;
                    }
                ),
                new ImgToolbarPlugin(), new CalloutToolbarPlugin(), new AttachmentExtensionPlugin(),
                new EmbedFrameExtensionPlugin(), new BookmarkBlockExtensionPlugin(),
                new InlineLinkExtension((uri) => {
                    if (uri.startsWith(DOC_LINK_HOST)) {
                        const _uri = uri.replace(DOC_LINK_HOST, '');
                        if (_uri.startsWith('document')) {
                            const [_docId, params] = _uri.split('/')[1].split('?');
                            if (_docId === this.docId) {
                                if (params && params.startsWith('blockId')) this.scrollToBlock(params.split('=')[1], true);
                                return;
                            }
                            this.router.navigateToNewTab(_uri.replace(DOC_LINK_HOST, ''));
                        }
                    } else {
                        window.open(uri, '_blank');
                    }
                }),
                new MentionPlugin((w, t) => this.docService.getMentionData(w, t)),
                new DividerExtensionPlugin()
            ],
            readonly: true
        });
        this.doc.event.bindHotkey({
            key: 's',
            shortKey: true
        }, () => {
            this.render.info('文档自动保存，不用手动保存哦');
        });
    }

    copyBlockLink(block: BlockCraft.BlockComponent) {
        const url = DOC_LINK_HOST + 'document/' + this.docId + '?blockId=' + block.id;
        this.doc.clipboard.copyText(url).then(() => {
            this.doc.messageService.success('已复制链接');
        });
    }

    // -------------------数据部分--------------
    getLocal(yDoc: Y.Doc) {
        this.syncStatusCode = SyncStatus.connecting;
        this.persistence = new IndexeddbPersistence(this.indexeddbId, yDoc);
        return new Promise((resolve, reject) => {
            this.persistence.once('synced', async () => {
                console.log('%c本地数据加载完成', 'background: blue; color: #fff;');
                this.syncStatusCode = SyncStatus.localSynced;

                // await this.initDocFromYBlock();
                resolve(true);
            });
        });
    }

    enterRoom(yDoc: Y.Doc) {
        this.provider = new WebsocketProvider(WS_URI, this.docId, yDoc, {
            maxBackoffTime: 10000
        });
        const statusHandler = (e: { status: string }) => {
            switch (e.status) {
                case 'connected':
                    this.syncStatusCode = this.doc.isInitialized ? SyncStatus.remoteSynced : SyncStatus.connected;
                    break;
                case 'disconnected':
                    this.syncStatusCode = this.doc.isInitialized ? SyncStatus.localStoring : SyncStatus.disconnected;
                    break;
            }
        };
        this.provider.on('status', statusHandler);
        this.provider.on('connection-error', (evt) => {
            this.syncStatusCode = this.doc.isInitialized ? SyncStatus.localStoring : SyncStatus.disconnected;
            console.log('连接错误', evt);
        });

        return new Promise((resolve, reject) => {
            this.provider.once('sync', (evt) => {
                console.log('%c远程数据加载完成', 'background: blue; color: #fff;', evt);
                resolve(evt);
            });
        });
    }

    async initDocFromMigrateData() {
        // 代表是发版之后的新建文档
        const res = await this.docService.getMigrateData(this.docId);
        const data = res.data;
        data.id = this.docId;
        await this.doc.initBySnapshot(data, this.container);
        this.initScrollTop();
    }

    async initDocAsEmpty() {
        const p = this.doc.schemas.createSnapshot('paragraph', []);
        const root = this.doc.schemas.createSnapshot('root', [this.docId, [p]]);
        await this.doc.initBySnapshot(root, this.container);
        this.diffIds.insert.add(p.id);
        this.diffIds.insert.add(root.id);
        if (this.isEditAllow) {
            this.doc.toggleReadonly(false);
            // requestIdleCallback(() => {
            //     this.doc.selection.setCursorAtBlock(p.id, true);
            // });
        }
    }

    private diffIds = {
        insert: new Set<string>(),
        update: new Set<string>(),
        delete: new Set<string>()
    };

    timer: any;

    private _diffWorkHandler = (events: Y.YEvent<any>[], tr: Y.Transaction) => {
        // 不是本地变化直接返回
        if (!tr.local) return;

        const _ids = new Set<string>();

        events.forEach(e => {
            const { path, changes, target } = e;
            // at top level, it`s mean that block is created or deleted
            if (!path.length) {
                changes.keys.forEach((change, key) => {
                    switch (change.action) {
                        case 'add':
                            if (this.diffIds.delete.has(key)) {
                                this.diffIds.delete.delete(key);
                            } else {
                                this.diffIds.insert.add(key);
                            }
                            _ids.add(key);
                            break;
                        case 'delete':
                            if (this.diffIds.insert.has(key)) {
                                this.diffIds.insert.delete(key);
                            } else {
                                this.diffIds.delete.add(key);
                            }
                            break;
                        case 'update':
                            if (this.diffIds.delete.has(key) || this.diffIds.insert.has(key)) return;
                            this.diffIds.update.add(key);
                            _ids.add(key);
                            break;
                    }
                });
                return;
            }

            const blockId = path[0] as string;
            this.diffIds.update.add(blockId);
            _ids.add(blockId);
        });

        const time = Date.now();

        if (tr.origin !== ORIGIN_SKIP_SYNC) {
            this.doc.crud.transact(() => {
                _ids.forEach(id => {
                    const yb = this.doc.yBlockMap.get(id);
                    if (yb) {
                        yb.get('meta').set('updated', {
                            t: time,
                            u: this.docDetail.localUser.userId,
                            n: this.docDetail.localUser.userName
                        });
                    }
                });
            }, ORIGIN_SKIP_SYNC);
        }

        if (this.timer) clearTimeout(this.timer);
        this.timer = setTimeout(async () => {
            await this.syncDiffToRemote();
        }, 500);
    };

    initDocExtra() {
        this.afterInit.emit();
        this.doc.yBlockMap.observeDeep(this._diffWorkHandler);
        this.doc.onDestroy(() => {
            this.doc.yBlockMap.unobserveDeep(this._diffWorkHandler);
        });

        // 是否有上次没保存上的diff内容
        this.persistence.get('_diffIds').then(res => {
            if (!res) return;
            const data = JSON.parse(res);
            this.diffIds.insert = new Set([...data.insert, ...this.diffIds.insert]);
            this.diffIds.update = new Set([...data.update, ...this.diffIds.update]);
            this.diffIds.delete = new Set([...data.delete.filter(id => !this.diffIds.insert.has(id) && !this.diffIds.delete.has(id)), ...this.diffIds.delete]);
            this.syncDiffToRemote().then(async () => {
                await this.persistence.set('_diffIds', null);
            });
        });

        if (!this.isEditAllow) return;
        this._editorAwareness = new BlockCraftAwareness(this.doc, this.provider.awareness);
        this._editorAwareness.setLocalUser({
            id: this.docDetail.localUser.userId,
            name: this.docDetail.localUser.userName
        });
    }

    storeDiffIdsToLocal() {
        return this.persistence.set('_diffIds', JSON.stringify({
            insert: Array.from(this.diffIds.insert),
            delete: Array.from(this.diffIds.delete),
            update: Array.from(this.diffIds.update)
        }));
    }

    isDiffSavePending = false;
    diffSaveCancel$ = new Subject<void>();

    syncDiffToRemote() {
        if (!this.diffIds.delete.size && !this.diffIds.update.size && !this.diffIds.insert.size) return;
        this.isDiffSavePending && this.diffSaveCancel$.next();

        const id2DiffData = (id: string, mode: 'Update' | 'Insert') => {
            const block = this.doc.getBlockById(id);
            const snapshot = block.toSnapshot(false);
            if (snapshot.nodeType !== BlockNodeType.editable) {
                // @ts-ignore
                snapshot.children = block.yBlock.get('children').toArray();
            } else {
                snapshot['content'] = snapshot.children;
                snapshot['text'] = block.textContent();
                delete snapshot.children;
            }
            snapshot['parentId'] = block.parentId;
            snapshot['mode'] = mode;
            return snapshot;
        };

        const diffBlocks = [
            ...Array.from(this.diffIds.update).filter(id => this.doc.yBlockMap.has(id)).map(id => id2DiffData(id, 'Update')),
            ...Array.from(this.diffIds.insert).map(id => id2DiffData(id, 'Insert')),
            ...Array.from(this.diffIds.delete).map(id => ({ id, mode: 'Delete' }))
        ];
        this.isDiffSavePending = true;
        return this.docService.diffStoreSnapshot(this.docId, diffBlocks, this.diffSaveCancel$).then(() => {
            this.isDiffSavePending = false;
            this.diffIds.insert.clear();
            this.diffIds.update.clear();
            this.diffIds.delete.clear();
        }).catch(async e => {
            if (e.canceled) return;
            await this.storeDiffIdsToLocal();
        });
    }

    // -------------------操作部分--------------
    setReadonly(v: boolean) {
        if (v === this.doc.isReadonly) return;
        this.doc.toggleReadonly(v);
    }

    // -------------------编辑部分--------------
    async importFromMarkdown() {
        const files = await this.injector.get(DOC_FILE_SERVICE_TOKEN).inputFiles('.md', false);
        if (!files?.length) return;
        const file = files[0];
        const text = await file.text();
        const mdAdapter = this.injector.get(DOC_ADAPTER_SERVICE_TOKEN).getAdapter(ClipboardDataType.RTF);
        if (!mdAdapter) return;
        const snapshot = await mdAdapter.toSnapshot(text);
        if (!snapshot) return;
        this.doc.crud.insertBlocks(this.doc.rootId, 0, snapshot.children as IBlockSnapshot[]);
    }

    exportMarkdown() {
        return new DocExportManager(this.doc).exportToMarkdown(`${this.docDetail.name}.md`);
    }

    exportImg() {
        return new DocExportManager(this.doc).exportToJpeg(`${this.docDetail.name}.jpg`, {
            bgcolor: '#fff',
            scale: 2.0
        });
    }

    exportJSON() {
        return new DocExportManager(this.doc).exportToJson(`${this.docDetail.name}.json`);
    }

    storeScrollTop() {
        const scrollTop = this.doc?.scrollContainer?.scrollTop;
        if (typeof scrollTop !== 'number') return;
        localStorage.setItem('editor-cache-scroll' + this.docDetail.id, scrollTop + '');
    }

    initScrollTop() {
        const query = this.activeRoute.snapshot.queryParams;
        const blockId = query['blockId'];
        if (blockId) {
            try {
                this.scrollToBlock(blockId, true);
            } catch (e) {
            }
        } else {
            const scroll = localStorage.getItem('editor-cache-scroll' + this.docDetail.id);
            if (scroll) {
                this.doc.scrollContainer?.scrollTo({ top: Number(scroll) });
            }
        }
    }

    scrollToBlock(blockId: string, toast = false, smooth = true) {
        let block;
        try {
            block = this.doc.getBlockById(blockId);
        } catch (e) {
            toast && this.render.warning('未找到指定段落');
            return;
        }
        block.hostElement.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto' });
        block.hostElement.style.backgroundColor = '#f0f0f0';
        toast && this.render.info('已定位到指定段落');
        setTimeout(() => {
            block.hostElement.style.backgroundColor = '';
        }, 800);
    }

    getJSON() {
        return this.doc.root.toSnapshot(true);
    }

    storeJSON() {
        return this.docService.storeMigrateData(this.docDetail.id, this.getJSON());
    }

    protected readonly SyncStatus = SyncStatus;
}
