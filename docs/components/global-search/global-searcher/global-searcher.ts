import { Component, ViewChild } from '@angular/core';
import {
    DOC_LINK_HOST, EntityAccessCode,
    EntityService, EntityType,
    IBasePageQueryParams, IPagingListResponse, isDocVersionLink, isEntityLink, parseDocVersionLink,
    parseEntityLink,
    RouterService
} from '@/pages/docs/services';
import { Subject } from 'rxjs';
import { debounce } from '@ccc/blockcraft';
import { GlobalSearchDialogComponent, ISearchListItem } from '@/pages/docs/components';
import { FormsModule } from '@angular/forms';
import { CdkConnectedOverlay } from '@angular/cdk/overlay';
import { CsesRenderService } from '@ccc/cses-common';

@Component({
    selector: 'dl-global-searcher',
    templateUrl: 'global-searcher.html',
    styleUrls: ['global-searcher.scss'],
    imports: [
        FormsModule,
        CdkConnectedOverlay,
        GlobalSearchDialogComponent
    ],
    standalone: true
})
export class DlGlobalSearcherComponent {
    showSearchDialog: boolean = false;
    searchText: string = '';
    searchResult: ISearchListItem[] = [];
    isDocLink = false;

    searchResultLoading: boolean = false;
    searchPageInfo: IBasePageQueryParams = {
        pageNumber: 1,
        pageSize: 20,
        totalCount: 21
    };
    searchCancel$ = new Subject<void>();

    @ViewChild('searchDialogComponent')
    searchDialogComponent: GlobalSearchDialogComponent;

    constructor(
        private routerService: RouterService,
        private entityService: EntityService,
        private render: CsesRenderService
    ) {
    }

    ngOnDestroy() {
        this.showSearchDialog = false;
    }

    onSearchTextChange = debounce((text: string) => {
        if (this.searchResultLoading) this.searchCancel$.next();
        if (this.searchText === text.trim()) return;
        this.resetSearch();
        this.searchText = text.trim();
        this.isDocLink = isEntityLink(text) || isDocVersionLink(text);
        this.getSearchResult();
    }, 0);

    onSubmit() {
        if (this.isDocLink) this.navByLink(this.searchText).catch(e => {
            if (e.error?.code !== EntityAccessCode.NOT_FOUND) return;
            this.render.error(e.error?.desc || '文件不存在或已删除')
        })
        else this.searchDialogComponent.selectItem(this.searchResult[this.searchDialogComponent.activeIndex]);
    }

    navByLink(text: string) {
        if (isDocVersionLink(text)) {
            const link = parseDocVersionLink(text);
            return this.routerService.navigateToNewTab(link)
        }
        if (isEntityLink(text)) {
            const link = parseEntityLink(text);
            return this.routerService.navigateToNewTab(link)
        }
    }

    async getSearchResult() {
        if (this.searchResultLoading) {
            this.searchCancel$.next();
        }

        this.searchResultLoading = true;

        try {
            let res: IPagingListResponse;
            if (this.searchText) {
                res = await this.entityService.globalKeywordSearch({
                    keyword: this.searchText,
                    ...this.searchPageInfo
                }, this.searchCancel$);
            } else {
                res = await this.entityService.getHomeDocView({
                    timeSort: 'openedAt',
                    ...this.searchPageInfo
                }, this.searchCancel$);
            }
            this.searchPageInfo.totalCount = res.totalCount;
            this.searchResult.push(...res.items.map(v => {
                v.highlight ??= v.name.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                return v;
            }));
            this.searchDialogComponent.cdr.markForCheck();
        } catch (e) {

        } finally {
            this.searchResultLoading = false;
        }

    }

    resetSearch() {
        this.searchResult = [];
        this.searchPageInfo = {
            pageNumber: 1,
            pageSize: 20,
            totalCount: 21
        };
    }

    onLoadMore() {
        if (this.searchPageInfo.pageNumber * this.searchPageInfo.pageSize >= this.searchPageInfo.totalCount) {
            return;
        }
        this.searchPageInfo.pageNumber++;
        this.getSearchResult();
    }

    onFocus() {
        this.getSearchResult().then(() => {
            setTimeout(() => {
                this.showSearchDialog = true;
            }, 200);
        });
    }

    onBlur() {
        this.showSearchDialog = false;
        this.resetSearch();
    }

    async onSearchItemClick(evt: ISearchListItem) {
        if (!this.entityService.spaceList?.length) {
            await this.entityService.getSpaceList();
        }

        const isInSelfSpace = this.entityService.isSelfSpace(evt.spaceId);
        switch (evt.nodeType) {
            case EntityType.Folder:
                this.routerService.navigateToNewTab(
                    isInSelfSpace ? `space/${evt.spaceId}/folder/${evt.id}` : `folder/${evt.id}`
                ).catch(e => {
                    if (e.error?.code !== EntityAccessCode.NOT_FOUND) return;
                    this.render.error(e.error?.desc || '文件不存在');
                });
                break;
            case EntityType.Document:
            default:
                this.routerService.navigateToDoc(
                    evt.id, evt.name, {
                        page: isInSelfSpace ? 'space' : 'share',
                        spaceId: evt.spaceId
                    }
                ).catch(e => {
                    if (e.error?.code !== EntityAccessCode.NOT_FOUND) return;
                    this.render.error(e.error?.desc || '文件不存在');
                });
                break;
        }
    }
}
