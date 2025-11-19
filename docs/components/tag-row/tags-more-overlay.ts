import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { ITag } from '@/pages/docs/services';
import { DlTagComponent } from '@/pages/docs/components';

@Component({
    selector: 'dl-tags-more-overlay',
    template: `
        @for (tag of tags; track tag.id) {
            <dl-tag [text]="tag.name" [color]="tag.color" [backColor]="tag.backColor"></dl-tag>
        }
    `,
    styles: [`
        :host {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            max-width: 300px;
            overflow: hidden;
            padding: 12px;
            box-shadow: var(--cs-box-shadow-3);
            background: var(--cs-bg-color);
            border-radius: var(--cs-border-radius);
        }
    `],
    standalone: true,
    imports: [
        DlTagComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlTagsMoreOverlayComponent {
    @Input()
    tags: ITag[] = []
}
