import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { DlButton } from '../button';
import { MatIcon } from '@angular/material/icon';
import { EntityService, EntityType, IEntity, RouterService } from '../../services';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';

export type ListMode = 'list' | 'list-simple'

interface Item {
    icon: string;
    value: ListMode;
    title: string;
}

@Component({
    selector: 'dl-page-btn-group',
    template: `
        @if (useCreateBtn) {
            <button dl-button (click)="onCreateDoc.emit()">
                <mat-icon svgIcon="bc_xinjianwenjian" style="width: 18px"></mat-icon>
                文档
            </button>

            <button dl-button (click)="onCreateFolder.emit()">
                <i class="bc_icon bc_folder-plus" style="font-size: 18px; color: #5DA1BA"></i>
                文件夹
            </button>

            <span class="divider"></span>
        }

        @for (item of modes; track item.value) {
            <i [class]="item.icon" [class.active]="listMode === item.value"
               nz-tooltip [nzTooltipTitle]="item.title"
               (click)="changeMode(item)"></i>
        }

        @if(useDropMenuBtn) {
            <span class="divider"></span>

            <i class="icon bc_icon bc_info-circle" [class.active]="isShowDropdownMenu"
               nz-dropdown nzTrigger="click"
               [nzDropdownMenu]="menu" nzPlacement="bottomRight" [(nzVisible)]="isShowDropdownMenu"
               (nzVisibleChange)="isShowDropdownMenuChange.emit($event)">
            </i>

            <nz-dropdown-menu #menu="nzDropdownMenu">
                <ng-content select=".dropdownMenu"></ng-content>
            </nz-dropdown-menu>
        }
    `,
    styleUrl: 'page-btn-group.scss',
    standalone: true,
    imports: [
        NzToolTipModule,
        DlButton,
        MatIcon,
        NzDropdownMenuComponent,
        NzDropDownDirective
    ]
})
export class PageBtnGroupComponent {
    @Input()
    listMode: ListMode = 'list';

    @Input()
    useCreateBtn: boolean = false;

    @Input()
    useDropMenuBtn: boolean = false;

    @Input()
    isShowDropdownMenu: boolean = false;

    @Output()
    listModeChange = new EventEmitter<string>();

    @Output()
    onCreateDoc = new EventEmitter<IEntity>();

    @Output()
    onCreateFolder = new EventEmitter<IEntity>();

    @Output()
    isShowDropdownMenuChange = new EventEmitter<boolean>();

    modes: Item[] = [
        {
            icon: 'bc_icon bc_liebiao',
            value: 'list',
            title: '普通列表'
        },
        {
            icon: 'bc_icon bc_jianhualiebiao',
            value: 'list-simple',
            title: '简化列表'
        }
    ];

    constructor(
    ) {
    }

    changeMode(item: Item) {
        if (item.value === this.listMode) return;
        this.listModeChange.emit(this.listMode = item.value);
    }


}
