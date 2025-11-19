import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    EventEmitter,
    Input,
    Output, ViewChild
} from '@angular/core';
import {
    EntityService,
    EntityType,
    EntityTypeName,
    IDlBaseDialog,
    IEntity, ISpaceEntity,
    RouterService
} from '@/pages/docs/services';
import { CsesRenderService } from '@ccc/cses-common';
import { DlButton, DlFolderTreeView, DlSpinMaskComponent, ITreeNode } from '@/pages/docs/components';
import { NgIf } from '@angular/common';

@Component({
    selector: 'dl-entity-move-dialog',
    templateUrl: 'entity-move-dialog.html',
    styleUrls: ['entity-move-dialog.scss'],
    standalone: true,
    imports: [
        DlFolderTreeView,
        NgIf,
        DlButton,
        DlSpinMaskComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlEntityMoveDialog implements IDlBaseDialog {
    @Input({ required: true })
    entityList: IEntity[] = [];

    @Input({ required: true })
    entityType: EntityType = EntityType.Document;

    @Input({ required: true })
    isLoading = false;

    @Output()
    onClose = new EventEmitter<void>();

    @Output()
    onOk = new EventEmitter<IEntity[]>();

    @ViewChild('folderTreeView', { read: DlFolderTreeView }) folderTreeView: DlFolderTreeView;

    constructor(
        private routerService: RouterService,
        private entityService: EntityService,
        private render: CsesRenderService,
        readonly destroyRef: DestroyRef,
        private cdr: ChangeDetectorRef
    ) {
    }

    activeSpace: ISpaceEntity;
    spaceList: ISpaceEntity[] = [];

    selectedFolders: ITreeNode[] = [];
    disabledIds = new Set<string>();
    lockedDisabledIds = new Set<string>();

    get selectedEntity() {
        return this.selectedFolders[0] || this.activeSpace;
    }

    async ngOnInit() {
        try {
            if (this.entityList.length === 1) {
                const curId = this.entityList[0].id;
                const path = await this.entityService.getEntityPath(curId, this.entityList[0].spaceId);
                // 禁用自己和上一级
                const parentId = path.items.at(-2)?.id;
                this.disabledIds.add(parentId);
                this.disabledIds.add(curId);
                this.lockedDisabledIds.add(curId);
            }
        } catch (e) {
            // ignore
        }

        if (!this.entityService.spaceList.length) {
            await this.entityService.getSpaceList();
        }
        this.spaceList = this.entityService.spaceList;
        this.activeSpace = this.spaceList.find(v => v.id === this.entityList[0].spaceId);

        this.cdr.markForCheck();
    }

    protected readonly EntityType = EntityType;

    toggleSpace(item: ISpaceEntity) {
        this.activeSpace = item;
    }

    onSure() {
        if (!this.selectedEntity || this.disabledIds.has(this.selectedEntity.id)) return;
        this.render.warnConfirm({
            title: '提示',
            content: `移动后，当前文档将继承目标位置的协作者和权限配置，原先继承的协作者将无法访问。`,
            beforeOpen: () => {
            },
            buttons: [
                {
                    label: '确定',
                    type: 'primary',
                    onClick: () => {
                        this.onOk.emit(
                            this.selectedFolders.length
                                ? [this.activeSpace, ...this.folderTreeView.getNodePath(this.selectedFolders[0]).map(v => v.origin)]
                                : [this.activeSpace]
                        );
                    }
                }
            ]
        });
    }

    onSelectedChange($event: ITreeNode[]) {
        this.selectedFolders = $event;
    }
}
