import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { NgIf } from '@angular/common';

type ButtonType = 'primary' | 'danger' | 'simple' | 'success' | 'warning' | 'info' | 'gray'

@Component({
    selector: 'button[dl-button]',
    template: `
        <i [class]="['bc_icon', icon]" *ngIf="icon"></i>
        <ng-content></ng-content>
    `,
    styleUrl: 'dl-button.scss',
    standalone: true,
    imports: [
        NgIf
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlButton {
    @Input()
    @HostBinding('attr.type')
    public type: ButtonType = 'simple';

    @Input()
    @HostBinding('attr.hover-type')
    hoverType: ButtonType | null = null;

    @Input()
    public size: 'small' | 'medium' | 'large' = 'medium';

    @Input()
    icon: string | null = null;

    private _dlDisabled = false;

    @Input()
    set dlDisabled(val: boolean) {
        this._dlDisabled = val;
        this.disabled = val ? 'disabled' : null;
    }

    get dlDisabled() {
        return this._dlDisabled;
    }

    @HostBinding('attr.disabled')
    disabled = null;

    @Input()
    @HostBinding('class.borderless')
    borderless = false

    constructor() {
    }

}
