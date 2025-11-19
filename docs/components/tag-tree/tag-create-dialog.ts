import { Component, DestroyRef, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { NgForOf, NgIf } from '@angular/common';
import { ITagColor, TagColorList } from './const';
import { ITag } from '@/pages/docs/services';
import { FormsModule } from '@angular/forms';
import { DlButton, DlSpinMaskComponent } from '@/pages/docs/components';

@Component({
    selector: 'tag-create-dialog',
    template: `
        <dl-spin-mask [dlSpinning]="isSpinning">
            <div class="dialog">
                <input class="dl-input" placeholder="输入标签名" autofocus [(ngModel)]="form.name" maxlength="8"
                       [class.error]="isNameInputError" #inputElement />
                <div class="color-wrapper">
                    <p>颜色</p>
                    <div class="color-list">
                    <span class="color-item" *ngFor="let color of colorList"
                          [class.active]="color.color === form.color"
                          [style.background]="color.backColor" (click)="onSelectColor(color)"></span>
                    </div>
                </div>
                <div class="divider"></div>
                <div style="display: flex; align-items: center; justify-content: flex-end; gap: 8px;">
                    @if (tag) {
                        <button dl-button type="primary" (click)="onUpdateBtnClick()"
                                [dlDisabled]="form.name === tag.name && form.color === tag.color">
                            确认修改
                        </button>
                        <button dl-button type="danger" icon="bc_shanchu" (click)="onDeleteBtnClick()"
                                *ngIf="deleteAble">
                            {{ deleteConfirmTwice ? '确认删除' : '删除' }}
                        </button>
                    } @else {
                        <button dl-button type="primary" (click)="onCreateBtnClick()">
                            <i class="bc_icon bc_plus"></i> 创建
                        </button>
                    }
                </div>

            </div>
        </dl-spin-mask>
    `,
    styles: [`
        .dialog {
            width: 196px;
            padding: 10px;
            box-shadow: var(--cs-box-shadow-2);
            display: flex;
            flex-direction: column;
            gap: 10px;
            background: var(--cs-bg-color);
            border-radius: var(--cs-border-radius);

            .divider {
                height: 1px;
                background-color: var(--cs-border-color);
                width: 100%;
            }

            button {
                flex: 1;
                border-radius: 4px;
            }

            .btn-create {
                width: 100%;
                //color: var(--cs-primary-color);
                //text-align: center;
                //align-items: center;
            }

            .color-wrapper {

                > p {
                    margin: 0 0 8px;
                    line-height: 20px;
                    font-size: 14px;
                    color: #999;
                    font-weight: bold;
                }

                .color-list {
                    display: flex;
                    justify-content: space-between;
                    flex-wrap: wrap;
                    gap: 4px;

                    .color-item {
                        width: 16px;
                        height: 16px;
                        border-radius: 4px;
                        cursor: pointer;
                        transition: all ease-in-out 0.1s;

                        &.active {
                            outline: 2px solid #4857E2;
                        }

                        &:hover {
                            outline: 2px solid #4d90fe;
                        }
                    }
                }
            }

        }
    `],
    imports: [
        NgForOf,
        FormsModule,
        DlButton,
        DlSpinMaskComponent,
        NgIf
    ],
    standalone: true
})
export class TagCreateDialog {
    private _tag: ITag;
    @Input()
    set tag(tag: ITag) {
        if(!tag) return
        this._tag = tag;
        this.form.name = tag.name;
        this.form.color = tag.color;
        this.form.backColor = tag.backColor;
    }

    get tag() {
        return this._tag;
    }

    @Input()
    isSpinning = false;

    @Input()
    deleteAble = true;

    @Output()
    create = new EventEmitter<Pick<ITag, 'name' | 'color' | 'backColor'>>();

    @Output()
    delete = new EventEmitter<ITag>();

    @Output()
    update = new EventEmitter<Pick<ITag, 'name' | 'color' | 'backColor'>>();

    @ViewChild('inputElement', { read: ElementRef }) inputElement: ElementRef;

    isNameInputError = false;
    colorList = TagColorList;
    form = {
        name: '',
        color: TagColorList[0].color,
        backColor: TagColorList[0].backColor
    };
    deleteConfirmTwice = false;

    constructor(
        readonly destroyRef: DestroyRef
    ) {
    }

    ngAfterViewInit() {
        this.inputElement.nativeElement.focus();
    }

    onSelectColor(color: ITagColor) {
        this.form.color = color.color;
        this.form.backColor = color.backColor;
    }

    onCreateBtnClick() {
        if (!this.form.name) {
            this.isNameInputError = true;
            return;
        }
        this.create.emit(this.form);
    }

    onUpdateBtnClick() {
        this.update.emit(this.form);
    }

    onDeleteBtnClick() {
        if (this.deleteConfirmTwice) {
            this.delete.emit(this.tag);
        } else {
            this.deleteConfirmTwice = true;
        }
    }
}
