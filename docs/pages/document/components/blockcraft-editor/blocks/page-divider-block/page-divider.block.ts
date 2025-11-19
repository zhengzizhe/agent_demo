import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PageDividerBlockModel } from './index';
import { BaseBlockComponent } from '@ccc/blockcraft';

@Component({
    selector: 'div.page-divider-block',
    template: `
        <div class="page-divider" contenteditable="false"></div>
    `,
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageDividerBlockComponent extends BaseBlockComponent<PageDividerBlockModel> {
}


