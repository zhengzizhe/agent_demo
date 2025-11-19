import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {RouterLinkActive} from "@angular/router";
import {throttle} from "@ccc/blockcraft";

@Component({
  selector: 'aside[dl-aside-menu]',
  templateUrl: './aside-menu.html',
  styleUrls: ['./aside-menu.scss'],
  imports: [
    RouterLinkActive,
  ],
  standalone: true,
})
export class AsideMenuComponent {
  @Input()
  hide = false

  @Output()
  hideChange = new EventEmitter<boolean>();

  @ViewChild('container', {read: ElementRef})
  container!: ElementRef<HTMLElement>

  constructor(
  ) {
  }

  onResizeHandlerMousedown(event: MouseEvent) {
    event.preventDefault();
    const startX = event.clientX;
    const startWidth = this.container.nativeElement.offsetWidth;
    const viewportWidth = document.body.clientWidth;
    const moveHandler = throttle((e: MouseEvent) => {
      let offset = e.clientX - startX;
      let sum = startWidth + offset;
      sum = Math.max(234, sum);
      sum = Math.min(sum, viewportWidth / 2);
      this.container.nativeElement.style.width = sum + 'px';
    }, 30)
    const upHandler = () => {
      document.removeEventListener('mousemove', moveHandler);
      document.removeEventListener('mouseup', upHandler);
    };
    document.addEventListener('mousemove', moveHandler);
    document.addEventListener('mouseup', upHandler);
  }

}
