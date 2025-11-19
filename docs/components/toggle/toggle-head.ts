import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'dl-toggle-head',
    template: `
        <span class="btn-toggle" [class.expand]="expand"
              (click)="onToggleExpand()">
              <i class="bc_icon bc_a-sanjiao-jinru6 dl-btn-hover"></i>
        </span>
        <ng-content></ng-content>
    `,
    styles: [`
        :host {
            display: flex;
            align-items: center;
            gap: 4px;
            color: var(--cs-font-color-3);

            .btn-toggle {
                width: 10px;
                height: 10px;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: transform ease .3s;

                > i {
                    font-size: 8px;
                    width: 10px;
                    height: 10px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }

                &.expand {
                    transform: rotate(90deg);
                }
            }
        }
    `],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlToggleHead {
    @Input()
    expand = true;

    @Output()
    expandChange: EventEmitter<boolean> = new EventEmitter<boolean>();

    constructor(
        private cdr: ChangeDetectorRef
    ) {
    }

    onToggleExpand() {
        this.expandChange.emit(this.expand = !this.expand);
        this.cdr.markForCheck();
    }

}
