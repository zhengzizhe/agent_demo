import { Component, DestroyRef, ViewChild } from '@angular/core';
import {
    DocsPermissionService,
    EntityService, EntityType,
    IDocumentEntity, IEntity, IOpMenuItem,
    ISpaceDetail, OpTypesStr,
    PermissionRole,
    RouterService,
    UserService
} from '../../../../services';
import {
    DlButton, DlGlobalSearcherComponent,
    DlTabComponent,
    EntityTableComponent,
    FolderCardList,
    IDlTabItem,
    ListMode,
    PageBtnGroupComponent
} from '../../../../components';
import { ClassifyDocTabs } from '../../const';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIcon } from '@angular/material/icon';
import { NgIf } from '@angular/common';
import { filter } from 'rxjs';
import { SpaceSettingDialog } from '../components/space-setting.dialog';

@Component({
    selector: 'space-home-main.dl-page-main',
    templateUrl: 'space-home.page.html',
    styleUrls: ['space-home.page.scss'],
    providers: [],
    imports: [
        DlTabComponent,
        FolderCardList,
        EntityTableComponent,
        DlButton,
        MatIcon,
        PageBtnGroupComponent,
        NgIf,
        DlGlobalSearcherComponent
    ],
    standalone: true
})
export class SpaceHomePage {
    protected readonly tabs = ClassifyDocTabs;

    spaceId = '';
    spaceDetail: ISpaceDetail;
    listMode: ListMode = 'list';

    activeViewTab = this.tabs[0].id;

    @ViewChild('folderCardList') folderCardList!: FolderCardList;
    @ViewChild('fileTable') fileTable!: EntityTableComponent;

    folderListQueryParams = {};
    fileTableQueryParams = {
        id: this.spaceId
    };

    permissionMap = {
        createAble: false,
    };

    opsList = [];

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
        this.activeRoute.params.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
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
        this.permissionMap = {
            createAble: false,
        };

        this.routerService.sharedDataChange$.pipe(filter(v => v.spaceDetail && v.spaceDetail.id === id), takeUntilDestroyed(this.destroyRef))
            .subscribe((data) => {
                const spaceDetail = data.spaceDetail;
                if (spaceDetail.id === this.spaceId) {
                    this.spaceDetail = spaceDetail;
                    this.permissionMap.createAble = this.spaceDetail.localUser.role >= PermissionRole.manager;
                    this.opsList = [
                        {
                            id: 'setting',
                            label: '空间设置',
                            name: 'setting',
                            icon: 'bc_settings'
                        }
                    ];
                }
            });
    }

    onTabChange($event: IDlTabItem) {
        this.activeViewTab = $event.id;
    }

    onRefresh() {
        this.folderCardList.refresh();
        this.fileTable.refresh();
        this.routerService.refreshPage();
    }

    onNavToDoc(evt: IEntity) {
        this.routerService.navigateToDoc(evt.id, evt.name, {
            page: 'space',
            spaceId: this.spaceId
        });
    }

    onCreateDoc() {
        if (!this.spaceDetail) return;
        this.entityService.createDocument(this.spaceId).then(doc => {
            setTimeout(() => {
                this.onNavToDoc(doc);
            }, 500);
        });
    }

    onCreateFolder() {
        if (!this.spaceDetail) return;
        this.entityService.createFolder(this.spaceId, this.spaceId).then(folder => {
            this.entityService.entitiesUpdate$.next({
                type: 'new',
                parentId: this.spaceId,
                entities: [
                    { id: folder.id, name: folder.name, nodeType: EntityType.Folder }
                ]
            });

            setTimeout(() => {
                this.routerService.navigate(['space', this.spaceId, EntityType.Folder, folder.id]);
            }, 500);
        });
    }

    onFileTableItemsChange(evt: { items: IEntity[]; type: OpTypesStr }) {
        if (evt.type === 'move') {
            this.fileTable.removeItems(evt.items);
        }
    }

    onMenuClick(item: any) {
        switch (item.name) {
            case 'setting':
                this.openTagsTreeDialog();
                break;
        }
    }

    openTagsTreeDialog() {
        const { cpr } = this.entityService.overlayService.showDialog(SpaceSettingDialog, {});
        cpr.instance.spaceDetail = this.spaceDetail;
    }
}
