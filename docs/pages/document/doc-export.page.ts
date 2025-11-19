import { Component, Injector, ViewChild, ViewContainerRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
    BLOCK_CREATOR_SERVICE_TOKEN,
    BlockCraftDoc,
    DOC_ADAPTER_SERVICE_TOKEN,
    DOC_FILE_SERVICE_TOKEN,
    DOC_LINK_PREVIEWER_SERVICE_TOKEN,
    DOC_MESSAGE_SERVICE_TOKEN, DocLinkPreviewerService,
    NoopLogger
} from '@ccc/blockcraft';
import {
    MENTION_EMBED_CONVERTER, OLD_LINK_EMBED_CONVERTER,
    SchemaStore,
    TASK_EMBED_CONVERTER
} from '@/pages/docs/pages/document/components/blockcraft-editor/const';
import * as Y from 'yjs';
import { CsesDocFileService } from '@/pages/docs/pages/document/components/blockcraft-editor/services/doc-file.service';
import {
    CsesDocMessageService
} from '@/pages/docs/pages/document/components/blockcraft-editor/services/doc-message.service';
import {
    CsesBlockCreatorService
} from '@/pages/docs/pages/document/components/blockcraft-editor/services/block-creator.service';
import { AdapterService } from '@/pages/docs/pages/document/components/blockcraft-editor/services/adapter.service';
import { ElectronService } from '@/app/core/services';
import { DocWaterMark } from '@/pages/docs/pages/document/components/doc-water-mark/doc-water-mark';

@Component({
    selector: 'div.doc-export-pdf-page',
    template: `
        <doc-water-mark [text]="waterMaskText"></doc-water-mark>
        <h1 style="font-size: 24px; font-weight: bolder; margin-bottom: 16px;">{{ docTitle }}</h1>
        <ng-container #container></ng-container>
    `,
    styles: [`
        :host {
            position: relative;
        }
    `],
    providers: [
        { provide: DOC_FILE_SERVICE_TOKEN, useClass: CsesDocFileService },
        { provide: DOC_MESSAGE_SERVICE_TOKEN, useClass: CsesDocMessageService },
        { provide: BLOCK_CREATOR_SERVICE_TOKEN, useClass: CsesBlockCreatorService },
        { provide: DOC_ADAPTER_SERVICE_TOKEN, useClass: AdapterService },
        { provide: DOC_LINK_PREVIEWER_SERVICE_TOKEN, useClass: DocLinkPreviewerService }
    ],
    imports: [DocWaterMark],
    standalone: true
})
export class DocExportPage {
    @ViewChild('container', { static: true, read: ViewContainerRef }) container!: ViewContainerRef;

    constructor(
        private activeRoute: ActivatedRoute,
        private injector: Injector,
        private electronService: ElectronService
    ) {
    }

    docId: string = '';
    docTitle: string = '';
    waterMaskText = '';
    exportType = 'pdf';

    ngOnInit() {
        this.docId = this.activeRoute.snapshot.queryParams['id'];
        this.docTitle = this.activeRoute.snapshot.queryParams['title'] || '';
        this.waterMaskText = this.activeRoute.snapshot.queryParams['water'] || '';
        this.exportType = this.activeRoute.snapshot.queryParams['export'] || 'pdf';
    }

    ngAfterViewInit() {
        const newStyles = document.createElement('style');
        newStyles.innerHTML = `
            html, body {
                all: unset;
                display: block;
                --bc-fs: 14px;
                --bc-lh: 20px;
                background: #fff;
            }

            [data-blockcraft-root="true"] .table-block .table-scrollable {
                overflow-x: unset !important;
                overflow-y: unset !important;
            }

            ${this.exportType === 'pdf' ? `
                [data-blockcraft-root="true"] .table-block .table-wrapper {
                    width: 100% !important;
                }
                [data-blockcraft-root="true"] .table-block table {
                    width: 100% !important;
                }

                [data-blockcraft-root="true"] .table-block table td.table-cell-block {
                    min-width: unset !important;
                }
            ` : ''}
        `;
        document.head.appendChild(newStyles);

        this.createEditor(this.docId).then(async doc => {
            if(this.exportType === 'pdf') {
                document.body.querySelectorAll('colgroup').forEach(node => {
                    node.remove();
                })
            }

            // 取消图片懒加载, 等待图片加载完全
            const imgElements = document.body.querySelectorAll('img');
            imgElements.forEach(img => {
                img.setAttribute('loading', 'eager');
            });
            const imgPromises = Array.from(imgElements).map(img => {
                return new Promise(resolve => {
                    img.onload = resolve;
                });
            });

            try {
                // @ts-expect-error
                const mermaidBlocks = doc.vm.store.values().filter(m => {
                    return m.instance.flavour === 'mermaid';
                });
                for (let m of mermaidBlocks) {
                    const mermaid = m.instance;

                    if (mermaid.props.mode !== 'text') {
                        mermaid.isIntersecting = true;
                        mermaid.hostElement.querySelector('.control-btns')?.remove()
                        mermaid.hostElement.setAttribute('data-mode', mermaid.props.mode);
                        await mermaid.renderGraph();
                    }
                }

                await Promise.all(imgPromises);
                requestAnimationFrame(() => {
                    this.electronService.ipcRenderer.send('doc-export-ready');
                });
            } catch (e) {
                this.electronService.ipcRenderer.send('doc-export-error');
            }
        }).catch(e => {
            this.electronService.ipcRenderer.send('doc-export-error');
        }).finally(() => {
            this.electronService.ipcRenderer.invoke('store:deleteLocal', 'doc-export-json');
        });
    }

    async createEditor(docId: string = this.docId) {
        if (!docId) return;
        const doc = new BlockCraftDoc({
            yDoc: new Y.Doc({
                guid: docId
            }),
            docId,
            schemas: SchemaStore,
            logger: new NoopLogger(),
            injector: this.injector,
            embeds: [['task', TASK_EMBED_CONVERTER], ['mention', MENTION_EMBED_CONVERTER], ['link', OLD_LINK_EMBED_CONVERTER]],
            plugins: [],
            readonly: true
        });

        const json = await this.electronService.ipcRenderer.invoke('store:getLocal', 'doc-export-json');
        const data = JSON.parse(json);
        // const res = await this.docService.getMigrateData(docId);
        // const data = res.data;
        // data.id = docId;
        await doc.initBySnapshot(data, this.container);
        return doc;
    }
}
