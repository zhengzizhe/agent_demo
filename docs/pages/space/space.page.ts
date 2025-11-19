import { Component, DestroyRef } from '@angular/core';
import {
    AsideMenuComponent, DlBackBtnComponent, DlButton,
    DlMenuButtonComponent,
    DlTabComponent,
    IMenu,
} from '../../components';
import { SPACE_ROUTES } from './const';
import {
    EntityService,
    ISpaceDetail,
    PAGE_ROUTE_PREFIX,
    PermissionRole,
    RouterService,
    UserService
} from '../../services';
import { ActivatedRoute, ActivationEnd, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FolderTreeComponent } from '../../components/folder-tree/folder-tree';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AsyncPipe } from '@angular/common';

@Component({
    selector: 'space-page.dl-page',
    templateUrl: './space.page.html',
    styleUrls: ['./space.page.scss'],
    imports: [
        DlButton,
        DlTabComponent,
        AsideMenuComponent,
        DlMenuButtonComponent,
        RouterLinkActive,
        RouterLink,
        RouterOutlet,
        FolderTreeComponent,
        AsyncPipe,
        DlBackBtnComponent
    ],
    standalone: true
})
export class SpacePage {

    protected pageInfo = {
        title: '空间',
        slogan: ''
    };

    spaceId = '';
    spaceDetail!: ISpaceDetail;
    spaceRoutes: IMenu[] = [];

    activeFolderId = '';

    constructor(
        private userService: UserService,
        private entityService: EntityService,
        protected routerService: RouterService,
        private activeRoute: ActivatedRoute,
        private readonly destroyRef: DestroyRef
    ) {
    }

    ngOnInit() {
        this.routerService.ngRouter.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(evt => {
            if (evt instanceof ActivationEnd) {
                const snapshot = evt.snapshot;
                if (!snapshot.children.length) {
                    if (snapshot.data['id'] === 'folder') {
                        this.activeFolderId = snapshot.params.id;
                    } else {
                        this.activeFolderId = '';
                    }
                }
            }
        });
        this.activeRoute.params.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
            const id = params['id'];
            if (id) {
                this.initData(id);
            }
        });

        this.spaceRoutes = SPACE_ROUTES.map(r => {
            return {
                ...r,
                route: PAGE_ROUTE_PREFIX + (r.route ? `/space/${this.spaceId}/${r.route}` : `/space/${this.spaceId}`)
            };
        });
    }

    initData(id: string) {
        this.spaceId = id;
        this.routerService.deleteSharedData('spaceDetail');
        this.entityService.getSpaceDetail(id).then((res) => {
            this.spaceDetail = res;
            this.pageInfo.title = res.name;
            this.pageInfo.slogan = res.spaceType === 'personal' ? this.userService.ctx.userInfo.name + '的个人空间' : '';
            this.routerService.setCurrentTabTitle(this.spaceDetail.name);

            this.routerService.setSharedData('spaceDetail', this.spaceDetail);
        });
    }

    goBack() {
        this.routerService.navigate([]);
        // this.routerService.back()
    }

    ngOnDestroy() {
        this.routerService.deleteSharedData('spaceDetail');
    }

    onNewFolder() {
        this.entityService.createFolder(this.spaceId, this.spaceId).then(res => {
            this.entityService.entitiesUpdate$.next({
                type: 'new',
                parentId: this.spaceId,
                entities: [res]
            })
        })
    }

    protected readonly PermissionRole = PermissionRole;
}
