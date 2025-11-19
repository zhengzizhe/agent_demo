import {ChangeDetectionStrategy, Component, Input} from "@angular/core";

export interface IMenu{
  title: string;
  route: string;
  icon: string;
}
@Component({
  selector: 'button[dl-menu-button]',
  template: `
    <i [class]="['bc_icon', menu.icon]"></i>
    {{ menu.title }}
  `,
  styles: [`
    :host {
      width: 100%;
      display: flex;
      padding: 8px;
      align-items: center;
      gap: 4px;
      height: 36px;
      cursor: pointer;
      border-radius: var(--cs-border-radius, 8px);
      transition: all ease .2s;

      &:hover {
        background: var(--cs-bg-color-2);
      }

      &.active {
        color: var(--cs-primary-color);
        background-color: var(--cs-bg-color-3);
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class DlMenuButtonComponent {

  @Input({required: true})
  menu!: IMenu

  constructor() {

  }


}
