import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, Input } from '@angular/core';

@Component({
    selector: 'doc-water-mark',
    template: ``,
    styles: [`
        :host {
            position: absolute;
            top: 0;
            left: 0;
            height: 100%;
            width: 100%;
            pointer-events: none;
            background-repeat: repeat;
            background-size: auto;
        }
    `],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocWaterMark {
    private _text = '';
    @Input()
    set text(v: string) {
        if (!v) return;
        this._text = v;
        this._watermark = this.createWatermark();
        this.applyWatermark();
    }

    get text() {
        return this._text;
    }

    private _watermark = '';

    constructor(
        public readonly host: ElementRef<HTMLCanvasElement>,
        protected destroyRef: DestroyRef
    ) {
    }

    ngAfterViewInit() {
        this.protectWatermark();
    }

    private createWatermark() {
        // const host = this.host.nativeElement
        if (!this.text) return '';
        const canvas = document.createElement('canvas');
        // canvas.width = 200;
        // canvas.height = 150;
        const ctx = canvas.getContext('2d');
        if (!ctx) return;
        ctx.rotate(-20 * Math.PI / 180);
        ctx.font = '13px Arial';
        ctx.fillStyle = '#f6f6f6';
        ctx.fillText(this.text, 10, 80);
        // const dpr = window.devicePixelRatio || 1;
        // canvas.width = host.offsetWidth;
        // canvas.height = 400;
        // // ctx.scale(dpr, dpr);
        //
        // ctx.font = `${6 * dpr}px Arial`;
        // ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
        // ctx.textAlign = 'center';  // 设置文本水平居中
        // ctx.textBaseline = 'middle';  // 设置文本垂直居中
        //
        // // 计算水印文本的宽度和高度
        // const textWidth = ctx.measureText(this.text).width;
        // const textHeight = 6 * dpr; // 文字的高度与字体大小相等
        // const angle = -30; // 倾斜角度
        // const textSpacing = 80; // 水平和垂直的文本间距
        //
        // // 动态计算文本间距，确保文字不会重叠
        // const horizontalSpacing = textSpacing + textWidth; // 水平方向的间距加上文本的宽度
        // const verticalSpacing = textSpacing + textHeight + 20; // 垂直方向的间距加上文本的高度
        //
        // // 循环绘制水印，直到覆盖整个容器
        // for (let y = -textHeight; y < canvas.height; y += verticalSpacing) {
        //     // 交错排列，偶数行和奇数行的水平位置不同
        //     let xOffset = (y % (verticalSpacing * 2) === 0) ? 0 : textWidth / 2;
        //
        //     for (let x = -textWidth; x < canvas.width; x += horizontalSpacing) {
        //         ctx.save(); // 保存当前绘制状态
        //
        //         // 移动坐标原点到水印文本位置（文本中心），加上交错的偏移量
        //         ctx.translate(x + xOffset + textWidth / 2, y + textHeight / 2); // 使文本旋转中心对齐到给定位置
        //
        //         // 旋转 Canvas 上下文
        //         ctx.rotate(angle * Math.PI / 180); // 旋转角度
        //
        //         // 绘制水印文本
        //         ctx.fillText(this.text, 0, 0); // 绘制文本（此时文本中心为原点）
        //
        //         ctx.restore(); // 恢复绘制状态
        //     }
        // }
        //
        return canvas.toDataURL('image/png');
    }

    applyWatermark() {
        this._watermark ||= this.createWatermark();
        const target = this.host.nativeElement;
        target.style.backgroundImage = `url(${this._watermark})`;
    }

    protectWatermark() {
        const observer = new MutationObserver(() => {
            this.applyWatermark();
        });
        observer.observe(this.host.nativeElement, {
            attributes: true,
            childList: true,
            subtree: true,
        });
        this.destroyRef.onDestroy(() => {
            observer.disconnect();
        });
    }
}
