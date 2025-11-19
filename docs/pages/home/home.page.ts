import { Component } from '@angular/core';
import {
    AsideMenuComponent,
    DlMenuButtonComponent,
    DlButton, DlTabComponent, PageBtnGroupComponent, DlToggleHead
} from '../../components';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { EntityService, ISpaceEntity, PAGE_ROUTE_PREFIX, RouterService } from '../../services';
import { AsyncPipe } from '@angular/common';
import { ExpandCollapseDirective } from '@/pages/docs/directives';

@Component({
    selector: 'home-main-page.dl-page',
    templateUrl: 'home.page.html',
    styleUrls: ['home.page.scss'],
    imports: [
        RouterLinkActive,
        RouterLink,
        AsideMenuComponent,
        DlMenuButtonComponent,
        DlButton,
        DlTabComponent,
        PageBtnGroupComponent,
        RouterOutlet,
        AsyncPipe,
        DlToggleHead,
        ExpandCollapseDirective,
    ],
    standalone: true
})
export class HomePage {
    protected readonly menuItems = [
        {
            title: '首页',
            route: PAGE_ROUTE_PREFIX,
            icon: 'bc_shouye'
        },
        {
            title: '共享',
            route: PAGE_ROUTE_PREFIX + '/share',
            icon: 'bc_gongxiang'
        }
    ];

    spaceList: ISpaceEntity[] = [];

    protected isShowSpaces = true;

    constructor(
        private entityService: EntityService,
        protected routerService: RouterService
    ) {
    }

    ngOnInit() {
        this.routerService.setCurrentTabTitle('文档库');
        this.getSpaceList();
    }

    navToSpace(space: ISpaceEntity) {
        this.routerService.navigate(
            ['space', space.id]
        );
    }

    getSpaceList() {
        this.entityService.getSpaceList().then(res => {
            this.spaceList = res;
        })
    }

    createDoc() {
        // const space = this.spaceList.find(space => space.spaceType === 'personal');
        // if (space) {
        //     this.entityService.createDocument(space.id);
        // }
    }

    onToggleExpandSpaces(e: boolean) {
        if(e) {
            this.getSpaceList();
        }
    }
}
