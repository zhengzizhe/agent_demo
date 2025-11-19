import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { EntityService, EntityType, EntityTypeName, IDocDetail, RouterService } from '@/pages/docs/services';
import { MatIcon } from '@angular/material/icon';
import { NgIf } from '@angular/common';
import { FavoriteIcon } from '@/pages/docs/components';
import { DlTagRowComponent } from '@/pages/docs/components/tag-row/tag-row';

interface IPath {
    id: string;
    name: string;
    svgIcon?: string;
    route?: string;
}

@Component({
    selector: 'doc-op-menu',
    templateUrl: 'doc-op-menu.html',
    styleUrls: ['doc-op-menu.scss'],
    imports: [
        MatIcon,
        NgIf,
        FavoriteIcon,
        DlTagRowComponent
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocOpMenuComponent {
    @Input({ required: true })
    docDetail: IDocDetail;

    private _spaceId = '';
    @Input({ required: true })
    set spaceId(val: string) {
        if (val && this._spaceId !== val) {
            this._spaceId = val;
            this.initPath();
        }
    }

    get spaceId() {
        return this._spaceId;
    }

    @Output()
    close = new EventEmitter<void>();

    paths: IPath[] = [];

    constructor(
        private readonly entityService: EntityService,
        private router: RouterService,
        private cdr: ChangeDetectorRef
    ) {
    }

    ngOnInit() {
    }

    async initPath() {
        const cur = {
            id: this.docDetail.id,
            name: this.docDetail.name || '未命名文档',
            svgIcon: 'bc_wendang0102',
            route: null
        };
        if (!this.entityService.spaceList.length) {
            await this.entityService.getSpaceList();
        }
        if (this.entityService.isSelfSpace(this.spaceId)) {
            this.entityService.getEntityPath(this.docDetail.id, this.spaceId).then((res) => {
                this.paths = res.items.slice(0, -1).map(item => {
                    return {
                        id: item.id,
                        name: item.name || ('未命名' + EntityTypeName[item.nodeType || EntityType.Document]),
                        svgIcon: item.nodeType === EntityType.Space ? 'bc_kongjian' : (item.nodeType === EntityType.Folder ? 'bc_folder-open' : 'bc_wendang0102'),
                        route: item.nodeType === EntityType.Space ? `space/${item.id}` : (item.nodeType === EntityType.Folder ? `space/${this.spaceId}/folder/${item.id}` : null)
                    };
                }).concat([cur]);
                this.cdr.markForCheck();
            });
        } else {
            this.paths = [
                {
                    id: 'share-page',
                    name: '共享',
                    route: 'share'
                },
                cur
            ];
            this.cdr.markForCheck();
        }
    }

    onNavClick(item: IPath) {
        if (typeof item.route === 'string') {
            this.close.emit()
            setTimeout(() => {
                const curTab = this.router.layoutService.currentTab
                this.router.navigateToNewTab(item.route).then(() => {
                    this.router.layoutService.closeTab(curTab)
                })
            }, 100)
        }
    }

    onFavorite() {
        if (!this.docDetail) return;
        const value = !this.docDetail.localUser.behavior.favorite;
        this.entityService.setFavoriteEntity(this.docDetail.id, value).then(res => {
            this.docDetail.localUser.behavior.favorite = value;
            this.cdr.markForCheck()
            value ? this.entityService.render.success('收藏成功') : this.entityService.render.info('取消收藏');
        });
    }
}
