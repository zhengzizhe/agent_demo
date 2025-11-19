import { Component, DestroyRef, EventEmitter, Input, Output } from '@angular/core';
import { IDlBaseDialog } from '@/pages/docs/services';
import { CheckBtn, DlButton, DlSpinMaskComponent } from '@/pages/docs/components';

@Component({
    selector: 'dl-delete-confirm-dialog',
    templateUrl: 'delete-confirm-dialog.html',
    styleUrls: ['delete-confirm-dialog.scss'],
    imports: [
        CheckBtn,
        DlButton,
        DlSpinMaskComponent
    ],
    standalone: true
})
export class DeleteConfirmDialog implements IDlBaseDialog {
    @Input()
    readonly title: string = '删除';

    @Input()
    readonly message: string = '确认删除?';

    @Input()
    isLoading = false

    @Output()
    readonly onConfirm = new EventEmitter<{ recycle: boolean }>();

    @Output()
    readonly onClose = new EventEmitter<void>();

    recycleChecked: boolean = true;

    constructor(
        readonly destroyRef: DestroyRef
    ) {
    }

    onConfirmClick() {
        this.onConfirm.emit({ recycle: this.recycleChecked });
    }
}
