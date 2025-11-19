import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { DlButton, DlTagTreeComponent } from '@/pages/docs/components';
import { NgIf } from '@angular/common';
import { ITag } from '@/pages/docs/services';

@Component({
    selector: 'dl-tags-selector',
    template: `
        <dl-tag-tree [readonly]="true" [sourceId]="spaceId" [selectedTagSet]="selectedSet"
                     (tagClick)="onTagClick($event)" #tagTreeComponent
                     *ngIf="spaceId"></dl-tag-tree>
        <div class="btn-group">
            <button dl-button type="simple" (click)="close.emit()">取消</button>
            <button dl-button type="primary" (click)="onSure()">确定 @if(selectedSet.size) {({{ selectedSet.size }})}
            </button>
        </div>
    `,
    styles: [`
        :host {
            display: block;
            width: clamp(520px, 65vw, 780px);
            background: var(--cs-page-bg-color);
            box-shadow: var(--cs-box-shadow-2);
            border-radius: var(--cs-border-radius);

            dl-tag-tree {
                height: clamp(300px, 50vh, 460px);
            }

            .btn-group {
                margin-top: 8px;
                display: flex;
                justify-content: flex-end;
                gap: 8px;
                padding: 16px 32px;
            }
        }
    `],
    imports: [
        DlTagTreeComponent,
        NgIf,
        DlButton
    ],
    standalone: true,
})
export class DlTagsSelectorDialog {
    @Input({ required: true })
    spaceId: string = '';

    @Input()
    set selectedTags(tags: ITag[]) {
        this.selectedSet = new Set<string>(tags.map(tag => tag.id));
    }

    @Output()
    selectedChange = new EventEmitter<ITag[]>();

    @Output()
    close = new EventEmitter<void>();

    @ViewChild('tagTreeComponent') tagTree: DlTagTreeComponent;

    selectedSet = new Set<string>();

    onTagClick(evt: ITag) {
        if (this.selectedSet.has(evt.id)) {
            this.selectedSet.delete(evt.id);
        } else {
            // evt['path'] ??= this.tagTree.getPaths(evt);
            this.selectedSet.add(evt.id);
        }
    }

    onSure() {
        if (!this.tagTree) return;
        this.selectedChange.emit(this.tagTree.getSelectedTags());
    }
}
