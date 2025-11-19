import {
    Directive,
    ElementRef,
    Input,
    Renderer2,
    OnChanges,
    SimpleChanges,
    Output,
    EventEmitter
} from '@angular/core';

@Directive({
    selector: '[dlExpandCollapse]',
    standalone: true,
})
export class ExpandCollapseDirective implements OnChanges {

    @Input('dlExpandCollapse') expanded = false; // 控制开关
    @Input() expandDuration = 225; // 动画时间 (ms)
    @Input() expandTiming = 'cubic-bezier(0.4, 0, 0.2, 1)'; // 动画缓动函数
    @Output() expandTransitionEnd = new EventEmitter<boolean>(); // 动画结束事件

    private isAnimating = false;

    constructor(private el: ElementRef, private renderer: Renderer2) {
    }

    ngOnInit() {
        this.renderer.setStyle(this.el.nativeElement, 'overflow', 'hidden');
        this.renderer.setStyle(this.el.nativeElement, 'height', this.expanded ? 'auto' : '0px');
        this.renderer.setStyle(this.el.nativeElement, 'transition', `height ${this.expandDuration}ms ${this.expandTiming}`);
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['expanded']) {
            this.toggle(this.expanded);
        }

        if (changes['expandDuration'] || changes['expandTiming']) {
            // 动态更新过渡样式
            this.renderer.setStyle(
                this.el.nativeElement,
                'transition',
                `height ${this.expandDuration}ms ${this.expandTiming}`
            );
        }
    }

    private toggle(expand: boolean) {
        const el = this.el.nativeElement as HTMLElement;
        // if (this.isAnimating) return;

        this.isAnimating = true;

        if (expand) {
            // 展开：先测量高度
            const startHeight = el.offsetHeight;
            const endHeight = el.scrollHeight;

            this.renderer.setStyle(el, 'height', `${startHeight}px`);
            el.getBoundingClientRect(); // 强制重绘
            this.renderer.setStyle(el, 'height', `${endHeight}px`);

            const onEnd = () => {
                this.renderer.setStyle(el, 'height', 'auto');
                el.removeEventListener('transitionend', onEnd);
                this.isAnimating = false;
                this.expandTransitionEnd.emit(true);
            };
            el.addEventListener('transitionend', onEnd);
        } else {
            // 收起：从当前高度过渡到 0
            const startHeight = el.scrollHeight;
            this.renderer.setStyle(el, 'height', `${startHeight}px`);
            el.getBoundingClientRect(); // 强制重绘
            this.renderer.setStyle(el, 'height', '0px');

            const onEnd = () => {
                el.removeEventListener('transitionend', onEnd);
                this.isAnimating = false;
                this.expandTransitionEnd.emit(false);
            };
            el.addEventListener('transitionend', onEnd);
        }
    }
}
