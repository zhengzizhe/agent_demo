import { Component, ElementRef, ViewChild, ViewContainerRef } from '@angular/core';
import { DlButton, FavoriteIcon } from '../../components';
import {
    EntityType,
    IAccessConfig,
    PermissionRole
} from '../../services';
import { FormsModule } from '@angular/forms';
import { AppContext, ScrollRestoreDirective } from '@ccc/core-common';
import { AsyncPipe, NgIf } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BlockCraftEditor } from './components/blockcraft-editor/editor.component';
import { SyncStatus, SyncStatusPipe } from './components/blockcraft-editor/syncStatus.pipe';
import { EditMode, EditModeList, IEditModeItem } from './const';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { Date2Pipe } from '@/pipes/date2.pipe';
import { CanComponentDeactivate } from '../../app/deactivateGuard';
import { NzBackTopComponent } from 'ng-zorro-antd/back-top';
import { DocWaterMark } from './components/doc-water-mark/doc-water-mark';
import { DocSettingMenuComponent } from './components/doc-setting-menu/doc-setting-menu';
import { IDocSettingMenuItem } from './components/doc-setting-menu/type';
import { DocOpMenuComponent } from './components/doc-op-menu';
import { ShareModalComponent } from '@/pages/docs/components/share-modal/share-modal';
import { DlUserProfileComponent } from '@/pages/docs/components';
import * as Y from 'yjs';
import {
    DlSaveVersionDialogComponent
} from './components/save-version-dialog/saveVersion.dialog';
import { DocAsideLeftComponent } from './components/doc-aside-left/doc-aside-left';
import { DocAsideRightComponent } from './components/doc-aside-right/doc-aside-right';
import { BaseDocumentPage } from '@/pages/docs/pages/document/base-document.page';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';
import { CdkConnectedOverlay, CdkOverlayOrigin } from '@angular/cdk/overlay';
import { nextTick } from '@ccc/blockcraft';

@Component({
    selector: 'document-page',
    templateUrl: './document.page.html',
    styleUrls: ['./document.page.scss'],
    imports: [
        DlButton,
        FormsModule,
        AsyncPipe,
        NgIf,
        BlockCraftEditor,
        SyncStatusPipe,
        DlUserProfileComponent,
        NzDropDownDirective,
        NzDropdownMenuComponent,
        Date2Pipe,
        NzBackTopComponent,
        DocWaterMark,
        DocSettingMenuComponent,
        DocOpMenuComponent,
        ShareModalComponent,
        ScrollRestoreDirective,
        DocAsideLeftComponent,
        DocAsideRightComponent,
        FavoriteIcon,
        NzTooltipDirective,
        CdkConnectedOverlay,
        CdkOverlayOrigin
    ],
    standalone: true
})
export class DocumentPage extends BaseDocumentPage implements CanComponentDeactivate {
    @ViewChild('titleInput', { read: ElementRef }) titleInput!: ElementRef<HTMLInputElement>;
    @ViewChild('editorContainer', { static: true, read: ViewContainerRef }) editorContainer!: ViewContainerRef;
    @ViewChild('editor', { read: BlockCraftEditor }) editor!: BlockCraftEditor;

    protected readonly PermissionRole = PermissionRole;
    viewReady: Promise<void>;
    private viewReadyResolver!: () => void;

    accessConfig: IAccessConfig;

    docName: string = '';

    protected syncStatusCode = SyncStatus.Syncing;

    protected editModes = EditModeList;
    protected currentEditMode = EditModeList[0];

    protected isReadonly = true;
    isEditAllow = false;

    protected dropMenuVisibleMap = {
        opMenu: false,
        shareDialog: false,
        editMode: false,
        settings: false
    };

    ngOnInit() {
        this.activeRoute.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(async (paramsMap) => {
            const docId = paramsMap.get('id');
            if (docId) {
                await this.initData(docId);
            }
        });
    }

    ngAfterViewInit() {
        this.viewReady = new Promise<void>((resolve) => {
            this.viewReadyResolver = resolve;
        });
        this.viewReadyResolver();
    }

    ngOnDestroy() {
        if (!this.docDetail) return;
        if (this.docDetail.name !== this.docName) {
            this.onRename();
        }
    }

    async initData(id: string) {
        this.docDetail = null;
        this.isEditAllow = false;
        this.isReadonly = true;
        this.currentEditMode = EditModeList[0];
        this.accessConfig = undefined;
        try {
            const res = await this.entityService.getDocumentDetail(id);
            res.nodeType = EntityType.Document;

            const isEditAllow = res.localUser.role >= PermissionRole.editor;
            if (!isEditAllow) {
                this.editModes = EditModeList.filter(item => item.value === EditMode.read);
            } else {
                this.editModes = EditModeList;
            }
            this.isEditAllow = isEditAllow;

            this.docName = res.name || '';
            this.docDetail = res;
            this.router.setCurrentTabTitle(res.name || '未命名文档');

            const userInfo = this.docDetail.owner;
            this.watermarkText = userInfo.userName + '  (' + userInfo.deptName + '/' + userInfo.orgName + ')';
        } catch (e) {
            this.render.error(typeof e === 'string' ? e : (e.error || '文档无法访问'));
            this.router.layoutService.closeCurrentTab();
            this.router.navigateToNewTab('');
        }
        this.initConfigs();
        this.permissionService.getShareConfig(this.docDetail.id).then(config => {
            this.accessConfig = config;
        });
    }

    navBack() {
        const queries = this.activeRoute.snapshot.queryParams;
        let uri = '';
        switch (queries.from) {
            case 'space':
                queries.spaceId && (uri = `space/${queries.spaceId}`);
                break;
            case 'folder':
                (queries.spaceId && queries.folderId) && (uri = `space/${queries.spaceId}/folder/${queries.folderId}`);
                break;
            case 'share':
                uri = 'share';
                break;
            default:
                break;
        }
        const curUri = this.router.currentUrl;
        this.router.navigateToNewTab(uri);
        this.router.layoutService.closeTabByUri(curUri);
    }

    // ---------------- 模式切换 ----------------
    onSyncStatusChange($event: SyncStatus) {
        this.syncStatusCode = $event;
        if ($event === SyncStatus.remoteSynced && this.isEditAllow) {
            this.setReadonly(false);

            // 绑定事件
            this.editor.doc.event.bindHotkey({
                key: ['f', 'F'],
                shortKey: true
            }, () => {
                this.activeAsideTabIdx = 3;
                this.showLeftAside = true;
            });
        }
    }

    setReadonly(v: boolean) {
        if (v === this.isReadonly) return;
        this.isReadonly = v;
        this.editor.setReadonly(v);
    }

    onEditorInit() {
        const readonly = !this.isEditAllow;
        this.currentEditMode = readonly ? EditModeList[0] : EditModeList[1];
        this.isReadonly = readonly;
        this.isEditAllow && setTimeout(() => {
            this.titleInput.nativeElement.focus();
        }, 10);
    }

    onShowEditModeMenu(evt: MouseEvent) {
        const target = evt.target as HTMLElement;
        const menus = structuredClone(this.editModes);
        menus.find(item => item.value === this.currentEditMode.value).active = true;
        const { cpr, close } = this.entityService.overlayService.showContextMenu({
            target,
            menus
        });
        this.dropMenuVisibleMap.editMode = true;
        cpr.instance.onMenuClick.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((item: IEditModeItem) => {
            this.onChangeEditMode(item);
            close();
        });
        cpr.instance.destroyRef.onDestroy(() => {
            this.dropMenuVisibleMap.editMode = false;
        });
    }

    onChangeEditMode(mode: IEditModeItem) {
        this.dropMenuVisibleMap.editMode = false;
        if (this.currentEditMode.value === mode.value) return;
        if (!this.isEditAllow || !this.editor.doc.isInitialized) return;
        this.currentEditMode = mode;
        this.setReadonly(this.currentEditMode.value === EditMode.read);
    }

    // ---------------- 模式切换 END ----------------

    // ---------------- 文档操作 ----------------
    isTitleInputComposing = false;

    onTitleBlur() {
        this.onRename().then(name => {
            this.router.setCurrentTabTitle(name);
        });
    }

    onTitleEnter(e: Event) {
        if (this.isTitleInputComposing) return;
        e.preventDefault();
        const selEnd = this.titleInput.nativeElement.selectionEnd;
        const str = this.titleInput.nativeElement.value.substring(selEnd);
        this.docName = this.docName.substring(0, this.titleInput.nativeElement.selectionStart);
        const p = this.editor.doc.schemas.createSnapshot('paragraph', [str]);
        this.editor.doc.crud.insertBlocks(this.editor.doc.rootId, 0, [p]).then(() => {
            this.editor.doc.selection.setCursorAtBlock(p.id, true);
        });
    }

    onRename(): Promise<string> {
        if (this.docDetail.owner.id !== this.userService.ctx.userInfo.userId) {
            this.render.warning('非文档所有者无法修改文档名称');
            return Promise.reject();
        }
        return new Promise((resolve, reject) => {
            if (!this.docDetail) return reject();
            const newName = this.docName;
            if (!newName) {
                this.docName = this.docDetail.name;
                return reject();
            }
            if (newName === this.docDetail.name) return reject();
            this.entityService.modifyDocMeta(this.docDetail.id, { name: newName }).then(res => {
                this.docDetail.name = newName;
                this.render.success('文档名已修改');
                resolve(newName);
            }).catch(err => {
                this.render.error('文档名修改失败');
                this.docName = this.docDetail.name;
                reject();
            });
        });
    }

    onFavorite() {
        if (!this.docDetail) return;
        const value = !this.docDetail.localUser.behavior.favorite;
        this.entityService.setFavoriteEntity(this.docDetail.id, value).then(res => {
            this.docDetail.localUser.behavior.favorite = value;
            value ? this.entityService.render.success('收藏成功') : this.entityService.render.info('取消收藏');
        });
    }

    async onShare() {
        if (!this.docDetail) return;
        this.accessConfig = await this.permissionService.getShareConfig(this.docDetail.id);
        this.dropMenuVisibleMap.shareDialog = true;
    }

    onSaveVersion() {
        if (!this.editor) return;
        const ySnapshot = Y.snapshot(this.editor.doc.yDoc);
        const u = Y.encodeSnapshot(ySnapshot);
        const { cpr, overlayRef } = this.entityService.overlayService.showDialog(DlSaveVersionDialogComponent);
        const now = Date.now();
        cpr.setInput('now', now);
        cpr.setInput('form', {
            name: (this.docDetail.name || '未命名文档') + ' 版本' + Date2Pipe.prototype.transform(now),
            description: ''
        });
        cpr.instance.onSure.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(form => {
            cpr.setInput('isLoading', true);
            this.docService.createVersion({
                ...form,
                data: u,
                id: this.docDetail.id
            }).then(res => {
                this.render.success('保存成功');
                overlayRef.dispose();
            }).catch(e => {
                cpr.setInput('isLoading', false);
                this.render.error(e.error || '保存失败');
            });
        });
    }

    onReadVersion(version: string) {
        if (!this.editor) return;
        this.router.navigateToNewTab(`document-version/${this.docDetail.id}?version=${version}`);
    }

    // ---------------- 文档操作 END ----------------

    // --------------- 配置类 --------------------
    onSettingMenuClick(item: IDocSettingMenuItem) {
        console.log(item);
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
                this.export(this.editor.doc, item.value);
                return;
            case 'print':
                this.print(this.editor.doc);
                return;
            case 'bindTags':
                this.entityService.modifyEntityTags(this.docDetail);
                return;
            case 'move':
                this.entityService.moveEntities([this.docDetail], EntityType.Document).then(res => {
                    if (!res.successIds.length) {
                        return this.render.warning('移动失败');
                    }
                    this.render.success('移动成功');
                });
                return;
            case 'saveVersion':
                if (!this.editor?.doc?.isInitialized) return;
                this.onSaveVersion();
                return;
            case 'version':
                this.onReadVersion(item.value);
                return;

        }
        localStorage.setItem('dl-doc-page-configs', JSON.stringify(this.docConfigs));
    }

    // --------------- 配置类  END--------------------

    canDeactivate(): Promise<boolean> {
        if (!this.docDetail) return Promise.resolve(true);
        return Promise.resolve(true);
    }

    async onDemonstrate() {
        try {
            // @ts-ignore
            const primaryScreen = (await getScreenDetails()).screens.find(
                (screen) => screen.isPrimary
            );
            this.pageMain.nativeElement.requestFullscreen({
                navigationUI: 'hide',
                // @ts-ignore
                screen: primaryScreen
            });
        } catch (err) {
            console.error(err.name, err.message);
        }

    }

    protected readonly EntityType = EntityType;
}
