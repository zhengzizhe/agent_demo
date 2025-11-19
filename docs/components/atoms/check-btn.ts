import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
    selector: 'dl-check-btn',
    template: `
        <i [class]="['bc_icon',  checked ? 'bc_xuanzhong-fill' : 'bc_weixuanzhong']"></i>
    `,
    styles: [`
        :host {
            cursor: pointer;
            color: var(--cs-font-color-3);

            &.active {
                color: var(--cs-primary-color);
            }

            &[disabled] {
                cursor: not-allowed;
                opacity: .8;
            }
        }
    `],
    standalone: true,
    host: {
        '[class.active]': 'checked',
        '[disabled]': 'disabled'
    },
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckBtn {
    @Input()
    checked: boolean = false;

    @Input()
    disabled: boolean = false;

}
