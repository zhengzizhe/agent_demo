import { Component, DestroyRef, EventEmitter, Input, Output } from '@angular/core';
import {
    EntityService, EntityType, EntityTypeName, IDlBaseDialog,
    IDocDetail, IDocumentEntity,
    IEntity,
    IFolderDetail, IFolderEntity, ITag,
    PermissionRole,
    RouterService
} from '@/pages/docs/services';
import { CheckBtn, DlTagComponent, DlUserProfileComponent, INavItem } from '@/pages/docs/components';
import { Date2Pipe } from '@/pages/docs/pipes';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { CsesRenderService } from '@ccc/cses-common';

@Component({
    selector: 'entity-detail-dialog',
    templateUrl: './entity-detail-dialog.html',
    styleUrls: ['./entity-detail-dialog.scss'],
    imports: [
        CheckBtn,
        Date2Pipe,
        DlUserProfileComponent,
        FormsModule,
        NgIf,
        DlTagComponent
    ],
    host: {
        'class': 'dl-dialog'
    },
    standalone: true
})
export class EntityDetailDialogComponent implements IDlBaseDialog {
    @Input({ required: true })
    entityDetail: IFolderEntity | IDocumentEntity;

    @Input({ required: true })
    localUserRole: PermissionRole;

    @Input({ required: true })
    entityType: EntityType;

    @Output()
    onClose = new EventEmitter<void>();

    @Output()
    onAddTag = new EventEmitter<void>();

    @Output()
    detailChange = new EventEmitter<Partial<IFolderDetail | IDocDetail>>();

    paths: IEntity[] = [];
    tags: ITag[] = []

    constructor(
        private routerService: RouterService,
        private entityService: EntityService,
        private render: CsesRenderService,
        readonly destroyRef: DestroyRef
    ) {
    }

    entityName: string = '';
    entityDesc = '';

    isLocked = false;

    ngOnInit() {
        this.entityName = this.entityDetail.name;
        this.entityDesc = this.entityDetail['description'] || '';
        this.entityService.getEntityPath(this.entityDetail.id, this.entityDetail.spaceId).then(res => {
            this.paths = res.items;
        });
        this.entityService.getTagsByEntity(this.entityDetail).then(res => {
            this.tags = res.items
        })
    }

    onNavToPath(path: INavItem['origin']) {
        this.onClose.emit();
        this.routerService.navigateByUri(path.route);
    }

    async onNameInputBlur() {
        if (this.entityDetail.name === this.entityName) return;
        if (!this.entityName) {
            this.render.warning('名称不能为空');
            this.entityName = this.entityDetail.name;
            return;
        }
        if (this.entityType === 'doc') {
            await this.entityService.modifyDocMeta(this.entityDetail.id, { name: this.entityName });
        } else {
            await this.entityService.modifyFolderMeta(this.entityDetail.id, { name: this.entityName });
        }
        this.entityDetail.name = this.entityName;
        this.detailChange.emit({
            name: this.entityName
        });
        this.render.info('名称已修改');
        this.entityService.entitiesUpdate$.next({
            type: 'update',
            entities: [this.entityDetail],
            filed: 'name'
        });
    }

    onDescInputBlur() {
        if (!this.entityDetail.description && !this.entityDesc) return;
        if (this.entityDetail.description === this.entityDesc) return;

        this.entityService.modifyFolderMeta(this.entityDetail.id, { description: this.entityDesc }).then(() => {
            this.entityDetail.description = this.entityDesc;
            this.render.info('文件夹描述已修改');
        });
    }

    protected readonly PermissionRole = PermissionRole;
    protected readonly EntityType = EntityType;
    protected readonly EntityTypeName = EntityTypeName;
}
