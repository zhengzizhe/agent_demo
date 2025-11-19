import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DlSpinComponent } from '@/pages/docs/components';
import { NgIf } from '@angular/common';

@Component({
    selector: 'dl-spin-mask',
    template: `
        <div class="mask" *ngIf="dlSpinning">
            <dl-spin></dl-spin>
            <p class="text" *ngIf="text">{{ text }}</p>
        </div>

        <ng-content></ng-content>
    `,
    styles: [`
        :host {
            display: block;
            position: relative;

            .mask {
                position: absolute;
                inset: 0;
                background: rgba(255, 255, 255, .6);
                z-index: 1000;
                display: flex;
                justify-content: center;
                align-items: center;
                flex-direction: column;

                .text {
                    margin-top: 12px;
                    font-size: 14px;
                    color: var(--cs-primary-color);
                }
            }
        }
    `],
    imports: [
        DlSpinComponent,
        NgIf
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlSpinMaskComponent {
    @Input()
    dlSpinning: boolean = false;

    @Input()
    text: string = '';

    constructor() {
    }
}
