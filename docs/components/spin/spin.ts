import {ChangeDetectionStrategy, Component, Input} from "@angular/core";
import {MatIcon} from "@angular/material/icon";

@Component({
  selector: 'dl-spin',
  template: `
    <i class="bc_icon bc_jiazai" [style.font-size]="iconSize"></i>
  `,
  styles: [`
    :host {
      width: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
      overflow: hidden;

      i {
        color: var(--cs-primary-color-disabled);
        animation: antRotate 1.2s infinite linear;
      }
    }
  `],
  imports: [
    MatIcon
  ],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlSpinComponent {

  @Input()
  iconSize = '40px'

}
