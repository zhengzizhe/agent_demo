import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'dl-tag',
    template: `
        <span>{{ text }}</span>
        @if(withDelete) {
            <i class="bc_icon bc_x-01" (click)="onDelete.emit()"></i>
        }
    `,
    styles: [`
        :host {
            display: flex;
            align-items: center;
            gap: 4px;
            border-radius: 4px;
            padding: 0 4px;
            height: 20px;
            font-size: 12px;

            > span {
                //max-width: 6em;
                min-width: 0;
                flex: 1;
                white-space: nowrap;
                text-overflow: ellipsis;
                overflow: hidden;
            }

            > i {
                font-size: smaller;
                color: var(--cs-font-color-2);
                cursor: pointer;
            }
        }
    `],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '[style.background-color]': 'backColor || "#f2f3f0"',
        '[style.color]': 'color',
        '[title]': 'text'
    }
})
export class DlTagComponent {
    @Input()
    text: string = '';

    @Input()
    color: string = '';

    @Input()
    backColor: string = '#f2f3f0';

    @Input()
    withDelete = false

    @Output()
    onDelete = new EventEmitter<void>();
}
