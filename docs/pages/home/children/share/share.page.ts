import { Component, ViewChild } from '@angular/core';
import {
    DlTabComponent,
    EntityTableComponent, FileOpButton_deleteDocs,
    fileTableHead_documentType,
    fileTableHead_timeSort,
    FolderCardList,
    IDlTabItem, IFileOpButtonItem,
    ListMode,
    PageBtnGroupComponent,
    IFileTableHeadItem, fileTableHead_owner_none, DlButton, DlGlobalSearcherComponent
} from '@/pages/docs/components';
import {
    DocsPermissionService, EntityAccessCode,
    EntityService, EntityType,
    IDocumentEntity,
    IFolderEntity,
    PermissionRole,
    RouterService,
    UserService
} from '@/pages/docs/services';
import { CsesRenderService } from '@ccc/cses-common';

@Component({
    selector: 'share-page.dl-page-main',
    templateUrl: './share.page.html',
    styleUrls: ['./share.page.scss'],
    imports: [
        DlTabComponent,
        PageBtnGroupComponent,
        EntityTableComponent,
        FolderCardList,
        DlButton,
        DlGlobalSearcherComponent
    ],
    standalone: true
})
export class SharePage {
    protected readonly tabs: IDlTabItem[] = [
        {
            id: 'toSelf',
            label: '共享给我'
        },
        {
            id: 'selfTo',
            label: '我共享的'
        }
    ];

    activeTabIndex = 0;

    listMode: ListMode = 'list';

    @ViewChild('fileTable') fileTable!: EntityTableComponent;
    @ViewChild('folderCardList') folderCardList!: FolderCardList;

    folderListQueryParams = { owner: null };
    folderListRequester = this.entityService.getSharingFolderList.bind(this.entityService);

    fileTableRequester = this.entityService.getSharingDocList.bind(this.entityService);
    fileTableQueryParams = { owner: null };
    fileTableHeadItems = [fileTableHead_documentType, fileTableHead_owner_none, fileTableHead_timeSort];

    constructor(
        protected readonly routerService: RouterService,
        protected readonly userService: UserService,
        protected readonly entityService: EntityService,
        readonly render: CsesRenderService,
        private readonly permissionService: DocsPermissionService
    ) {
    }

    onRefresh() {
        this.fileTable.refresh();
        this.folderCardList.refresh();
    }

    onNavToFolder = (evt: IFolderEntity) => {
        this.routerService.navigateByUri(
            this.entityService.isSelfSpace(evt.spaceId) ? `space/${evt.spaceId}/folder/${evt.id}` : `folder/${evt.id}?from=share&start=${evt.id}`
        ).catch(e => {
            if (e.error?.code !== EntityAccessCode.NOT_FOUND) return;
            this.render.error(e.error.desc || '文件不存在');
            this.onRefresh();
        })
    };

    onNavToDoc(evt: IDocumentEntity) {
        this.routerService.navigateToDoc(evt.id, evt.name, { page: 'share' }).catch(e => {
            if (e.error?.code !== EntityAccessCode.NOT_FOUND) return;
            this.render.error(e.error.desc || '文件不存在');
            this.onRefresh();
        });
    }

    onTabChange(evt: IDlTabItem) {
        this.folderListQueryParams.owner = evt.id === 'selfTo' ? this.userService.ctx.userInfo.userId : null;
        this.fileTableQueryParams.owner = evt.id === 'selfTo' ? this.userService.ctx.userInfo.userId : null;
        this.onRefresh();
    }


}
