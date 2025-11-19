import { Component, DestroyRef, EventEmitter, Input, Output } from '@angular/core';
import { ICreateDocumentForm, IDlBaseDialog } from '../../services';
import { DlSpinMaskComponent } from '@/pages/docs/components/spin/spin-mask';

@Component({
    selector: 'doc-create-dialog',
    templateUrl: './doc-create-dialog.html',
    styleUrls: ['./doc-create-dialog.scss'],
    imports: [
        DlSpinMaskComponent
    ],
    standalone: true
})
export class DocCreateDialogComponent implements IDlBaseDialog {
    @Input()
    targetId!: string;

    @Input()
    isLoading = false

    @Output()
    onClose = new EventEmitter<void>();

    @Output()
    onOk = new EventEmitter<Pick<ICreateDocumentForm, 'name' | 'type'>>();

    constructor(
        readonly destroyRef: DestroyRef
    ) {
    }

    select() {
        this.onOk.emit({
            name: '',
            type: 'text'
        });
    }

}
