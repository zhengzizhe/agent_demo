import { Component, Input } from '@angular/core';
import { DlLikeBtnComponent } from '@/pages/docs/components';
import { EntityService, IDocDetail } from '@/pages/docs/services';
import { ComponentPortal } from '@angular/cdk/portal';
import { DlDocLikeDialogComponent } from './doc-like-dialog/doc-like-dialog';
import { merge } from 'rxjs';

@Component({
    selector: 'doc-aside-right',
    templateUrl: 'doc-aside-right.html',
    styleUrls: ['../doc-aside-left/doc-aside-left.scss'],
    imports: [
        DlLikeBtnComponent
    ],
    standalone: true
})
export class DocAsideRightComponent {
    @Input()
    isShowAside = false;

    @Input({required: true})
    docDetail: IDocDetail

    constructor(
        readonly entityService: EntityService,
    ) {
    }

    onLike(evt: Event) {
        if (!this.docDetail) return;
        const target = evt.target as HTMLInputElement;

        const overlayRef = this.entityService.overlayService.overlay.create({
            positionStrategy: this.entityService.overlayService.overlay.position().flexibleConnectedTo(target).withPositions([
                { originX: 'start', originY: 'bottom', overlayX: 'end', overlayY: 'bottom', offsetX: -8 }
            ]),
            hasBackdrop: true,
            backdropClass: 'cdk-overlay-transparent-backdrop'
        });
        const cpr = overlayRef.attach(new ComponentPortal(DlDocLikeDialogComponent, null, this.entityService.injector));
        cpr.setInput('docDetail', this.docDetail);
        merge(cpr.instance.onClose, overlayRef.backdropClick()).subscribe(() => {
            overlayRef.dispose();
        });
    }
}
