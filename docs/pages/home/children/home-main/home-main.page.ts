import { Component, ViewChild } from '@angular/core';
import { FileOpButton_removeRecent, HomeFileTabs } from './const';
import {
    DocBehaviors,
    EntityAccessCode,
    EntityService,
    EntityType,
    EntityTypeName,
    IBasePageQueryParams,
    IDocumentEntity,
    IEntity,
    IFolderEntity, OpTypes,
    OpTypesStr,
    RouterService
} from '@/pages/docs/services';
import {
    DlButton,
    DlConfirmDialogComponent,
    DlGlobalSearcherComponent,
    DlTabComponent,
    EntityTableComponent,
    fileTableHead_timeSort,
    FolderCardList,
    IDlTabItem,
    ListMode,
    PageBtnGroupComponent
} from '@/pages/docs/components';
import {
    fileTableHead_documentType,
    fileTableHead_owner,
    fileTableHead_user_ops
} from '@/pages/docs/components/file-table';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';

@Component({
    selector: 'home-main.dl-page-main',
    templateUrl: './home-main.page.html',
    styleUrls: ['./home-main.page.scss'],
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
export class HomeMainPage {
    protected readonly tabs = HomeFileTabs;

    listMode: ListMode = 'list';

    @ViewChild('fileTable', { read: EntityTableComponent }) fileTable!: EntityTableComponent;
    @ViewChild('folderCardList', { read: FolderCardList }) folderCardList!: FolderCardList;

    recentFileTableHead = [
        fileTableHead_documentType,
        fileTableHead_owner,
        fileTableHead_user_ops
    ];
    recentFileTableOpButtons = [FileOpButton_removeRecent];

    recentRequester = this.entityService.getHomeDocView.bind(this.entityService);
    favoriteFoldersRequester = (params: IBasePageQueryParams) => {
        return this.entityService.getBehaviorFolderList({
            ...params,
            behaviorType: DocBehaviors.favorite
        });
    };
    favoriteDocsRequester = (params: IBasePageQueryParams) => {
        return this.entityService.getBehaviorDocList({
            ...params,
            behaviorType: DocBehaviors.favorite
        });
    };

    favoriteFileTableHead = [
        fileTableHead_documentType,
        fileTableHead_owner,
        fileTableHead_timeSort
    ];
    favoriteFolderListQueryParams = { owner: null };
    favoriteListDisableOps = [OpTypes.delete]

    activeTab = HomeFileTabs[0];

    constructor(
        private entityService: EntityService,
        private routerService: RouterService
    ) {
    }

    onRefresh() {
        this.fileTable?.refresh();
        this.folderCardList?.refresh();
    }

    onNavToDoc(evt: IDocumentEntity) {
        this.routerService.navigateToDoc(evt.id, evt.name).catch(e => {
            if (e.error?.code !== EntityAccessCode.NOT_FOUND) return;
            this.confirmRemoveFrom(evt, EntityType.Document, () => {
                if (this.activeTab.id === 'recent') {
                    return this.entityService.removeFromHomeView([evt.id]).then(res => {
                        this.fileTable?.removeItems([evt]);
                        this.entityService.render.success('已从最近列表移除');
                    });
                } else if (this.activeTab.id === 'favorite') {
                    return this.entityService.setFavoriteEntity(evt.id, false).then(res => {
                        this.entityService.render.success('已取消收藏');
                        this.fileTable?.removeItems([evt]);
                    });
                }
            });
        });
    }

    onTabChange(evt: IDlTabItem) {
        this.activeTab = evt;
    }

    onNavToFolder = (evt: IFolderEntity) => {
        const uri = this.entityService.isSelfSpace(evt.spaceId) ? `space/${evt.spaceId}/folder/${evt.id}` : `folder/${evt.id}?from=share&start=${evt.id}`;
        this.routerService.navigateByUri(uri).catch(e => {
            if (e.error?.code !== EntityAccessCode.NOT_FOUND) return;
            this.confirmRemoveFrom(evt, EntityType.Folder, () => {
                if (this.activeTab.id !== 'favorite') return Promise.reject();
                return this.entityService.setFavoriteEntity(evt.id, false).then(res => {
                    this.entityService.render.success('已取消收藏');
                    this.folderCardList.removeItems([evt.id]);
                });
            });
        });
    };

    onFavoriteTableChange(evt: { items: IEntity[]; type: OpTypesStr }) {
        if(evt.type === OpTypes.cancelFavorite) {
            this.fileTable.removeItems(evt.items);
        }
    }

    onFavoriteFoldersChange(evt: { items: IEntity[]; type: OpTypesStr }) {
        if(evt.type === OpTypes.cancelFavorite) {
            this.folderCardList.removeItems(evt.items.map(v => v.id));
        }
    }

    confirmRemoveFrom(item: IEntity, nodeType: EntityType, onRemove: () => Promise<any>) {
        const { cpr, overlayRef } = this.entityService.overlayService.showDialog(DlConfirmDialogComponent, {
            hasBackdrop: true,
            transparentBackdrop: true
        });
        cpr.setInput('title', `${EntityTypeName[nodeType]}已删除`);
        cpr.setInput('message', this.activeTab.id === 'recent' ? `该${EntityTypeName[nodeType]}已被删除，是否从最近列表移除？` : `该${EntityTypeName[nodeType]}已被删除，是否取消收藏？`);
        cpr.setInput('buttons', [{
            id: 'remove',
            text: this.activeTab.id === 'recent' ? '移除' : '取消收藏',
            type: 'danger'
        }]);
        cpr.instance.onButtonClick.pipe(filter(btn => btn.id === 'remove'), takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(() => {
            cpr.setInput('isLoading', true);
            onRemove().then(() => {
                overlayRef?.dispose();
            }).catch(() => {
                cpr.setInput('isLoading', false);
            });
        });
    }


}
