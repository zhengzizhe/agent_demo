import { Component, EventEmitter, Inject, Injector, Input, Output, ViewChild, ViewContainerRef } from '@angular/core';
import {
    AttachmentExtensionPlugin,
    BLOCK_CREATOR_SERVICE_TOKEN,
    BlockCraftDoc,
    BookmarkBlockExtensionPlugin,
    CodeInlineEditorBinding,
    ConsoleLogger,
    DOC_ADAPTER_SERVICE_TOKEN,
    DOC_FILE_SERVICE_TOKEN,
    DOC_LINK_PREVIEWER_SERVICE_TOKEN,
    DOC_MESSAGE_SERVICE_TOKEN,
    DocLinkPreviewerService,
    EmbedFrameExtensionPlugin,
    ImgToolbarPlugin,
    InlineLinkExtension,
    OrderedBlockPlugin,
    TableBlockBinding
} from '@ccc/blockcraft';
import { CsesDocFileService } from './services/doc-file.service';
import { CsesDocMessageService } from './services/doc-message.service';
import { CsesBlockCreatorService } from './services/block-creator.service';
import { AdapterService } from './services/adapter.service';
import { MENTION_EMBED_CONVERTER, OLD_LINK_EMBED_CONVERTER, SchemaStore, TASK_EMBED_CONVERTER } from './const';
import { IndexeddbPersistence } from 'y-indexeddb';
import { CsesContextService, CsesRenderService } from '@ccc/cses-common';
import * as Y from 'yjs';
import { WebsocketProvider } from 'y-websocket';
import { NzBackTopComponent } from 'ng-zorro-antd/back-top';
import { DOC_LINK_HOST, DocService, IDocDetail, RouterService } from '@/pages/docs/services';
import { DlSpinComponent } from '@/pages/docs/components';
import { NgIf } from '@angular/common';
import { AppContext } from '@ccc/core-common';

let WS_URI = 'ws://ws-doc-pre.cses7.com';
// const WS_URI = 'ws://193.168.2.100:30204/collaborate';
// const WS_URI = 'ws://ws-doc.cses7.com';
// const WS_URI = 'ws://196.168.1.81:1234/collaborate';

@Component({
    selector: 'doc-version-reader',
    template: `
        <div style="padding: 30px 0 60px;">
            <ng-container #container></ng-container>
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
        { provide: DOC_LINK_PREVIEWER_SERVICE_TOKEN, useClass: DocLinkPreviewerService }
    ],
    imports: [
        NzBackTopComponent,
        DlSpinComponent,
        NgIf
    ],
    standalone: true
})
export class DocVersionReader {
    @Input({ required: true })
    docDetail!: IDocDetail;

    private _versionSnapshot = '';
    @Input({ required: true })
    set versionSnapshot(val: string) {
        if (!val || val === this._versionSnapshot) return;
        this._versionSnapshot = val;
        this.checkoutVersion(this._versionSnapshot);
    }

    get versionSnapshot() {
        return this._versionSnapshot;
    }

    @Output()
    readonlyChange = new EventEmitter<boolean>();

    @ViewChild('container', { static: true, read: ViewContainerRef }) container!: ViewContainerRef;

    doc!: BlockCraft.Doc;

    originYDoc: Y.Doc;

    constructor(
        @Inject(AppContext) public ctx: CsesContextService,
        private readonly router: RouterService,
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
        this.originYDoc?.destroy()
        this.destroyDoc();
    }

    async createDoc() {
        // 获取版本
        if (!this.versionSnapshot) return;
        const ySnapshot = this.b2s(this.versionSnapshot);

        const originYDoc = new Y.Doc({
            guid: this.docId,
            gc: false
        });

        await this.getLocal(originYDoc);
        await this.enterRoom(originYDoc);

        this.originYDoc = originYDoc;

        const yDoc = Y.createDocFromSnapshot(originYDoc, ySnapshot);

        await this.initDocFromYDoc(yDoc);
    }

    b2s(data: string) {
        const update = Uint8Array.from(atob(data), c => c.charCodeAt(0));
        return Y.decodeSnapshot(update);
    }

    async initDocFromYDoc(yDoc: Y.Doc) {
        this.creatBlockCraftDoc(yDoc);

        const initFnByY = async () => {
            const yRoot = this.doc.yBlockMap.get(this.docId);
            if (yRoot) {
                await this.doc.initByYBlock(yRoot, this.container);
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
    }

    destroyDoc() {
        this.doc?.yDoc.destroy();
        this.doc?.destroy();
        this.doc = null;
    }

    checkoutVersion(data: string) {
        if (!this.originYDoc) return;
        const ySnapshot = this.b2s(data);
        this.destroyDoc();
        const yDoc = Y.createDocFromSnapshot(this.originYDoc, ySnapshot);
        this.initDocFromYDoc(yDoc);
    }

    creatBlockCraftDoc(yDoc: Y.Doc) {
        this.doc = new BlockCraftDoc({
            yDoc,
            docId: this.docId,
            schemas: SchemaStore,
            logger: new ConsoleLogger(),
            injector: this.injector,
            embeds: [['task', TASK_EMBED_CONVERTER], ['mention', MENTION_EMBED_CONVERTER], ['link', OLD_LINK_EMBED_CONVERTER]],
            plugins: [
                new OrderedBlockPlugin(),
                new TableBlockBinding(),
                new CodeInlineEditorBinding(),
                new ImgToolbarPlugin(),
                new AttachmentExtensionPlugin(),
                new EmbedFrameExtensionPlugin(),
                new BookmarkBlockExtensionPlugin(),
                new InlineLinkExtension((uri) => {
                    if (uri.startsWith(DOC_LINK_HOST)) {
                        const _uri = uri.replace(DOC_LINK_HOST, '');
                        if (_uri.startsWith('document')) {
                            const [_docId, params] = _uri.split('/')[1].split('?');
                            if (_docId === this.docId) {
                                if (params && params.startsWith('blockId')) this.scrollToBlock(params.split('=')[1]);
                                return;
                            }
                            this.router.navigateToNewTab(_uri.replace(DOC_LINK_HOST, ''));
                        }
                    } else {
                        window.open(uri, '_blank');
                    }
                })
            ],
            readonly: true
        });
    }

    getLocal(yDoc: Y.Doc) {
        const persistence = new IndexeddbPersistence(this.indexeddbId, yDoc);
        return new Promise((resolve, reject) => {
            persistence.once('synced', async () => {
                persistence.destroy();
                resolve(true);
            });
        });
    }

    enterRoom(yDoc: Y.Doc) {
        const provider = new WebsocketProvider(WS_URI, this.docId, yDoc, {
            maxBackoffTime: 10000
        });

        return new Promise((resolve, reject) => {
            provider.once('sync', (evt) => {
                provider.disconnect();
                provider.destroy();
                resolve(evt);
            });
        });
    }

    scrollToBlock(blockId: string) {
        let block;
        try {
            block = this.doc.getBlockById(blockId);
        } catch (e) {
            return;
        }
        block.hostElement.style.backgroundColor = '#f0f0f0';
        setTimeout(() => {
            block.hostElement.style.backgroundColor = '';
        }, 800);
    }

}
