import { ComponentRef, DestroyRef, EventEmitter, Injectable, Injector } from '@angular/core';
import { ConnectedPosition, Overlay, OverlayConfig, OverlayRef, PositionStrategy } from '@angular/cdk/overlay';
import { ComponentPortal, ComponentType } from '@angular/cdk/portal';
import { fromEvent, merge, take } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IDlBaseDialog, IDlContextMenuItem } from './types';
import { DlContextMenuComponent } from '@/pages/docs/components';

const defaultConnectedPositions: ConnectedPosition[] = [
    { originX: 'end', originY: 'bottom', overlayX: 'end', overlayY: 'top', offsetY: 4 },
    { originX: 'end', originY: 'bottom', overlayX: 'start', overlayY: 'top', offsetY: 4 }
];

interface IContextMenuPosition {
    x: number;
    y: number;
}

type IOverlayConfig = {
    target?: HTMLElement;
    // 和target搭配使用
    connectedPositions?: ConnectedPosition[];
    // 全局位置
    globalPosition?: IContextMenuPosition;
    hasBackdrop?: boolean;
    transparentBackdrop?: boolean
    panelClass?: string

    // 返回false则不关闭
    onClose?: () => boolean
}

@Injectable()
export class DocsOverlayService {
    constructor(
        readonly overlay: Overlay,
        readonly injector: Injector
    ) {
    }

    private contextMenuMap = new Map<string, OverlayRef>();

    showDialog<T extends IDlBaseDialog>(dialog: ComponentType<T>, config: IOverlayConfig = {}) {
        let positionStrategy: PositionStrategy;
        if (config?.target) {
            positionStrategy = this.overlay.position().flexibleConnectedTo(config.target)
                .withPositions(config.connectedPositions || defaultConnectedPositions);
        } else {
            positionStrategy = this.overlay.position().global().centerHorizontally().centerVertically();
        }

        const overlayConfig: OverlayConfig = {
            positionStrategy,
            hasBackdrop: config?.hasBackdrop ?? true,
            panelClass: config.panelClass
        };
        if (config.transparentBackdrop) {
            overlayConfig.backdropClass = 'cdk-overlay-transparent-backdrop';
        }
        const overlayRef = this.overlay.create(overlayConfig);
        const portal = new ComponentPortal(dialog, null, this.injector);
        const cpr = overlayRef.attach(portal) as ComponentRef<T>;

        merge(overlayRef.backdropClick(), cpr.instance.onClose).pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(() => {
            const res = config?.onClose?.() ?? true;
            if (!res) return;
            overlayRef.dispose();
        });

        return { cpr, overlayRef };
    }

    showContextMenu(config: IOverlayConfig & { menus: IDlContextMenuItem[] }) {
        const { target, menus, connectedPositions, globalPosition, transparentBackdrop, hasBackdrop } = config;
        let positionStrategy: PositionStrategy;
        if (globalPosition) {
            positionStrategy = this.overlay.position().global().left(`${globalPosition.x}px`).top(`${globalPosition.y}px`);
        } else {
            positionStrategy = this.overlay.position().flexibleConnectedTo(target!)
                .withPositions(connectedPositions || defaultConnectedPositions)
                .withPush(true);
        }
        const portal = new ComponentPortal(DlContextMenuComponent);
        const dialogRef = this.overlay.create({
            positionStrategy,
            hasBackdrop: hasBackdrop ?? false,
            backdropClass: transparentBackdrop ? 'cdk-overlay-transparent-backdrop' : '',
            panelClass: config.panelClass
        });
        const cpr = dialogRef.attach(portal) as ComponentRef<DlContextMenuComponent>;
        cpr.setInput('menus', menus);

        const close = () => {
            if (!hasBackdrop) {
                target.style.pointerEvents = null;
            }
            cpr.instance.hostEl.nativeElement.classList.add('dl-ani-dropdown-close');
            setTimeout(() => {
                dialogRef.dispose();
            }, 230);
        };

        if (!hasBackdrop) {
            target.style.pointerEvents = 'none';
            fromEvent(document, 'click', { capture: true }).pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(event => {
                if (!cpr.instance.hostEl.nativeElement.contains(event.target as HTMLElement)) {
                    close();
                }
            });
        }

        dialogRef.backdropClick().pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(close);

        return {
            cpr,
            close
        };
    }

}
