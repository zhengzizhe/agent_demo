import { Component, ElementRef, EventEmitter, Inject, Input, Output } from '@angular/core';
import { throttle } from '@ccc/blockcraft';
import { CheckBtn } from '../atoms';
import { IFilesQueryParams, IFileTableHeadItem, IFilterOption } from './types';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { NgForOf } from '@angular/common';
import { AppContext } from '@ccc/core-common';
import { CsesContextService } from '@ccc/cses-common';

@Component({
    selector: 'file-table-head',
    template: `
        <div class="file-table-list-item">
            <span class="btn-select"></span>
            <!--      <check-btn [checked]="_isCheckAll" (click)="onCheckAll.emit(_isCheckAll = !_isCheckAll)"></check-btn>-->

            @for (item of items; track item.label) {
                <div class="item">

                    @if (item.type === 'filter') {
                        <div class="item-label dl-btn-hover" [class.active]="item.active" [(nzVisible)]="item.active"
                             nz-dropdown [nzDropdownMenu]="menu" nzTrigger="click">
                            <span>{{ item.label }}</span>
                            <i class="bc_icon bc_xiajaintou icon-drop-down"></i>
                        </div>

                        <nz-dropdown-menu #menu="nzDropdownMenu">
                            <div class="dl-dropdown-menu">
                                <div class="dl-dropdown-menu-item" *ngFor="let option of item.filterOptions"
                                     (click)="onChangeFilter(item, option)"
                                     [class.active]="item.activeFilter === option.value">
                                    {{ option.label }}
                                </div>
                            </div>
                        </nz-dropdown-menu>
                    } @else {
                        <div class="item-label">
                            <span>{{ item.label }}</span>
                        </div>
                    }

                    <span class="resize-bar" (mousedown)="$event.preventDefault(); onResize($event, $index)"></span>
                </div>
            }

            <span style="min-width: 30px;"></span>
        </div>
    `,
    styleUrls: ['./file-table.scss'],
    styles: [`
        :host {
            display: block;

            .item-label {
                display: flex;
                align-items: center;
                gap: 4px;
                width: fit-content;
                padding: 0 4px;
                height: 28px;
            }

            .file-table-list-item {
                height: 40px;
                padding: 0;

                &:hover {
                    box-shadow: none;
                }

                .resize-bar {
                    display: none;
                }

                &:hover {
                    background: unset;

                    .resize-bar {
                        display: block;
                    }
                }
            }
        }
    `],
    imports: [
        CheckBtn,
        NzDropDownDirective,
        NzDropdownMenuComponent,
        NgForOf
    ],
    standalone: true
})
export class FileTableHeadComponent {
    @Input()
    params: IFilesQueryParams = {};

    @Input({ required: true })
    public items: IFileTableHeadItem[] = [];

    @Input()
    colSizes: number[] = [];

    @Output()
    paramsChange = new EventEmitter<IFilesQueryParams>();

    @Output()
    colSizesChange = new EventEmitter<number[]>();

    @Output()
    onCheckAll = new EventEmitter<boolean>();

    protected _isCheckAll = false;

    private _col_proportion = [.6, .2, .2];
    private _contentWidth = 1200;
    private resizeObs = new ResizeObserver(throttle((entries) => {
        const _w = entries[0].contentRect.width - 60;
        if (_w === this._contentWidth) return;
        this._contentWidth = _w;
        this.reCalcColSizes();
    }, 300));

    constructor(
        private el: ElementRef,
        @Inject(AppContext) public readonly ctx: CsesContextService,
    ) {
    }

    ngOnInit() {
    }

    ngAfterViewInit() {
        this._contentWidth = this.el.nativeElement.clientWidth - 60;
        this.reCalcColSizes();
        this.resizeObs.observe(this.el.nativeElement);
    }

    onChangeFilter(item: IFileTableHeadItem, option: IFilterOption) {
        if (item.type !== 'filter') return;
        if (item.activeFilter === option.value) {
            return;
        }
        item.activeFilter = option.value;
        item.label = option.label;
        if (item.value === 'owner') {
            this.params.owner = option.value === 'self' ? this.ctx.userInfo.userId : option.value;
        } else {
            // @ts-ignore
            this.params[item.value] = option.value;
        }
        this.paramsChange.emit(this.params);
    }

    reCalcColSizes() {
        this.colSizes = this._col_proportion.map(v => {
            return Math.round(this._contentWidth * v);
        });
        this.colSizesChange.emit(this.colSizes);
    }

    onResize(evt: Event, index: number) {
        const target = evt.target as HTMLElement;
        let prevStartX = (evt as MouseEvent).clientX;

        const onMouseMove = throttle((evt: MouseEvent) => {
            evt.preventDefault();
            const diff = (evt.clientX - prevStartX);
            if (this.colSizes[index] + diff < 100) return;
            if (this.colSizes[index] + diff > this._contentWidth - 100 * (this.colSizes.length - 1)) return;
            prevStartX = evt.clientX;
            this.colSizes = this.colSizes
                .map((v, i) =>
                    i === index ? v + diff : v - Math.floor(diff / (this.colSizes.length - 1))
                );
            this._col_proportion = this.colSizes.map(v => v / this._contentWidth);
            this.colSizesChange.emit(this.colSizes);
        }, 100);
        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
        };
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    }


}

