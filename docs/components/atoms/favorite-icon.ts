import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';

@Component({
    selector: 'dl-favorite-icon',
    template: ``,
    styles: [`
        :host {
            color: var(--cs-font-color-3);

            &.active {
                color: #FFCC17;
            }
        }
    `],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '[class]': '["bc_icon", active ? "bc_shoucang-fill" : "bc_shoucang"]',
    }
})
export class FavoriteIcon {
    @Input()
    @HostBinding('class.active')
    active = false
}
