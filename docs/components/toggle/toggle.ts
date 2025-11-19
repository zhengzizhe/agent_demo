import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DlToggleHead } from './toggle-head';

@Component({
    selector: 'dl-toggle',
    template: `
        <dl-toggle-head></dl-toggle-head>
    `,
    styles: [``],
    standalone: true,
    imports: [
        DlToggleHead
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToggleComponent {
    @Input()
    title: string



}
