import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'dl-back-btn',
    template: `
        <i class="bc_icon bc_fanhui" ></i>
    `,
    styles: [`
        :host {
            cursor: pointer;

            > i {
                display: inline-block;
                transition: all ease-in-out .2s;
            }

            &:hover {
                > i {
                    transform: translateX(-5px);
                }
            }
        }
    `],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlBackBtnComponent {

}
