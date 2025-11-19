import { ChangeDetectorRef, Component, DestroyRef, EventEmitter, Input, Output } from '@angular/core';
import { EntityService, IDlBaseDialog, IEntity, ITag } from '@/pages/docs/services';
import { NgIf } from '@angular/common';
import { DlSpinMaskComponent, DlTagComponent, DlTagTreeComponent } from '@/pages/docs/components';

@Component({
    selector: 'dl-tag-bind-dialog',
    templateUrl: 'tag-bind-dialog.html',
    styleUrls: ['tag-bind-dialog.scss'],
    imports: [
        DlTagComponent,
        NgIf,
        DlTagTreeComponent,
        DlSpinMaskComponent
    ],
    standalone: true
})
export class DlTagBindDialog implements IDlBaseDialog {
    @Input()
    entity: IEntity;

    @Output()
    onClose = new EventEmitter();

    tags: ITag[] = [];
    isLoading = false;

    selectedTagSet = new Set<string>();

    constructor(
        readonly destroyRef: DestroyRef,
        private cdr: ChangeDetectorRef,
        private entityService: EntityService
    ) {
    }

    ngOnInit() {
        this.entityService.getTagsByEntity({ id: this.entity.id }).then(res => {
            this.tags = res.items;
            this.selectedTagSet = new Set(this.tags.map(v => v.id));
        });
    }

    onTagClick(evt: ITag) {
        if (this.selectedTagSet.has(evt.id)) {
            this.unbind(evt);
        } else {
            this.bind(evt);
        }
    }

    bind(tag: ITag) {
        if (this.isLoading) return;
        this.isLoading = true;
        this.entityService.bindTag({ id: this.entity.id, tagId: tag.id }).then(res => {
            this.tags.push(tag);
            this.selectedTagSet.add(tag.id);
        }).catch(e => {
            this.entityService.render.error(e.error || '添加标签失败');
        }).finally(() => {
            this.isLoading = false;
        });
    }

    unbind(tag: ITag) {
        if (this.isLoading) return;
        this.isLoading = true;
        this.entityService.unbindTag({ id: this.entity.id, tagId: tag.id }).then(res => {
            this.tags = this.tags.filter(v => v.id !== tag.id);
            this.selectedTagSet.delete(tag.id);
        }).catch(e => {
            this.entityService.render.error(e.error || '删除标签失败');
        }).finally(() => {
            this.isLoading = false;
        });
    }
}
