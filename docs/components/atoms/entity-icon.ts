import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { EntityType, EntityTypeStr, IEntity } from '@/pages/docs/services';
import { MatIcon } from '@angular/material/icon';
import { NgStyle } from '@angular/common';

const entityIcons: Record<EntityType, string> = {
    [EntityType.Folder]: 'bc_folder-close-color',
    [EntityType.Document]: 'bc_wendang0102',
    [EntityType.Space]: 'bc_kongjian',
    [EntityType.File]: 'bc_wendang0102'
};

@Component({
    selector: 'dl-entity-icon',
    template: `
        @if (icon) {

        } @else {
            <mat-icon [svgIcon]="entityIcons[nodeType]" [ngStyle]="{width: size + 'px', height: size + 'px'}" ></mat-icon>
        }
    `,
    styles: [`
        :host {
            display: block;
        }
    `],
    imports: [
        MatIcon,
        NgStyle
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DlEntityIconComponent {
    @Input()
    icon: IEntity['icon'];

    @Input()
    nodeType: EntityTypeStr | EntityType

    @Input()
    size: number = 16

    protected entityIcons = entityIcons;
}
