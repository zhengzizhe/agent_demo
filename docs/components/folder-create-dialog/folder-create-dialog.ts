import { Component, DestroyRef, EventEmitter, inject, Input, Output } from '@angular/core';
import { DlButton } from '../button';
import { ICreateFolderForm, IDlBaseDialog } from '../../services';
import { FormsModule } from '@angular/forms';
import { DlSpinMaskComponent } from '@/pages/docs/components';

@Component({
    selector: 'folder-create-dialog',
    templateUrl: './folder-create-dialog.html',
    styleUrls: ['./folder-create-dialog.scss'],
    standalone: true,
    imports: [
        DlButton,
        FormsModule,
        DlSpinMaskComponent
    ]
})
export class FolderCreateDialogComponent implements IDlBaseDialog {
    @Input()
    isLoading = false

    @Output()
    onClose = new EventEmitter<void>();

    @Output()
    onOk = new EventEmitter<Pick<ICreateFolderForm, 'name' | 'description'>>();

    destroyRef = inject(DestroyRef)

    name = '';
    description = '';

    nameError = false;

    onSubmit() {
        if (!this.name) {
            this.nameError = true;
            return;
        }
        this.onOk.emit({
            name: this.name.trim(),
            description: this.description
        });
    }
}
