import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgForOf } from '@angular/common';
import { PermissionRole } from '@/pages/docs/services';

@Component({
    selector: 'role-option-menu',
    template: `
        <div class="dl-dropdown-menu-item" *ngFor="let option of roleOptions"
             [class.active]="role === option.value"
             (click)="onRoleChange(option.value)">
            {{ option.label }}
        </div>
        <ng-content></ng-content>
    `,
    styles: [`
        :host {

        }
    `],
    imports: [
        NgForOf
    ],
    standalone: true,
    host: {
        '[class.dl-dropdown-menu]': 'true'
    }
})
export class RoleOptionMenu {
    @Input()
    roleOptions: any[] = [];

    @Input({required: true})
    role: PermissionRole

    @Output()
    roleChange = new EventEmitter<PermissionRole>();

    @Output()
    remove = new EventEmitter<void>();

    onRoleChange(role: PermissionRole) {
        this.roleChange.emit(role);
    }

    onRemove() {
        this.remove.emit();
    }

}
