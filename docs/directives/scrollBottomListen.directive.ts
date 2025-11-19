import {Directive, ElementRef, EventEmitter, HostListener, Input, Output} from "@angular/core";
import {throttle} from "@ccc/blockcraft";

@Directive({
  selector: '[scrollBottomListen]',
  standalone: true
})
export class scrollBottomListenDirective {
  @Input()
  distance = 100

  @Output()
  onScrollBottom = new EventEmitter<void>()

  constructor(
    readonly elementRef: ElementRef
  ) {
  }

  get hostElement(): HTMLElement {
    return this.elementRef.nativeElement
  }

  @HostListener('scroll', ['$event'])
  onScroll = throttle((event: Event) => {
    const target = this.hostElement
    if (target.scrollTop + target.clientHeight > target.scrollHeight - this.distance) {
      this.onScrollBottom.emit()
    }
  }, 200)

}
