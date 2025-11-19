import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EditableBlockComponent } from '@ccc/blockcraft';

@Component({
    selector: 'div.guidance-block',
    template: ``,
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GuidanceBlockComponent extends EditableBlockComponent {

}
