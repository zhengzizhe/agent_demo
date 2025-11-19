import { Component, DestroyRef, ElementRef, Inject, ViewChild } from '@angular/core';
import {
    copyDocVersionLink,
    DocService, DocsPermissionService,
    EntityService, EntityType,
    IDocConfigs,
    IDocDetail, IDocVersionInfo,
    PermissionRole,
    RouterService,
    UserService
} from '@/pages/docs/services';
import { BlockCraftEditor } from '@/pages/docs/pages/document/components/blockcraft-editor/editor.component';
import { Date2Pipe } from '@/pipes/date2.pipe';
import { DlButton, DlUserProfileComponent } from '@/pages/docs/components';
import { DocAsideLeftComponent } from '@/pages/docs/pages/document/components/doc-aside-left/doc-aside-left';
import { DocAsideRightComponent } from '@/pages/docs/pages/document/components/doc-aside-right/doc-aside-right';
import { DocWaterMark } from '@/pages/docs/pages/document/components/doc-water-mark/doc-water-mark';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AppContext, ScrollRestoreDirective } from '@ccc/core-common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DocVersionReader } from '@/pages/docs/pages/document/components/blockcraft-editor/doc-version-reader';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';
import { DocSettingMenuComponent } from '@/pages/docs/pages/document/components/doc-setting-menu/doc-setting-menu';
import { IDocSettingMenuItem } from '@/pages/docs/pages/document/components/doc-setting-menu/type';
import { BaseDocumentPage } from '@/pages/docs/pages/document/base-document.page';

@Component({
    selector: 'document-version-page',
    templateUrl: 'document-version-page.html',
    styleUrls: ['document-version-page.scss', '../document.page.scss'],
    imports: [
        BlockCraftEditor,
        Date2Pipe,
        DlUserProfileComponent,
        DocAsideLeftComponent,
        DocAsideRightComponent,
        DocWaterMark,
        FormsModule,
        NgIf,
        ScrollRestoreDirective,
        DocVersionReader,
        NzDropDownDirective,
        NzDropdownMenuComponent,
        DlButton,
        NzTooltipDirective,
        DocSettingMenuComponent
    ],
    standalone: true
})
export class DocumentVersionPage extends BaseDocumentPage {
    @ViewChild('editor', { read: DocVersionReader }) editor!: DocVersionReader;

    docId = '';
    docName: string = '';

    version: string = '';
    versionInfo: IDocVersionInfo | null = null;
    versionList: IDocVersionInfo[] = [];

    dropMenuVisibleMap = {
        versionList: false,
        settings: false
    };

    ngOnInit() {
        this.activeRoute.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(async (paramsMap) => {
            const docId = this.activeRoute.snapshot.paramMap.get('id');
            this.version = paramsMap.get('version');
            if (docId) {
                this.docId = docId;
                await this.initData(this.docId);
            }
        });
    }

    async initData(id: string) {
        this.docDetail = null;
        this.versionInfo = null;
        try {
            const res = await this.entityService.getDocumentDetail(id);
            res.nodeType = EntityType.Document;

            await this.getVersionInfo(id, this.version);

            this.docDetail = res;

            const userInfo = this.docDetail.owner;
            this.watermarkText = userInfo.userName + '  (' + userInfo.deptName + '/' + userInfo.orgName + ')';

        } catch (e) {
            this.render.error(typeof e === 'string' ? e : (e.error || '文档无法访问'));
            this.router.layoutService.closeCurrentTab();
            this.router.navigateToNewTab('');
        }
        this.initConfigs();
    }

    async getVersionInfo(id: string, version: string) {
        this.versionInfo = await this.docService.readVersion(id, version);
        this.docName = this.versionInfo.docName || '未命名文档';
        this.router.setCurrentTabTitle(this.versionInfo.name);
    }

    getVersionList() {
        this.docService.getVersionList(this.docDetail.id).then(res => {
            this.versionList = res.items;
        });
    }

    async checkoutVersion(evt: IDocVersionInfo) {
        this.dropMenuVisibleMap.versionList = false;
        this.version = evt.version;
        await this.getVersionInfo(this.docId, evt.version);
    }

    backOriginDoc() {
        if (!this.docId) return;
        const curTab = this.router.layoutService.currentTab;
        this.router.navigateToDoc(this.docId, this.docDetail?.name).then(() => {
            this.router.layoutService.closeTab(curTab);
        });
    }

    copyVersionLink() {
        copyDocVersionLink(this.docId, this.version).then(() => {
            this.render.success('复制成功');
        });
    }

    onSettingMenuClick(item: IDocSettingMenuItem) {
        this.dropMenuVisibleMap.settings = false;
        switch (item.name) {
            case 'pageWidth':
                this.docConfigs.pageWidth = item.value;
                break;
            case 'fontSize':
                this.docConfigs.fontSize = item.value;
                this.setFontSize();
                break;
            case 'export':
                this.export(this.editor.doc, item.value, this.versionInfo.name);
                return;
        }
    }

    protected readonly PermissionRole = PermissionRole;
}
