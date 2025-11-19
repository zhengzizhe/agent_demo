import { Component, DestroyRef, EventEmitter, Input, Output } from '@angular/core';
import { DlButton, DlSpinMaskComponent } from '@/pages/docs/components';
import { IDlBaseDialog } from '@/pages/docs/services';
import { Date2Pipe } from '@/pages/docs/pipes';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'doc-save-version-base-dialog',
    template: `
        <dl-spin-mask [dlSpinning]="isLoading" text="正在创建版本...">
            <div class="dl-dialog" style="width: 400px;">
                <div class="dialog-header">
                    <span class="dialog-title">保存 {{ now | date2 }} 的文档版本</span>
                    <i class="bc_icon bc_guanbi dialog-close-btn" (click)="onClose.emit()"></i>
                </div>

                <div class="dialog-divider-line"></div>

                <div class="dialog-body">
                    <div class="dl-form-item">
                        <label for="versionName">版本名称</label>
                        <input id="versionName" type="text" class="dl-input" maxlength="100" [(ngModel)]="form.name" (ngModelChange)="form.name = $event.trim()" placeholder="请输入版本名称">
                    </div>
                    <div class="dl-form-item">
                        <label for="versionName">备注</label>
                        <textarea id="versionName" style="line-height: 24px;" type="text" class="dl-textarea" placeholder="描述主要改动点" maxlength="100" [(ngModel)]="form.description"></textarea>
                    </div>
                </div>

                <div class="dialog-footer">
                    <button dl-button (click)="onClose.emit()">取消</button>
                    <button dl-button type="primary" [dlDisabled]="!form.name" (click)="onSure.emit(form)">确定</button>
                </div>
            </div>
        </dl-spin-mask>
    `,
    styles: `
    :host {
    }
    `,
    imports: [
        DlSpinMaskComponent,
        Date2Pipe,
        DlButton,
        FormsModule
    ],
    standalone: true
})
export class DlSaveVersionDialogComponent implements IDlBaseDialog {
    @Input()
    isLoading = false;

    @Input()
    now: number

    @Input()
    form = {
        name: '',
        description: ''
    }

    @Output()
    readonly onSure = new EventEmitter<typeof this.form>();

    @Output()
    onClose = new EventEmitter<void>();

    constructor(
        readonly destroyRef: DestroyRef
    ) {
    }

}
