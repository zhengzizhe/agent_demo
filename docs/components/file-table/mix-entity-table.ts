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
    DocsOverlayService,
    IBasePageQueryParams, IDlContextMenuItem,
    IEntity,
    IOpMenuItem,
    IPagingListResponse
} from '../../services';
import { FileTableHeadComponent } from './file-table-head';
import { FileOpButton_clearChecked, FileTableHeader } from './const';
import { scrollBottomListenDirective } from '../../directives';
import { IEntityTableOpButtonItem, IFilesQueryParams, IFileTableHeadItem } from './types';
import { CheckBtn, FavoriteIcon } from '../atoms';
import { MatIcon } from '@angular/material/icon';
import { Date2Pipe } from '../../pipes';
import { NzEmptyComponent } from 'ng-zorro-antd/empty';
import { DlSpinComponent, DlSpinMaskComponent } from '../spin';
import { NgIf, NgTemplateOutlet } from '@angular/common';
import { CsesRenderService } from '@ccc/cses-common';
import { CdkConnectedOverlay } from '@angular/cdk/overlay';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DlEntityIconComponent } from '@/pages/docs/components/atoms/entity-icon';

@Component({
    selector: 'dl-mix-entity-table',
    template: `
        <dl-spin-mask [dlSpinning]="isLoading" style="height: 100%;">
            <file-table-head [items]="headItems" [(params)]="_params" (paramsChange)="onParamsChange($event)"
                             (colSizesChange)="setColSizesStyle($event)"></file-table-head>

            <div class="file-table-list" scrollBottomListen (onScrollBottom)="onRequestNextPage()" #fileTableList>
                @for (item of itemList; track item.id) {
                    <div class="file-table-list-item" (click)="onCheck(item)">

                        @if (multiSelect) {
                            <dl-check-btn class="btn-select" [checked]="checkedList.has(item.id)"></dl-check-btn>
                        } @else {
                            <span class="btn-select"></span>
                        }

                        <div class="item-info item">

                            <dl-entity-icon [size]="40" [nodeType]="item.nodeType"></dl-entity-icon>

                            <div>
                                <p class="title">
                                    <span class="item-title">{{ item.name || '未命名文档' }}</span>
                                </p>
                                <!--                            <p class="other">-->
                                <!--                                <span>12文件夹</span>·<span>12文件</span>-->
                                <!--                            </p>-->
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

                <dl-spin *ngIf="_isLoadingMore"></dl-spin>
                @if (!_isLoadingMore && !pageQueryParams.totalCount) {
                    <nz-empty style="margin: 16px 0"></nz-empty>
                }
            </div>
        </dl-spin-mask>

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
            overflow-y: hidden;

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

                    &:hover {
                        > i {
                            color: var(--cs-primary-color-disabled);
                        }
                    }

                    > i {
                        border-radius: 4px;
                        background: var(--cs-primary-color);
                        color: var(--cs-bg-color);
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
        DlEntityIconComponent,
        DlSpinMaskComponent
    ],
    standalone: true
})
export class MixEntityTableComponent {
    @Input()
    colItem2Template: TemplateRef<IEntity>;

    @Input()
    colItem3Template: TemplateRef<IEntity>;

    @Input({ required: true })
    requester: (p: any) => Promise<IPagingListResponse<IEntity>>;

    @Input()
    headItems: IFileTableHeadItem[] = FileTableHeader;

    @Input()
    queryParams: IFilesQueryParams & Record<string, any> = {};

    @Input()
    multiSelect = true;

    @Input()
    contextMenus: IDlContextMenuItem[] = [];

    @Input()
    isLoading = false;

    @Input()
    @HostBinding('class.single-mode')
    singleMode = false;

    @Input()
    set opButtonGroup(val: IEntityTableOpButtonItem[]) {
        this.opButtons = [FileOpButton_clearChecked, ...val];
    }

    @Output()
    onOpButtonClick = new EventEmitter<IEntityTableOpButtonItem>();

    @Output()
    onCheckedChange = new EventEmitter<IEntity[]>();

    @Output()
    onContextMenuClick = new EventEmitter<{
        menu: IDlContextMenuItem,
        entity: IEntity
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

    itemList: IEntity[] = [];

    checkedList = new Set<string>();
    protected _checkedFileList = new Set<IEntity>();

    protected opButtons: IEntityTableOpButtonItem[] = [];

    isShowBtnGroup = false;
    protected _isLoadingMore = false;

    constructor(
        public readonly hostEl: ElementRef,
        public readonly render: CsesRenderService,
        private overlayService: DocsOverlayService,
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
        if (this._isLoadingMore) return;
        this._isLoadingMore = true;
        const params = { ...this._params, ...this.queryParams, ...this.pageQueryParams };
        return this.requester(params).then(data => {
            this.itemList = this.itemList.concat(data.items || []);
            this.pageQueryParams.totalCount = data.totalCount;
        }).catch(e => {
            this.render.error(e.error)
            throw e
        }).finally(() => {
            this._isLoadingMore = false;
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
        if (this._isLoadingMore || this.itemList.length >= this.pageQueryParams.totalCount) return;
        this.pageQueryParams.pageNumber++;
        this.requestData().then(e => {
            this.pageQueryParams.pageNumber--;
        })
    }

    getCheckedIds() {
        return Array.from(this.checkedList);
    }

    getCheckedItems() {
        return Array.from(this._checkedFileList);
    }

    onCheck(item: IEntity) {
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
        if (!this.multiSelect) return;
        const docs = Array.from(this._checkedFileList);
        // // 权限降级
        // let role = PermissionRole.owner;
        // for (let i = 0; i < docs.length; i++) {
        //     if (!docs[i].localUser?.role) {
        //         docs[i].localUser ??= { role: PermissionRole.reader };
        //         docs[i].localUser.role = await this.permissionService.getEntityLocalUserRole(docs[i].id);
        //     }
        //     if (docs[i].localUser.role < role) {
        //         role = docs[i].localUser.role;
        //     }
        // }
        //
        // const _ops_roles = {
        //     [FileOpButton_deleteDocs.value]: PermissionRole.owner,
        //     [FileOpButton_moveDocs.value]: PermissionRole.owner
        // };
        // this.opButtons = [FileOpButton_clearChecked].concat([FileOpButton_moveDocs, FileOpButton_deleteDocs].filter(item => role >= _ops_roles[item.value]));
        this.onCheckedChange.emit(docs);
    }

    removeItems(items: string[]) {
        const ids = new Set(items);
        this.itemList = this.itemList.filter(item => !ids.has(item.id));
        this.checkedList = new Set(Array.from(this.checkedList).filter(id => !ids.has(id)));
        this._checkedFileList = new Set(Array.from(this._checkedFileList).filter(item => !ids.has(item.id)));
        if (this.checkedList.size === 0) {
            this.isShowBtnGroup = false;
        }
    }

    onBtnClick(item: IEntityTableOpButtonItem) {
        if (item.value === 'clearChecked') {
            this.clearChecked();
        } else {
            this.onOpButtonClick.emit(item);
        }
    }

    async onContextMenuToggle(evt: MouseEvent, doc: IEntity) {
        evt.preventDefault();
        evt.stopPropagation();

        if (!this.contextMenus?.length) return;

        const target = evt.target as HTMLElement;
        target.classList.add('active');

        const { close, cpr } = this.overlayService.showContextMenu({
            target,
            menus: this.contextMenus
        });
        this.destroyRef.onDestroy(() => {
            close();
        });
        cpr.instance.onMenuClick.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe((menu: IOpMenuItem) => {
            close();
            this.onContextMenuClick.emit({
                entity: doc,
                menu
            });
        });
        cpr.instance.onClose.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(() => {
            target.classList.remove('active');
        });
    }

}
