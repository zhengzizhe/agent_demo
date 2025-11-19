import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
    selector: 'dl-like-btn',
    template: ``,
    styles: [`
        :host {

            &.bc_dianzan1 {
                color: var(--cs-primary-color);
            }
        }
    `],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '[class]': '["bc_icon", liked ? "bc_dianzan1" : "bc_dianzan"]',
    }
})
export class DlLikeBtnComponent {
    @Input()
    liked = false;
}
