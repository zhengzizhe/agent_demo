import { DestroyRef, EventEmitter } from '@angular/core';

export abstract class IDlBaseDialog {
    onClose: EventEmitter<void>;
    destroyRef: DestroyRef;
}

export interface IDlContextMenuItem {
    id: any;
    name: string;
    label: string;
    value?: any;
    className?: string;
    icon?: string;
    disabled?: boolean;
    divide?: boolean;
    active?: boolean;
}
