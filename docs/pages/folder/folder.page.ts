import { Component, DestroyRef, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
    DlBreadcrumbComponent,
    DlButton, DlGlobalSearcherComponent,
    DlTabComponent,
    EntityDetailDialogComponent,
    EntityTableComponent,
    FolderCardList,
    INavItem,
    ListMode,
    PageBtnGroupComponent
} from '../../components';
import {
    copyEntityLink,
    DocsOverlayService,
    DocsPermissionService,
    EntityService,
    EntityType,
    getFilteredOpMenuList,
    IDlContextMenuItem, IDocumentEntity,
    IEntity,
    IFolderDetail,
    IFolderEntity, OpTypes,
    OpTypesStr,
    PermissionRole,
    RouterService
} from '../../services';
import { ClassifyDocTabs } from '../space/const';
import { NgIf } from '@angular/common';
import { CdkConnectedOverlay } from '@angular/cdk/overlay';
import { CsesRenderService } from '@ccc/cses-common';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'folder-page.dl-page-main',
    templateUrl: 'folder.page.html',
    styleUrls: ['folder.page.scss'],
    imports: [
        DlButton,
        DlTabComponent,
        DlBreadcrumbComponent,
        EntityTableComponent,
        FolderCardList,
        PageBtnGroupComponent,
        NgIf,
        CdkConnectedOverlay,
        DlGlobalSearcherComponent
    ],
    standalone: true
})
export class FolderPage {

    protected readonly tabs = ClassifyDocTabs;

    folderId = '';
    folderDetail: IFolderDetail | null = null;
    paths: INavItem['origin'][] = [];

    listMode: ListMode = 'list';

    isManager = true;

    @ViewChild('folderCardList') folderCardList!: FolderCardList;
    @ViewChild('fileTable') fileTable!: EntityTableComponent;

    folderListQueryParams = { id: '' };

    fileTableQueryParams = { id: '' };

    routeQueryParams: Record<string, string> = {};

    opsList = [];
    isShowOpMenu = false;

    folderCardDisableOps: OpTypesStr[] = [];

    constructor(
        private entityService: EntityService,
        private routerService: RouterService,
        private activeRoute: ActivatedRoute,
        private permissionService: DocsPermissionService,
        private overlayService: DocsOverlayService,
        private render: CsesRenderService,
        private destroyRef: DestroyRef
    ) {
    }

    ngOnInit() {
        this.activeRoute.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(route => {
            this.refreshPage();
        });
    }

    ngOnDestroy() {
    }

    refreshPage() {
        this.folderId = '';
        this.folderDetail = null;
        this.routeQueryParams = this.activeRoute.snapshot.queryParams;
        this.opsList = [];
        this.isManager = false;

        const id = this.activeRoute.snapshot.paramMap.get('id');
        id && this.initData(id);
    }

    async initData(id: string) {
        this.folderId = id;

        try {
            const res = await this.entityService.getFolderDetail(id);
            res.nodeType = EntityType.Folder;

            this.folderListQueryParams.id = this.folderId;
            this.fileTableQueryParams.id = this.folderId;

            this.folderDetail = res;
            this.folderDetail.nodeType = EntityType.Folder;

            this.folderCardDisableOps = this.routeQueryParams.from === 'share' ? ['move', 'delete'] : [];

            // 拿到路径
            await this.getPaths(this.routeQueryParams);
            this.isManager = this.folderDetail.localUser?.role >= PermissionRole.manager;
            this.routerService.setCurrentTabTitle(this.folderDetail.name);
        } catch (e) {
            this.render.error(e.error || '文件夹无法访问');
            this.routerService.layoutService.closeCurrentTab();
            this.routerService.navigateToNewTab('');
        }

    }

    async getPaths(query) {
        if (!this.folderDetail) return;

        if (query.from === 'share') {
            let path: IEntity[] = [];
            if (query.start && query.start !== this.folderId) {
                path = (await this.entityService.getEntityPath(this.folderId, query.start)).items;
            } else {
                path = [{
                    id: this.folderDetail.id,
                    name: this.folderDetail.name,
                    nodeType: EntityType.Folder,
                    spaceId: this.folderDetail.spaceId
                }];
            }

            const paths: INavItem['origin'][] = path.map(item => {
                return {
                    id: item.id,
                    name: item.name,
                    route: `folder/${item.id}?from=share&start=${this.routeQueryParams.start || this.folderId}`
                };
            });
            paths.unshift({
                id: 'share',
                name: '共享',
                route: 'share'
            });
            this.paths = paths;
            return;
        }

        const spaceId = this.folderDetail.spaceId;
        const path = (await this.entityService.getEntityPath(this.folderId, spaceId)).items;

        this.paths = path.map((item) => {
            return {
                id: item.id,
                name: item.name,
                route: item.nodeType === 'space'
                    ? `space/${item.id}`
                    : `space/${spaceId}/folder/${item.id}`,
                icon: item.nodeType === 'space' ? 'bc_kongjian' : undefined
            };
        });
    }

    openOpMenus() {
        this.opsList = getFilteredOpMenuList(this.folderDetail.localUser.role, this.folderDetail.localUser, this.folderCardDisableOps);
    }

    onRefresh() {
        this.folderCardList.refresh();
        this.fileTable.refresh();
        this.routerService.refreshPage();
    }

    onNavToFolder = (folder: IFolderEntity) => {
        if (this.routeQueryParams.from === 'share') {
            this.routerService.navigateByUri(`folder/${folder.id}?from=share&start=${this.routeQueryParams.start || this.folderId}`);
        } else {
            this.routerService.navigate(
                ['space', folder.spaceId, 'folder', folder.id]
            );
        }
    };

    onNavToDoc(doc: IEntity) {
        if (this.routeQueryParams.from === 'share') {
            this.routerService.navigateToDoc(doc.id, doc.name, { page: 'share' });
        } else {
            this.routerService.navigateToDoc(doc.id, doc.name, {
                page: 'folder',
                spaceId: this.folderDetail.spaceId,
                folderId: this.folderId
            });
        }
    }

    onCreateFolder() {
        if (!this.folderDetail) return;
        this.entityService.createFolder(this.folderId, this.folderDetail.spaceId).then(folder => {
            this.entityService.entitiesUpdate$.next({
                type: 'new',
                entities: [folder],
                parentId: this.folderId
            });

            setTimeout(() => {
                if (this.routeQueryParams.from === 'share') {
                    this.routerService.navigateByUri(`folder/${folder.id}?from=share&start=${this.routeQueryParams.start || folder.id}`);
                } else {
                    this.routerService.navigate(['space', folder.spaceId, 'folder', folder.id]);
                }
            }, 500);
        });
    }

    onCreateDoc() {
        if (!this.folderDetail) return;

        this.entityService.createDocument(this.folderId).then(doc => {
            setTimeout(() => {
                this.onNavToDoc(doc);
            }, 500);
        });
    }

    onMenuClick(item: IDlContextMenuItem) {
        this.isShowOpMenu = false;
        switch (item.name) {
            case 'detail':
                this.showDetail();
                break;
            case 'share':
                this.onShare();
                break;
            case 'copyLink':
                this.onCopyLink();
                break;
            case 'delete':
                this.onDelete();
                break;
            case 'move':
                this.onMove();
                break;
            case 'bindTag':
                this.entityService.modifyEntityTags(this.folderDetail);
                break;
            case OpTypes.favorite:
            case OpTypes.cancelFavorite:
                this.onToggleFavorite(this.folderDetail);
                break;
        }
    }

    showDetail() {
        const { cpr } = this.overlayService.showDialog(EntityDetailDialogComponent);
        cpr.setInput('entityDetail', this.folderDetail);
        cpr.setInput('entityType', EntityType.Folder);
        cpr.setInput('localUserRole', this.folderDetail.localUser.role);
        cpr.instance.detailChange.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(form => {
            if (form.name) {
                this.paths[this.paths.length - 1].name = form.name;
                this.paths = [...this.paths];
            }
        });
    }

    onShare() {
        this.permissionService.showShareModal({
            entity: this.folderDetail,
            entityType: EntityType.Folder,
            owner: this.folderDetail.owner,
            localUserRole: this.folderDetail.localUser.role
        });
    }

    onCopyLink() {
        copyEntityLink(this.folderId, EntityType.Folder).then(() => {
            this.render.success('链接已复制');
        });
    }

    onDelete() {
        if (!this.paths.length) return;
        const parent = this.paths.at(-2);
        if (!parent?.route) return;
        this.entityService.tryDeleteFolder(this.folderDetail).then(() => {
            setTimeout(() => {
                this.render.info('文件夹已删除');
                this.routerService.navigateByUri(parent.route || '');
                this.entityService.entitiesUpdate$.next({
                    type: 'delete',
                    entities: [this.folderDetail],
                    parentId: parent.id
                });
            }, 500);
        }).catch(e => {
            if (e.isCancel) return;
            this.render.info(e.error);
        });
    }

    onMove() {
        this.entityService.moveEntities([this.folderDetail], EntityType.Folder).then(res => {
            if (!res.successIds.length) {
                this.render.error('移动失败:' + res.failedMessage[0].errorMessage);
                return;
            }
            const path = res.targetPath;
            // console.log('------paht', path, this.activeRoute.snapshot.parent.paramMap.get('id'));
            // 空间更换
            if (path[0].id !== this.folderDetail.spaceId) {
                setTimeout(() => {
                    this.routerService.navigateToNewTab(this.paths.at(-2)?.route || '').then(() => {
                        this.routerService.layoutService.closeCurrentTab();
                    });
                }, 500);
                return;
            }
            this.refreshPage();
        });
    }

    onFileTableItemsChange(evt: { items: IEntity[]; type: OpTypesStr }) {
        if (evt.type === 'move') {
            this.fileTable.removeItems(evt.items);
        }
    }

    onToggleFavorite(item: IFolderEntity) {
        const value = !item.localUser?.behavior?.favorite;
        this.entityService.setFavoriteEntity(item.id, value).then(res => {
            item.localUser ??= { role: PermissionRole.reader, behavior: {} };
            item.localUser.behavior.favorite = value;
            value ? this.render.success('已收藏') : this.render.info('取消收藏');
        });
    }
}
