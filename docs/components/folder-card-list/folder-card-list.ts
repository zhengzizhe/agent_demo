import { Component, DestroyRef, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import {
    copyEntityLink,
    DocsOverlayService,
    DocsPermissionService,
    EntityService,
    EntityType,
    getFilteredOpMenuList,
    IEntity,
    IFolderEntity,
    IOpMenuItem,
    IPagingListResponse,
    OpTypes,
    OpTypesStr,
    PermissionRole,
    RouterService
} from '../../services';
import { NgIf } from '@angular/common';
import { scrollBottomListenDirective } from '../../directives';
import { DlSpinComponent } from '../spin';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { CsesRenderService } from '@ccc/cses-common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DlTagComponent, FavoriteIcon } from '@/pages/docs/components';
import { DlTagRowComponent } from '@/pages/docs/components/tag-row/tag-row';

@Component({
    selector: 'folder-card-list',
    templateUrl: 'folder-card-list.html',
    styleUrls: ['folder-card-list.scss'],
    imports: [
        NgIf,
        scrollBottomListenDirective,
        DlSpinComponent,
        NzDropdownMenuComponent,
        NzDropDownDirective,
        DlTagComponent,
        DlTagRowComponent,
        FavoriteIcon
    ],
    standalone: true,
    host: {
        '[class.empty]': 'pageData.totalCount === 0'
    }
})
export class FolderCardList {
    @Input()
    private entityId = '';

    @Input()
    requester: (p: any) => Promise<IPagingListResponse<IFolderEntity>> = this.entityService.getFolderList.bind(this.entityService);

    @Input()
    queryParams: Record<string, any> = {};

    @Input()
    navigator: (folder: IFolderEntity) => void = (folder) => {
        this.router.navigate(
            ['space', folder.spaceId, 'folder', folder.id]
        );
    };

    @Input()
    disableOps: OpTypesStr[] | OpTypes[];

    @Input()
    withTags = false;

    @Output()
    onItemsChange = new EventEmitter<{
        items: IEntity[],
        type: OpTypesStr
    }>();

    protected showAll = false;

    pageData = {
        pageNumber: 1,
        pageSize: 15,
        totalCount: 0
    };

    folders: IFolderEntity[] = [];

    protected showFolders: IFolderEntity[] = [];

    private _clientWidth = 1200;

    private _resizeObs = new ResizeObserver(async evt => {
        const width = evt[0].contentRect.width;
        this._clientWidth = width;
        this.setShowFolders(width);
    });

    protected _isLoading = false;
    protected showMaxTagCount = 1;

    @ViewChild('listElement', { read: ElementRef }) listElement!: ElementRef<HTMLElement>;

    constructor(
        private entityService: EntityService,
        private permissionService: DocsPermissionService,
        private overlayService: DocsOverlayService,
        private router: RouterService,
        private render: CsesRenderService,
        private destroyRef: DestroyRef,
        private host: ElementRef
    ) {
    }

    ngOnInit() {
        this.refresh();
    }

    async ngAfterViewInit() {
        this._clientWidth = this.listElement.nativeElement.clientWidth;

        this._resizeObs.observe(this.host.nativeElement);
        this.destroyRef.onDestroy(() => {
            this._resizeObs.disconnect();
        });
    }

    async refresh() {
        this.folders = [];
        this.pageData.pageNumber = 1;
        await this.request();
        this.setShowFolders();
    }

    request() {
        if (this._isLoading) return Promise.reject();
        this._isLoading = true;
        return this.requester({
            ...this.queryParams,
            pageNumber: this.pageData.pageNumber,
            pageSize: this.pageData.pageSize
        }).then(res => {
            this.folders.push(...(res.items || []));
            this.pageData.totalCount = res.totalCount;
        }).finally(() => {
            this._isLoading = false;
        });
    }

    requestNextPage() {
        if (this._isLoading || this.folders.length >= this.pageData.totalCount) {
            return Promise.resolve();
        }
        this.pageData.pageNumber++;
        return this.request();
    }

    async onToggleMoreFolders() {
        if (this.showAll) {
            this.showAll = false;
        } else {
            await this.requestNextPage();
            this.showAll = true;
        }
        this.setShowFolders();
    }

    setShowFolders(width = this._clientWidth) {
        if (width > 1440) {
            this.showFolders = this.showAll ? this.folders : this.folders!.slice(0, 10);
            this.listElement.nativeElement.style.setProperty('--card-line-count', '5');
            this.showMaxTagCount = 2;
        } else if (width > 768) {
            this.showFolders = this.showAll ? this.folders : this.folders!.slice(0, 8);
            this.listElement.nativeElement.style.setProperty('--card-line-count', '4');
            this.showMaxTagCount = 1;
        } else {
            this.showFolders = this.showAll ? this.folders : this.folders!.slice(0, 6);
            this.listElement.nativeElement.style.setProperty('--card-line-count', '3');
            this.showMaxTagCount = 1;
        }
    }

    onNavTo(folder: IFolderEntity) {
        this.navigator(folder);
    }

    async onCardMenuDropdownClick(folder: IFolderEntity, evt: MouseEvent) {
        evt.stopPropagation();
        evt.preventDefault();

        const role = await this.permissionService.getEntityLocalUserRole(folder.id);
        const target = evt.target as HTMLElement;
        target.classList.add('active');
        const { cpr, close } = this.overlayService.showContextMenu({
            target,
            menus: getFilteredOpMenuList(role, folder.localUser, this.disableOps)
        });
        cpr.instance.onMenuClick.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe((menu: IOpMenuItem) => {
            close();
            switch (menu.name) {
                case OpTypes.share:
                    this.shareFolder(folder);
                    break;
                case OpTypes.copyLink:
                    copyEntityLink(folder.id, EntityType.Folder).then(res => {
                        this.render.success('链接已复制');
                    });
                    break;
                case OpTypes.delete:
                    this.deleteFolder(folder);
                    break;
                case OpTypes.detail:
                    this.showFolderDetail(folder);
                    break;
                case OpTypes.move:
                    this.moveFolder(folder);
                    break;
                case OpTypes.bindTag:
                    this.entityService.modifyEntityTags(folder).then(res => {
                        folder.tags = res;
                    });
                    break;
                case OpTypes.favorite:
                case OpTypes.cancelFavorite:
                    this.onToggleFavorite(folder);
                    break;

            }
        });
        cpr.instance.onClose.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(() => {
            target.classList.remove('active');
        });
    }

    shareFolder(folder: IFolderEntity) {
        if (!folder) return;
        this.permissionService.getEntityLocalUserRole(folder.id).then(role => {
            this.permissionService.showShareModal({
                entity: folder,
                entityType: EntityType.Folder,
                owner: folder.owner,
                localUserRole: role
            });
        });
    }

    deleteFolder(folder: IFolderEntity) {
        if (!folder) return;
        this.entityService.tryDeleteFolder(folder).then(() => {
            this.entityService.entitiesUpdate$.next({
                type: 'delete',
                entities: [{ ...folder, nodeType: EntityType.Folder }],
                parentId: this.entityId
            });
            this.removeItems([folder.id]);
            this.render.info('文件夹已删除');
        }).catch(e => {
            if (e.isCancel) return;
            this.render.error(e.error || '删除文件夹失败');
        });
    }

    moveFolder(folder: IFolderEntity) {
        this.entityService.moveEntities([folder], EntityType.Folder).then(res => {
            if (!res.successIds.length) {
                this.render.error(res.failedMessage[0].errorMessage || '移动失败');
                return;
            }
            this.render.success('移动成功');
            this.removeItems([folder.id]);
        });
    }

    removeItems(ids: string[]) {
        this.folders = this.folders.filter(f => !ids.includes(f.id));
        this.pageData.totalCount -= ids.length;
        this.setShowFolders();
    }

    async showFolderDetail(folder: IFolderEntity) {
        const role = await this.permissionService.getEntityLocalUserRole(folder.id);
        this.entityService.showEntityDetail(
            folder,
            role,
            EntityType.Folder,
            (form) => {
                if (form.name) {
                    folder.name = form.name;
                }
            }
        );
    }

    onToggleFavorite(item: IFolderEntity) {
        const value = !item.localUser?.behavior?.favorite;
        this.entityService.setFavoriteEntity(item.id, value).then(res => {
            item.localUser ??= { role: PermissionRole.reader, behavior: {} };
            item.localUser.behavior.favorite = value;
            value ? this.render.success('已收藏') : this.render.info('取消收藏');
            this.onItemsChange.emit({
                type: value ? OpTypes.favorite : OpTypes.cancelFavorite,
                items: [item]
            });
        });
    }
}
