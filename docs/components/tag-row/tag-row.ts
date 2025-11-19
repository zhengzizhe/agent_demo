import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Injector, Input, SimpleChanges } from '@angular/core';
import { ITag } from '@/pages/docs/services';
import { DlTagComponent } from '@/pages/docs/components';
import { Overlay } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { DlTagsMoreOverlayComponent } from '@/pages/docs/components/tag-row/tags-more-overlay';

@Component({
    selector: 'dl-tag-row',
    templateUrl: 'tag-row.html',
    styleUrls: ['tag-row.scss'],
    standalone: true,
    imports: [
        DlTagComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlTagRowComponent {
    @Input()
    tags: ITag[] = [];

    @Input()
    maxShowCount = 3;

    tagsToShow: ITag[] = [];

    constructor(
        readonly cdr: ChangeDetectorRef,
        private overlay: Overlay,
        private injector: Injector
    ) {
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['tags'] || changes['maxShowCount']) {
            this.tagsToShow = this.tags.slice(0, this.maxShowCount);
            this.cdr.markForCheck();
        }
    }

    onShowMore(evt: MouseEvent) {
        evt.stopPropagation();
        const target = (evt.target as HTMLElement).closest('dl-tag');
        const portal = new ComponentPortal(DlTagsMoreOverlayComponent, null, this.injector);
        const overlayRef = this.overlay.create({
            positionStrategy: this.overlay.position().flexibleConnectedTo(target).withPositions([
                { originX: 'end', overlayX: 'end', originY: 'bottom',  overlayY: 'top', offsetY: 4 },
                { originX: 'end',  overlayX: 'end', originY: 'top', overlayY: 'bottom', offsetY: -4 }
            ]),
            hasBackdrop: true,
            backdropClass: 'dl-overlay-backdrop',
        });
        const cpr = overlayRef.attach(portal);
        cpr.setInput('tags', this.tags)
        overlayRef.backdropClick().subscribe(() => {
            overlayRef.dispose();
        })
    }
}
