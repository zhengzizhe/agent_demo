import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { CheckBtn } from '@/pages/docs/components';

@Component({
    selector: 'dl-folder-tree-view-node',
    template: `
        @if (useCheckBtn) {
            <div class="folder-tree-view-node__selector">
                <dl-check-btn [checked]="active"></dl-check-btn>
            </div>
        }
        <div class="folder-tree-view-node__container dl-btn-hover" [class.active]='active'
             [style.padding-left.px]="(depth || 0) * 30">
            <div class="folder-tree-view-node__toggle" [class.active]="isExpanded">
                @if (hasChild) {
                    <i [class]="['bc_icon', isLoading ? 'bc_jiazai' : 'bc_xiajaintou']"
                       (click)="onEmitExpand()"></i>
                }
            </div>

            <div class="folder-tree-view-node__content" (click)="!disabled && nodeClick.emit()">
                <mat-icon class="folder-tree-view-node__icon" svgIcon="bc_folder-close-color"></mat-icon>
                <span>{{ name }}</span>
            </div>
        </div>
    `,
    styles: [`
        :host {
            position: relative;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
            width: 100%;
            cursor: pointer;

            &[disabled] {
                .folder-tree-view-node__content {
                    cursor: not-allowed;
                    opacity: 0.6;
                }
            }

            &:hover {
                .folder-tree-view-node__selector {
                    visibility: visible;
                }
            }

            .folder-tree-view-node {
                &__selector {
                    margin-right: 8px;
                    visibility: hidden;
                }

                &__toggle {
                    margin-right: 8px;
                    width: 20px;
                    height: 20px;

                    > i {
                        width: 100%;
                        height: 100%;
                        display: flex;
                        align-items: center;
                        cursor: pointer;
                        justify-content: center;
                        transform: rotate(-90deg);
                        transition: transform 0.2s ease-in-out;

                        &.bc_jiazai {
                            color: var(--cs-primary-color);
                            animation: antRotate 1.2s infinite linear;
                            transition: none;
                        }
                    }

                    &.active {
                        > i {
                            transform: rotate(0deg);
                        }
                    }
                }

                &__icon {
                    margin-right: 4px;
                }

                &__container, &__content {
                    flex: 1;
                    display: flex;
                    align-items: center;
                    padding: 0 8px;
                    height: 36px;
                    font-size: 14px;

                    &.active {
                        background: var(--cs-primary-color-transparent);
                    }
                }
            }
        }
    `],
    imports: [
        MatIcon,
        CheckBtn
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '[attr.disabled]': 'disabled',
    }
})
export class FolderTreeViewNodeComponent {
    @Input()
    useCheckBtn = false;

    @Input()
    active = false;

    @Input()
    isExpanded = false;

    @Input()
    name = '';

    @Input()
    isLoading = false;

    @Input()
    depth = 0;

    @Input()
    hasChild = true;

    private _disabled: true | null = null;

    @Input()
    set dlDisabled(val: boolean) {
        this._disabled = val ? true : null;
    }

    get disabled() {
        return this._disabled;
    }

    @Output()
    toggleExpand = new EventEmitter<any>();

    @Output()
    nodeClick = new EventEmitter<any>();

    onEmitExpand() {
        this.toggleExpand.emit();
    }
}
