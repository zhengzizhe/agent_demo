import { Component, DestroyRef, EventEmitter, Output } from '@angular/core';
import { DlButton } from '@/pages/docs/components';
import { FormsModule } from '@angular/forms';
import { CsesSelectorComponent, CsesSelectorItemComponent } from '@ccc/cses-common';
import { NgForOf } from '@angular/common';
import { IDlBaseDialog } from '@/pages/docs/services';

@Component({
    selector: 'export-config-menu',
    templateUrl: 'export-config-menu.html',
    styleUrls: ['export-config-menu.scss'],
    imports: [
        DlButton,
        FormsModule,
        CsesSelectorComponent,
        CsesSelectorItemComponent,
        NgForOf
    ],
    standalone: true
})
export class ExportConfigMenuComponent implements IDlBaseDialog{
    marginList = [
        { label: '默认', value: 1 },
        { label: '默认', value: 1 },
        { label: '默认', value: 1 },
    ]

    pageSizeList = [
        { label: 'A0', value: 'A0' },
        { label: 'A1', value: 'A1' },
        { label: 'A2', value: 'A2' },
        { label: 'A3', value: 'A3' },
        { label: 'A4', value: 'A4' },
        { label: 'A5', value: 'A5' },
        { label: 'A6', value: 'A6' },
        { label: 'Legal', value: 'Legal' },
        { label: 'Letter', value: 'Letter' },
        { label: 'Tabloid', value: 'Tabloid' },
        { label: 'Ledger', value: 'Ledger' },
    ]

    // 排版
    layoutList = [
        { label: '横向', value: 'landscape' },
        { label: '纵向', value: 'portrait' }
    ]

    // 缩放比例
    scaleList = [
        { label: '25%', value: .25 },
        { label: '50%', value: .5 },
        { label: '75%', value: .75 },
        { label: '100%', value: 1 },
        { label: '125%', value: 1.25 },
        { label: '150%', value: 1.5 },
        { label: '175%', value: 1.75 },
        { label: '200%', value: 2 }
    ]

    config = {
        pageSize: 'A4',
        layout: 'portrait',
        scale: 1
    }

    @Output()
    onClose = new EventEmitter<void>();

    @Output()
    onOk = new EventEmitter<typeof this.config>();

    constructor(
        readonly destroyRef: DestroyRef
    ) {
    }

    onSubmit() {
        this.onOk.emit(this.config);
    }
}
