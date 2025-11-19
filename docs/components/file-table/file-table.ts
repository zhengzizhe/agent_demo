import {
    Component,
    DestroyRef,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    Output,
    TemplateRef
} from '@angular/core';
import {
    copyEntityLink,
    DocsOverlayService,
    DocsPermissionService,
    EntityService,
    EntityType,
    getFilteredOpMenuList,
    IBasePageQueryParams,
    IDocumentEntity,
    IEntity,
    IOpMenuItem,
    IPagingListResponse,
    OpTypes,
    OpTypesStr,
    PermissionRole,
    RouterService
} from '../../services';
import { FileTableHeadComponent } from './file-table-head';
import { FileOpButton_clearChecked, FileOpButton_deleteDocs, FileOpButton_moveDocs, FileTableHeader } from './const';
import { scrollBottomListenDirective } from '../../directives';
import { IFileOpButtonItem, IFilesQueryParams, IFileTableHeadItem } from './types';
import { CheckBtn, FavoriteIcon } from '../atoms';
import { MatIcon } from '@angular/material/icon';
import { Date2Pipe } from '../../pipes';
import { NzEmptyComponent } from 'ng-zorro-antd/empty';
import { DlSpinComponent } from '../spin';
import { NgIf, NgTemplateOutlet } from '@angular/common';
import { CsesRenderService } from '@ccc/cses-common';
import { CdkConnectedOverlay } from '@angular/cdk/overlay';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EntityDetailDialogComponent } from '@/pages/docs/components';
import { DlEntityIconComponent } from '@/pages/docs/components/atoms/entity-icon';

@Component({
    selector: 'file-table',
    template: `
        <file-table-head [items]="headItems" [(params)]="_params" (paramsChange)="onParamsChange($event)"
                         (colSizesChange)="setColSizesStyle($event)"></file-table-head>

        <div class="file-table-list" scrollBottomListen (onScrollBottom)="onRequestNextPage()" #fileTableList>
            @for (item of itemList; track item.id) {
                <div class="file-table-list-item" (click)="onCheck(item)">

                    <!--                    <span class="btn-select"></span>-->
                    <dl-check-btn class="btn-select" [checked]="checkedList.has(item.id)"></dl-check-btn>

                    <div class="item-info item" (click)="navTo(item, $event)">

                        <dl-entity-icon [size]="40" [nodeType]="EntityType.Document"></dl-entity-icon>

                        <div>
                            <p class="title">
                                <span class="item-title">{{ item.name || '未命名文档' }}</span>
                                @if (item.localUser?.behavior?.favorite) {
                                    <dl-favorite-icon [active]="true"></dl-favorite-icon>
                                }
                                <!--                                <span class="tag">{{  }}</span>-->
                            </p>

                            <!--              <p class="other">-->
                            <!--                <span>12文件夹</span>·<span>12文件</span>-->
                            <!--              </p>-->
                        </div>
                    </div>

                    <div class="item">
                        <ng-container
                            *ngTemplateOutlet="colItem2Template || itemOwnerTemplate; context: {$implicit: item}"></ng-container>
                    </div>

                    <div class="item">
                        <ng-container
                            *ngTemplateOutlet="colItem3Template || itemTimeSortTemplate; context: {$implicit: item}"></ng-container>
                    </div>

                    <span style="min-width: 30px;" class="col-more">
                        <span class="btn-more dl-btn-hover" (click)="onContextMenuToggle($event, item)">
                            <i class="bc_icon bc_gengduo"></i>
                        </span>
                    </span>

                </div>
            }

            <dl-spin *ngIf="_isLoading"></dl-spin>
            @if (!_isLoading && pageQueryParams.totalCount === 0) {
                <nz-empty style="margin: 16px 0"></nz-empty>
            }
        </div>

        <ng-template let-item #itemOwnerTemplate>
            {{ item?.owner?.userName }}
        </ng-template>

        <ng-template let-item #itemTimeSortTemplate>
            {{ item[_params.timeSort!] | date2 }}
        </ng-template>

        <ng-template cdk-connected-overlay [cdkConnectedOverlayOrigin]="fileTableList"
                     [cdkConnectedOverlayHasBackdrop]="false"
                     [cdkConnectedOverlayPositions]="[
                         {originX: 'center', originY: 'bottom', overlayX: 'center', overlayY: 'center', offsetY: -43},
                     ]"
                     [cdkConnectedOverlayOpen]="isShowBtnGroup">
            <div class="dl-btn-group-overlay">
                @for (btn of opButtons; track btn.value) {
                    <button [class]="['btn', btn.className]" (click)="onBtnClick(btn)" nz-tooltip
                            [nzTooltipTitle]="btn.label">
                        <i [class]="['bc_icon', btn.icon]"></i>
                    </button>
                }
            </div>
        </ng-template>
    `,
    styleUrls: ['./file-table.scss'],
    styles: [`
        :host {
            display: block;
            padding: 0 16px;

            .file-table-list {
                height: calc(100% - 40px);
                overflow-y: auto;
            }

            &.single-mode {
                .col-more {
                    display: none;
                }

                ::ng-deep .file-table-list-item {

                    .item {
                        display: none !important;

                        &:first-of-type {
                            display: flex !important;
                        }
                    }

                    .resize-bar {
                        display: none !important;
                    }
                }
            }

        }

        .dl-btn-group-overlay {
            display: flex;
            padding: 8px;
            align-items: center;
            gap: 16px;
            border-radius: var(--cs-border-radius);
            border: 1px solid var(--cs-border-color, #E6E6E6);
            background: var(--cs-bg-color);
            box-shadow: 0 -1px 20px 0 rgba(0, 0, 0, 0.08);

            .btn {
                width: 32px;
                height: 32px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 20px;
                border-radius: 4px;
                transition: all ease .15s;

                > i {
                    line-height: 20px;
                }

                &:hover {
                    background: var(--cs-bg-color-2);
                }

                &.btn-delete {
                    color: var(--cs-error-color);

                    &:hover {
                        background: rgba(245, 63, 63, 0.1);
                    }
                }

                &.btn-removeRecent {
                    color: var(--cs-error-color);
                }

                &.btn-clear {
                    position: relative;

                    &:hover {
                        > i {
                            color: var(--cs-primary-color-disabled);
                        }
                    }

                    > i {
                        color: var(--cs-primary-color);
                    }

                    &::after {
                        content: '';
                        position: absolute;
                        top: 50%;
                        left: 50%;
                        transform: translate(-50%, -50%);
                        height: 1px;
                        width: 10px;
                        background: #fff;
                    }
                }
            }
        }
    `],
    imports: [
        FileTableHeadComponent,
        scrollBottomListenDirective,
        CheckBtn,
        MatIcon,
        Date2Pipe,
        NzEmptyComponent,
        DlSpinComponent,
        NgIf,
        CdkConnectedOverlay,
        NzTooltipDirective,
        NgTemplateOutlet,
        FavoriteIcon,
        DlEntityIconComponent
    ],
    standalone: true
})
export class EntityTableComponent {
    @Input()
    colItem2Template!: TemplateRef<IEntity>;

    @Input()
    colItem3Template!: TemplateRef<IEntity>;

    @Input()
    requester: (p: any) => Promise<IPagingListResponse<IDocumentEntity>> = this.entityService.getDocList.bind(this.entityService);

    @Input()
    headItems: IFileTableHeadItem[] = FileTableHeader;

    @Input()
    queryParams: IFilesQueryParams & Record<string, any> = {};

    @Input()
    extraButtonGroup: IFileOpButtonItem[] = [];

    @Input()
    @HostBinding('class.single-mode')
    singleMode = false;

    @Input()
    disabledOps: OpTypesStr[] | OpTypes[]

    @Output()
    onNavigate = new EventEmitter<IDocumentEntity>();

    @Output()
    onCheckedChange = new EventEmitter<IDocumentEntity[]>();

    @Output()
    onItemsChange = new EventEmitter<{
        items: IEntity[],
        type: OpTypesStr
    }>();

    protected _params: IFilesQueryParams = {
        timeSort: 'updatedAt',
        documentType: null,
        owner: null
    };

    pageQueryParams: Required<IBasePageQueryParams> = {
        pageNumber: 1,
        pageSize: 20,
        totalCount: 1
    };

    itemList: IDocumentEntity[] = [];

    checkedList = new Set<string>();
    protected _checkedFileList = new Set<IDocumentEntity>();

    protected opButtons: IFileOpButtonItem[] = [];

    isShowBtnGroup = false;
    protected _isLoading = false;

    constructor(
        public readonly entityService: EntityService,
        public readonly router: RouterService,
        public readonly hostEl: ElementRef,
        public readonly render: CsesRenderService,
        private overlayService: DocsOverlayService,
        private permissionService: DocsPermissionService,
        private destroyRef: DestroyRef
    ) {
    }

    ngOnInit() {
        this.refresh();
    }

    ngOnDestroy() {
        this.isShowBtnGroup = false;
    }

    resetData() {
        this.clearChecked();
        this.itemList = [];
        this.pageQueryParams.pageNumber = 1;
    }

    refresh() {
        this.resetData();

        this.headItems = structuredClone(this.headItems);

        this._params = this.headItems.reduce((acc, cur) => {
            if (cur.type === 'none') return acc;
            acc[cur.value] = cur.activeFilter || null;
            return acc;
        }, {});

        this.requestData();
    }

    onParamsChange(params: IFilesQueryParams) {
        this.resetData();
        this.requestData();
    }

    async requestData() {
        if (this._isLoading || this.itemList.length >= this.pageQueryParams.totalCount) return;
        this._isLoading = true;
        const params = { ...this._params, ...this.queryParams, ...this.pageQueryParams }
        return this.requester(params).then(data => {
            this.itemList = this.itemList.concat(data.items || []);
            this.pageQueryParams.totalCount = data.totalCount;
        }).catch(e => {
            this.render.error(e.error)
            throw e
        }).finally(() => {
            this._isLoading = false;
        })
    }

    setColSizesStyle(colSizes: number[]) {
        if (!colSizes?.length) return;
        this.hostEl.nativeElement.style.setProperty('--col-size-1', colSizes[0] + 'px');
        this.hostEl.nativeElement.style.setProperty('--col-size-2', colSizes[1] + 'px');
        this.hostEl.nativeElement.style.setProperty('--col-size-3', colSizes[2] + 'px');
        // this.hostEl.nativeElement.style.setProperty('--col-size-4', colSizes[3] + 'px')
    }

    onRequestNextPage() {
        if (this._isLoading || this.itemList.length >= this.pageQueryParams.totalCount) return;
        this.pageQueryParams.pageNumber++;
        this.requestData().catch(e => {
            this.pageQueryParams.pageNumber--;
        })
    }

    getCheckedItems() {
        return Array.from(this._checkedFileList);
    }

    onCheck(item: IDocumentEntity) {
        if (this.checkedList.has(item.id)) {
            this.checkedList.delete(item.id);
            this._checkedFileList.delete(item);
        } else {
            this.checkedList.add(item.id);
            this._checkedFileList.add(item);
        }
        this.isShowBtnGroup = this.checkedList.size > 0;
        this.checkedChange();
    }

    clearChecked() {
        this.checkedList.clear();
        this._checkedFileList.clear();
        this.isShowBtnGroup = false;
        this.checkedChange();
    }

    async checkedChange() {
        const docs = Array.from(this._checkedFileList);
        // 权限降级
        let role = PermissionRole.owner;
        for (let i = 0; i < docs.length; i++) {
            if (!docs[i].localUser?.role) {
                docs[i].localUser ??= { role: PermissionRole.reader };
                docs[i].localUser.role = await this.permissionService.getEntityLocalUserRole(docs[i].id);
            }
            if (docs[i].localUser.role < role) {
                role = docs[i].localUser.role;
            }
        }

        const _ops_roles = {
            [FileOpButton_deleteDocs.value]: PermissionRole.owner,
            [FileOpButton_moveDocs.value]: PermissionRole.owner
        };
        this.opButtons = [FileOpButton_clearChecked, ...this.extraButtonGroup].concat([FileOpButton_moveDocs, FileOpButton_deleteDocs].filter(item => role >= _ops_roles[item.value]));
        this.onCheckedChange.emit(docs);
    }

    navTo(item: IDocumentEntity, evt: Event) {
        evt.stopPropagation();
        this.onNavigate.emit(item);
    }

    removeItems(items: IEntity[]) {
        const ids = new Set(items.map(v => v.id));
        this.itemList = this.itemList.filter(item => !ids.has(item.id));
        this.checkedList = new Set(Array.from(this.checkedList).filter(id => !ids.has(id)));
        this._checkedFileList = new Set(Array.from(this._checkedFileList).filter(item => !ids.has(item.id)));
        if (this.checkedList.size === 0) {
            this.isShowBtnGroup = false;
        }

    }

    onBtnClick(item: IFileOpButtonItem) {
        item.onClick && item.onClick.call(this);
    }

    async onContextMenuToggle(evt: MouseEvent, doc: IDocumentEntity) {
        evt.preventDefault();
        evt.stopPropagation();

        let role: PermissionRole;
        if (!doc.localUser?.role) {
            role = await this.permissionService.getEntityLocalUserRole(doc.id);
        } else {
            role = doc.localUser?.role;
        }

        const target = evt.target as HTMLElement;
        target.classList.add('active');

        const { close, cpr } = this.overlayService.showContextMenu({
            target,
            menus: getFilteredOpMenuList(role, doc.localUser, this.disabledOps)
        });
        this.destroyRef.onDestroy(() => {
            close();
        });
        cpr.instance.onMenuClick.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe((menu: IOpMenuItem) => {
            close();
            switch (menu.name) {
                case OpTypes.share:
                    this.permissionService.showShareModal({
                        entity: doc,
                        entityType: EntityType.Document,
                        localUserRole: role,
                        owner: doc.owner
                    });
                    break;
                case OpTypes.copyLink:
                    copyEntityLink(doc.id, EntityType.Document).then(res => {
                        this.render.success('链接已复制');
                    });
                    break;
                case OpTypes.delete:
                    this.deleteDocs([doc]);
                    break;
                case OpTypes.detail:
                    this.showFileDetail(doc);
                    break;
                case OpTypes.move:
                    this.moveEntities([doc]);
                    break;
                case OpTypes.bindTag:
                    this.bindTags(doc);
                    break;
                case OpTypes.favorite:
                case OpTypes.cancelFavorite:
                    this.onToggleFavorite(doc);
                    break;
            }
        });
        cpr.instance.onClose.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(() => {
            target.classList.remove('active');
        });
    }

    deleteDocs(docs: IDocumentEntity[]) {
        return this.entityService.tryDeleteDocs(docs).then(res => {
            this.render.success('文档已删除');
            const docIds = docs.map(v => v.id);
            this.itemList = this.itemList.filter(v => !docIds.includes(v.id));
            this.pageQueryParams.totalCount -= docs.length;
            this.onItemsChange.emit({
                items: docs,
                type: 'delete'
            });
        }).catch(e => {
            if (e.isCancel) return;
            this.render.error(e.error || '删除文档失败');
        });
    }

    async showFileDetail(file: IDocumentEntity) {
        const role = await this.permissionService.getEntityLocalUserRole(file.id);
        const { cpr } = this.overlayService.showDialog(EntityDetailDialogComponent);
        cpr.setInput('entityDetail', file);
        cpr.setInput('localUserRole', role);
        cpr.setInput('entityType', EntityType.Document);
        cpr.instance.detailChange.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(form => {
            if (form.name) {
                file.name = form.name;
            }
        });
    }

    moveEntities(docs: IDocumentEntity[]) {
        this.entityService.moveEntities(docs, EntityType.Document).then(res => {
            switch (res.successIds.length) {
                case 0:
                    this.render.error('移动失败:' + res.failedMessage[0].errorMessage);
                    break;
                case docs.length:
                    this.render.success(`移动成功${docs.length}项`);
                    break;
                default:
                    this.render.info(`移动成功${res.successIds.length}项，失败${res.failIds.length}项`);
            }
            // res.successList.forEach(r => {
            // });
            if (res.successIds.length) {
                this.onItemsChange.emit({
                    items: res.successEntities,
                    type: 'move'
                });
            }
        });
    }

    bindTags(doc: IDocumentEntity) {
        this.entityService.modifyEntityTags(doc).then(res => {
            doc['tags'] = res;
        });
    }

    onToggleFavorite(item: IDocumentEntity) {
        const value = !item.localUser?.behavior?.favorite;
        this.entityService.setFavoriteEntity(item.id, value).then(res => {
            item.localUser ??= { role: PermissionRole.reader, behavior: {} };
            item.localUser.behavior.favorite = value;
            value ? this.render.success('已收藏') : this.render.info('取消收藏');
            this.onItemsChange.emit({
                items: [item],
                type: value ? OpTypes.favorite : OpTypes.cancelFavorite
            });
        });
    }

    protected readonly EntityType = EntityType;
}
