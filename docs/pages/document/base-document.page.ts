import { Component, DestroyRef, ElementRef, Inject, ViewChild } from '@angular/core';
import {
    ExportConfigMenuComponent
} from '@/pages/docs/pages/document/components/export-config-menu/export-config-menu';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AppContext } from '@ccc/core-common';
import { CsesContextService, CsesRenderService } from '@ccc/cses-common';
import {
    DocService,
    DocsPermissionService,
    EntityService, IDocConfigs,
    IDocDetail,
    RouterService,
    UserService
} from '@/pages/docs/services';
import { ElectronService } from '@/app/core/services';
import { NzMessageService } from 'ng-zorro-antd/message';
import { Overlay } from '@angular/cdk/overlay';
import { ActivatedRoute } from '@angular/router';
import { DocExportManager } from '@ccc/blockcraft';
import { setDocFontSize } from '@/pages/docs/pages/document/utils';

@Component({
    selector: 'dl-base-document-page',
    template: '',
    standalone: true
})
export class BaseDocumentPage {
    @ViewChild('pageMain', { read: ElementRef }) pageMain!: ElementRef<HTMLElement>;

    docDetail: IDocDetail | null = null;
    docConfigs: IDocConfigs = {
        pageWidth: 'default',
        fontSize: 'default'
    };

    showLeftAside = false;
    activeAsideTabIdx = 0;
    watermarkText = '';

    constructor(
        @Inject(AppContext) public ctx: CsesContextService,
        public readonly render: CsesRenderService,
        protected router: RouterService,
        protected userService: UserService,
        protected entityService: EntityService,
        protected docService: DocService,
        protected permissionService: DocsPermissionService,
        protected electronService: ElectronService,
        protected destroyRef: DestroyRef,
        protected message: NzMessageService,
        public readonly overlay: Overlay,
        public readonly activeRoute: ActivatedRoute
    ) {
    }

    initConfigs() {
        const cacheConfigs = localStorage.getItem('dl-doc-page-configs');
        if (cacheConfigs) {
            this.docConfigs = JSON.parse(cacheConfigs);
            this.setFontSize();
        }
    }

    setFontSize(size: string = this.docConfigs.fontSize) {
        setDocFontSize(size);
    }

    async export(doc: BlockCraft.Doc, type: 'pdf' | 'img' | 'html' | 'snapshot', title = this.docDetail?.name) {
        if (!doc) return;

        if (type === 'snapshot') {
            return new DocExportManager(doc).exportToJson(`${name}.json`);
        }

        let exportConfig = {};
        if (type === 'pdf') {
            exportConfig = await this.getExportConfigs();
            if (!exportConfig) return;
        }

        const id = this.message.loading('正在导出，请稍候...').messageId;
        this.destroyRef.onDestroy(() => {
            this.message.remove(id);
        });
        try {
            await this.electronService.ipcRenderer.invoke('store:setLocal', 'doc-export-json', JSON.stringify(doc.root.toSnapshot(true)));
            await this.electronService.ipcRenderer.invoke('doc:export', {
                id: this.docDetail.id,
                title,
                type,
                watermarkText: this.watermarkText,
                exportConfig
            });
        } catch (e) {
            this.message.error('导出失败');
        } finally {
            this.message.remove(id);
        }
    }

    async print(doc: BlockCraft.Doc) {
        await this.electronService.ipcRenderer.invoke('store:setLocal', 'doc-export-json', JSON.stringify(doc.root.toSnapshot(true)));
        this.electronService.ipcRenderer.invoke('doc:print', {
            id: this.docDetail.id,
            type: 'pdf',
            watermarkText: this.watermarkText,
        });
    }

    getExportConfigs() {
        const { cpr, overlayRef } = this.entityService.overlayService.showDialog(ExportConfigMenuComponent, {
            hasBackdrop: true
        });
        return new Promise((resolve, reject) => {
            cpr.instance.onOk.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(async form => {
                overlayRef.dispose();
                resolve(form);
            });
        });
    }

}
