import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component, DestroyRef, ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    Output
} from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {NzEmptyComponent} from "ng-zorro-antd/empty";
import { EntityService, EntityType, IDlUser } from '@/pages/docs/services';
import { scrollBottomListenDirective } from '@/pages/docs/directives';
import { Date2Pipe } from '@/pages/docs/pipes';

export interface ISearchListItem {
    id: string;
    name: string;
    nodeType?: EntityType
    owner?: IDlUser
    updater?: IDlUser
    spaceId?: string
    spaceName?: string
    openedAt?: number
    updatedAt?: number
    block?: {
        id: string
        text: string
    },
    highlight: string
}

@Component({
    selector: 'dl-global-search-dialog',
    templateUrl: './global-search-dialog.html',
    styleUrls: ['./global-search-dialog.scss'],
    imports: [
        NgForOf,
        MatIcon,
        NzEmptyComponent,
        NgIf,
        scrollBottomListenDirective,
        Date2Pipe
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GlobalSearchDialogComponent {
    protected _searchResult: ISearchListItem[] = [];
    @Input({required: true})
    set searchResult(val: ISearchListItem[]) {
        this._searchResult = val
        this.setActiveIndex(0);
    }
    get searchResult() {
        return this._searchResult;
    }

    @HostBinding('style.width.px')
    @Input()
    width: number = 542;

    @Output() itemClick = new EventEmitter<ISearchListItem>();

    @Output() scrolledBottom = new EventEmitter<void>();

    constructor(
        public readonly host: ElementRef,
        public readonly cdr: ChangeDetectorRef,
        public readonly destroyRef: DestroyRef
    ) {
    }

    activeIndex = 0;

    trackByFn(index: number, item: any) {
        return item.id;
    }

    selectUp() {
        this.setActiveIndex(this.activeIndex - 1)
    }

    selectDown() {
        this.setActiveIndex(this.activeIndex + 1)
    }

    setActiveIndex(index: number) {
        if (index < 0) {
            this.activeIndex = this.searchResult.length - 1;
        } else if (index >= this.searchResult.length) {
            this.activeIndex = 0;
        } else {
            this.activeIndex = index;
        }
        this.cdr.markForCheck();
    }

    selectItem(item: ISearchListItem) {
        this.itemClick.emit(item);
    }

    protected readonly EntityType = EntityType;
}
