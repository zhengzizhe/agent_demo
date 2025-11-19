import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface IDlTabItem {
    id: string;
    label: string;
    icon?: string;
    disable?: boolean;
    name?: string;
}

interface IDlShowTabItem extends IDlTabItem {
    _skipIndex?: number
}

@Component({
    selector: 'dl-tab',
    templateUrl: './dl-tab.html',
    styleUrls: ['./dl-tab.scss'],
    standalone: true
})
export class DlTabComponent {

    private _items: IDlTabItem[] = [];
    @Input({ required: true })
    set items(items: IDlTabItem[]) {
        this._items = items;
        if(this.overflow === 'hidden') {
            this.items_show = items.slice(0, 3).map(v => {
                return {
                    ...v,
                    _skipIndex: 0
                }
            })
        } else {
            this.items_show = items.map(v => {
                return {
                    ...v,
                    _skipIndex: 0
                }
            })
        }

    }

    get items(): IDlTabItem[] {
        return this._items;
    }

    @Input()
    set activeIndex(index: number) {
        this.active_tab_idx = index;
    }

    @Input()
    overflow: 'hidden' | 'visible' = 'hidden';

    @Input()
    tabItemWidth: number = 64;

    @Output()
    onTabChange = new EventEmitter<IDlTabItem>();

    @Output()
    activeIndexChange = new EventEmitter<number>();

    protected items_show: IDlShowTabItem[] = [];
    protected active_tab_idx: number = 0;

    onItemClick($index: number) {
        const item = this.items_show[$index];
        if (item.disable) {
            return;
        }
        this.active_tab_idx = $index;
        this.onTabChange.emit(this.items_show[this.active_tab_idx]);
        this.activeIndexChange.emit(this.active_tab_idx);
    }
}
