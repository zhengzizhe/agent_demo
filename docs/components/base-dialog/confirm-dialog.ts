import { ChangeDetectionStrategy, Component, DestroyRef, EventEmitter, Input, Output } from '@angular/core';
import { CheckBtn, DlButton, DlSpinMaskComponent } from '@/pages/docs/components';
import { IDlBaseDialog } from '@/pages/docs/services';

interface IDialogButton {
    id: string,
    text: string;
    type: 'primary' | 'simple' | 'danger';
    icon?: string;
}
@Component({
    selector: 'dl-confirm-dialog',
    template: `
        <dl-spin-mask [dlSpinning]="isLoading" style="border-radius: var(--cs-border-radius);">
            <div class="dl-dialog">
                <div class="dialog-header">
                    <h2 class="dialog-title">{{ title }}</h2>
                    <i class="bc_icon bc_guanbi dialog-close-btn" (click)="onClose.emit()"></i>
                </div>

                <div class="dialog-divider-line"></div>

                <div class="dialog-body">
                    <p class="msg">
                        {{ message }}
                    </p>
                    <ng-content></ng-content>
                </div>

                <div class="dialog-footer">
                    <button dl-button (click)="onClose.emit()">取消</button>
                    @for (btn of buttons; track btn.id) {
                        <button dl-button [type]="btn.type" (click)="onButtonClick.emit(btn)">{{ btn.text }}</button>
                    }
                </div>
            </div>
        </dl-spin-mask>
    `,
    styles: `
        :host {
            min-width: 300px;

            .msg {
                color: var(--cs-font-color-2);
            }
        }
    `,
    standalone: true,
    imports: [
        CheckBtn,
        DlButton,
        DlSpinMaskComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlConfirmDialogComponent implements IDlBaseDialog {
    @Input({required: true})
    title: string = '';

    @Input()
    message: string = '';

    @Input()
    isLoading = false

    @Input()
    buttons: IDialogButton[] = []

    @Output()
    onButtonClick = new EventEmitter<IDialogButton>();

    @Output()
    readonly onClose = new EventEmitter<void>();

    constructor(
        readonly destroyRef: DestroyRef
    ) {
    }
}
