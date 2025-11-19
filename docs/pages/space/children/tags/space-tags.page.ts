import { Component, DestroyRef, ViewChild } from '@angular/core';
import {
    DlButton, DlTabComponent, DlTagComponent,
    EntityTableComponent, fileTableHead_documentType, fileTableHead_owner, fileTableHead_timeSort,
    FolderCardList, IDlTabItem, IFileTableHeadItem, ListMode,
    PageBtnGroupComponent
} from '@/pages/docs/components';
import { NgIf } from '@angular/common';
import {
    DocsPermissionService,
    EntityService, IDocumentEntity, IEntity,
    ISpaceDetail, ITag, OpTypesStr,
    PermissionRole,
    RouterService,
    UserService
} from '@/pages/docs/services';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SpaceSettingDialog } from '@/pages/docs/pages/space/children/components/space-setting.dialog';
import { filter, take } from 'rxjs';
import { CdkConnectedOverlay, CdkOverlayOrigin } from '@angular/cdk/overlay';
import { DlTagsSelectorDialog } from '@/pages/docs/pages/space/children/tags/tags-selector';
import { DlTagRowComponent } from '@/pages/docs/components/tag-row/tag-row';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';

const fileTableHead_tags: IFileTableHeadItem = {
    label: '标签',
    type: 'none',
    value: 'tags'
};

@Component({
    selector: 'space-tags.dl-page-main',
    templateUrl: './space-tags.page.html',
    styleUrls: ['./space-tags.page.scss'],
    imports: [
        EntityTableComponent,
        FolderCardList,
        NgIf,
        PageBtnGroupComponent,
        DlButton,
        DlTabComponent,
        DlTagComponent,
        CdkConnectedOverlay,
        CdkOverlayOrigin,
        DlTagsSelectorDialog,
        DlTagRowComponent,
        NzDropdownMenuComponent,
        NzDropDownDirective
    ],
    standalone: true
})
export class SpaceTagsPage {

    spaceId: string = '';
    spaceDetail: ISpaceDetail;

    tabItems: IDlTabItem[] = [
        {
            id: 'all',
            label: '所有类型'
        },
        {
            id: 'file',
            label: '文档'
        }
    ];
    activeFilterTab = 'all';
    listMode: ListMode = 'list';

    @ViewChild('folderCardList') folderCardList: FolderCardList;
    @ViewChild('fileTable') fileTable: EntityTableComponent;

    tagFolderRequester = this.entityService.getTagFolderList.bind(this.entityService);
    tagFileRequester = this.entityService.getTagDocList.bind(this.entityService);
    folderListQueryParams = {};
    fileTableQueryParams = {};

    showManageTagsBtn = false;

    tagsFileTableHead = [
        fileTableHead_documentType,
        fileTableHead_tags,
        fileTableHead_timeSort
    ];

    isShowTagFilter = false;
    selectedTags: ITag[] = [];
    selectedTagsIdList: string[] = [];

    constructor(
        private permissionService: DocsPermissionService,
        private userService: UserService,
        private entityService: EntityService,
        private routerService: RouterService,
        private activeRoute: ActivatedRoute,
        private readonly destroyRef: DestroyRef
    ) {
    }

    ngOnInit() {
        this.activeRoute.parent.params.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
            this.spaceId = '';

            const id = params['id'];
            if (id) {
                this.initData(id);
            }
        });
    }

    initData(id: string) {
        this.folderListQueryParams = { id };
        this.fileTableQueryParams = { id };
        this.spaceId = id;

        this.routerService.sharedDataChange$.pipe(filter(v => v.spaceDetail && v.spaceDetail.id === id), takeUntilDestroyed(this.destroyRef))
            .subscribe((data) => {
                const spaceDetail = data.spaceDetail;
                if (spaceDetail.id === this.spaceId) {
                    this.spaceDetail = spaceDetail;
                    this.showManageTagsBtn = this.spaceDetail.localUser.role >= PermissionRole.manager;
                }
            });
    }

    onRefresh() {
        this.folderCardList?.refresh();
        this.fileTable?.refresh();
    }

    onManageTags() {
        const { cpr } = this.entityService.overlayService.showDialog(SpaceSettingDialog, {});
        cpr.instance.spaceDetail = this.spaceDetail;
        cpr.instance.activeTab = 'tags';
    }

    onFilterTabChange(evt: IDlTabItem) {
        this.activeFilterTab = evt.id;
    }

    onNavToDoc(evt: IDocumentEntity) {
        if (this.entityService.isSelfSpace(this.spaceId)) {
            this.routerService.navigateToDoc(evt.id, evt.name, { page: 'space', spaceId: this.spaceId });
        }
    }

    onFileTableItemsChange($event: { items: IEntity[]; type: OpTypesStr }) {

    }

    onDeleteSelectedTag(tag: ITag) {
        this.selectedTags = this.selectedTags.filter(t => t.id !== tag.id);
        this.onTagsSelectedChange(this.selectedTags);
    }

    clearSelectedTags() {
        this.selectedTags = [];
        this.onTagsSelectedChange(this.selectedTags);
    }

    onTagsSelectedChange(evt: ITag[]) {
        this.isShowTagFilter = false;
        this.selectedTags = evt;
        // this.selectedTagsIdList = evt.map(v => v['path'].join(':'));
        this.selectedTagsIdList = evt.map(v => v.id);
        this.folderListQueryParams = { ...this.folderListQueryParams, tags: this.selectedTagsIdList };
        this.fileTableQueryParams = { ...this.fileTableQueryParams, tags: this.selectedTagsIdList };
        requestAnimationFrame(() => {
            this.onRefresh();
        });
    }
}
