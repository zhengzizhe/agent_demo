import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterService } from '../../services';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { MatIcon } from '@angular/material/icon';
import { DlBackBtnComponent } from '@/pages/docs/components';

export interface INavItem {
    collapse?: boolean;
    origin: {
        id: string
        name: string
        icon?: string
        route: string
    };
}

@Component({
    selector: 'dl-breadcrumb',
    template: `
        <dl-back-btn (click)="navBack()" />
        <!--     <span class="btn-advance bc_icon bc_fanhui"></span>-->

        @for (item of navs; track item.origin.id) {
            <nav>
                @if (item.origin.icon) {
                    <i [class]="['bc_icon', item.origin.icon]"></i>
                }
                @if (item.collapse) {
                    <span class="nav-name" nz-dropdown nzTrigger="click" [nzDropdownMenu]="menu">
            ...
          </span>
                } @else {
                    <span class="nav-name" (click)="onNavClick(item.origin.route)">{{ item.origin.name }}</span>
                }
                <!--        <span class="dropdown-btn">-->
                <!--          <i class="bc_icon bc_xiajaintou"></i>-->
                <!--        </span>-->
            </nav>
            <span class="divider">/</span>
        }

        <nz-dropdown-menu #menu='nzDropdownMenu'>
            <div class="dl-dropdown-menu">
                @for (item of hiddenNavs; track item.origin.id) {
                    <div class="dl-dropdown-menu-item" style="min-width: 200px; max-width: 400px"
                         [style.margin-left.px]="14 * $index"
                         (click)="onNavClick(item.origin.route)">
                        <i class="bc_icon bc_folder-open icon-folder"></i>
                        <span class="dl-dropdown-menu-item__item">{{ item.origin.name }}</span>
                    </div>
                }
            </div>
        </nz-dropdown-menu>
    `,
    styleUrl: './breadcrumb.scss',
    imports: [
        NzDropDownDirective,
        NzDropdownMenuComponent,
        MatIcon,
        DlBackBtnComponent
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlBreadcrumbComponent {

    private _paths: INavItem['origin'][] = [];

    @Input({ required: true })
    set paths(val: INavItem['origin'][]) {
        if (!val?.length) {
            return;
        }

        this._paths = val;
        if (val.length <= 3) {
            this.navs = val.map(v => ({ origin: v }));
        } else {
            this.navs = [
                { origin: val[0] },
                { origin: val[1], collapse: true },
                { origin: val[val.length - 2] },
                { origin: val[val.length - 1] }
            ];
            this.hiddenNavs = val.slice(1, val.length - 2).map(
                v => ({ origin: v })
            );
        }
    }

    get paths() {
        return this._paths;
    }

    navs: INavItem[] = [];
    hiddenNavs: INavItem[] = [];

    constructor(
        private router: RouterService
    ) {
    }

    onNavClick(route: string) {
        this.router.navigateByUri(route);
    }

    navBack() {
        const prevIndex = Math.max(this.navs.length - 2, 0);
        this.router.navigateByUri(this.navs[prevIndex].origin.route);
    }
}
