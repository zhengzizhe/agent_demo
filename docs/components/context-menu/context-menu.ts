import { Component, DestroyRef, ElementRef, EventEmitter, Input, Output } from '@angular/core';
import { IDlContextMenuItem } from '@/pages/docs/services';
import { NgIf } from '@angular/common';

@Component({
    selector: 'dl-context-menu',
    templateUrl: './context-menu.html',
    styleUrls: ['./context-menu.scss'],
    standalone: true,
    imports: [
        NgIf
    ],
    host: {
        '[class]': '["dl-dropdown-menu", "dl-ani-dropdown"]'
    }
})
export class DlContextMenuComponent {
    @Input({ required: true })
    menus: IDlContextMenuItem[] = [];

    @Output()
    onMenuClick = new EventEmitter<IDlContextMenuItem>();

    @Output()
    onClose = new EventEmitter<void>();

    constructor(
        readonly hostEl: ElementRef<HTMLElement>,
        readonly destroyRef: DestroyRef
    ) {
    }

    protected onItemClick(item: IDlContextMenuItem) {
        this.onMenuClick.emit(item);
    }

    ngOnDestroy() {
        this.onClose.emit();
    }
}
