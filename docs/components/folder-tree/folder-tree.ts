import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, Input } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { EntityService, EntityType, IEntity, IFolderEntity, RouterService } from '../../services';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';

interface IFolderItem {
    parentId: string;
    id: string;
    origin: IFolderEntity;
    children: IFolderItem[];
    depth: number;
    expanded?: boolean;
    loading?: boolean;
}

@Component({
    selector: 'folder-tree',
    templateUrl: 'folder-tree.html',
    styleUrls: ['folder-tree.scss'],
    imports: [
        NgTemplateOutlet
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FolderTreeComponent {

    private _spaceId = '';

    @Input({ required: true })
    set spaceId(id: string) {
        if (!id) return;
        this._spaceId = id;
        this.resetFolderList();
    }

    get spaceId() {
        return this._spaceId;
    }

    private _activeFolderId = '';
    @Input()
    set activeFolderId(id: string) {
        this._activeFolderId = id;
        this.cdr.detectChanges();
    }

    get activeFolderId() {
        return this._activeFolderId;
    }

    folderList: IFolderItem[] = [];

    flatFolderMap = new Map<string, IFolderItem>();

    constructor(
        private entityService: EntityService,
        protected router: RouterService,
        private cdr: ChangeDetectorRef,
        private destroyRef: DestroyRef
    ) {
    }

    ngOnInit() {
        this.entityService.entitiesUpdate$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(update => {
            console.log('🎄 ~~~~~~~~~~~~~folder-tree update', update);
            // 如果是删除，从目录树中删除
            if (update.type === 'delete') {
                const folderEntitiesIdList = update.entities.filter(v => v.nodeType === EntityType.Folder).map(v => v.id)
                if (!folderEntitiesIdList.length) return;
                this.removeItemsFrom(folderEntitiesIdList, update.parentId)
                this.cdr.markForCheck();
                return;
            }
            if (update.type === 'new') {
                const parentFolder = this.flatFolderMap.get(update.parentId);
                const folderItems = update.entities.filter(v => v.nodeType === 'folder').map(v => this._transform(v, parentFolder));
                if(!folderItems.length) return;
                this.addItemsTo(folderItems, update.parentId)
                this.cdr.markForCheck();
                return;
            }
            if (update.type === 'move') {
                const folderEntities = update.entities.filter(v => v.nodeType === EntityType.Folder);
                if (!folderEntities.length) return;
                // 先移除
                folderEntities.forEach(entity => {
                    const treeItem = this.flatFolderMap.get(entity.id);
                    this.removeItemsFrom([entity.id], treeItem?.parentId || this.spaceId);
                });

                const targetFolder = this.flatFolderMap.get(update.targetId);
                const folderItems = folderEntities.map(v => this._transform(v, targetFolder));
                this.addItemsTo(folderItems, update.targetId);
                this.cdr.markForCheck();
                return
            }
            if(update.type === 'update') {
                const folderEntities = update.entities.filter(v => v.nodeType === EntityType.Folder);
                if (!folderEntities.length) return;
                folderEntities.forEach(entity => {
                    const treeItem = this.flatFolderMap.get(entity.id);
                    if(treeItem) {
                        treeItem.origin[update.filed] = entity[update.filed];
                    }
                })
                this.cdr.markForCheck();
            }
        });
    }

    private removeItemsFrom(items: string[], parentId: string) {
        if (parentId === this.spaceId) {
            this.folderList = this.folderList.filter(v => !items.includes(v.id));
        } else {
            const parentFolder = this.flatFolderMap.get(parentId);
            if (parentFolder) {
                parentFolder.expanded && (parentFolder.children = parentFolder.children.filter(v => !items.includes(v.id)));
                parentFolder.origin.subFolderCount -= items.length;
            }
        }
        items.forEach(e => {
            this.flatFolderMap.delete(e);
        })
    }

    private addItemsTo(items: IFolderItem[], parentId: string) {
        if (parentId === this.spaceId) {
            this.folderList.push(...items);
            this.folderList = this.folderList.sort((a, b) => b.origin.createdAt - a.origin.createdAt)
        } else {
            const parentFolder = this.flatFolderMap.get(parentId);
            if (parentFolder) {
                parentFolder.origin.subFolderCount += items.length;

                if (parentFolder.expanded) {
                    parentFolder.children.push(...items);
                    parentFolder.children = parentFolder.children.sort((a, b) => b.origin.createdAt - a.origin.createdAt)
                }
            }
        }
        items.forEach(e => {
            this.flatFolderMap.set(e.id, e);
        })
    }

    async toggleFolder(folder: IFolderItem, evt: MouseEvent) {
        evt.stopPropagation();
        // 三级以后展开弹窗
        // if(folder.depth >= 2) {
        //   return
        // }
        if (folder.expanded) {
            folder.expanded = false;
            folder.children = [];
        } else {
            await this.expandFolder(folder);
        }
        this.cdr.markForCheck();
    }

    async resetFolderList() {
        this.folderList = [];
        this.flatFolderMap.clear();
        this.folderList = await this.queryFolders(this.spaceId);
        this.cdr.markForCheck();
    }

    async expandFolder(folder: IFolderItem) {
        folder.loading = true;
        folder.children = await this.queryFolders(folder.id);
        folder.loading = false;
        folder.expanded = true;
        this.cdr.markForCheck();
    }

    private _transform(v: IEntity, parent?: IFolderItem): IFolderItem {
        return {
            parentId: parent?.id || this.spaceId,
            id: v.id,
            origin: <IFolderEntity>v,
            children: [],
            depth: (parent?.depth ?? -1) + 1
        };
    }

    async queryFolders(id: string): Promise<IFolderItem[]> {
        const res = await this.entityService.getFolderList({ id });
        const parentFolder = this.flatFolderMap.get(id);
        const items = (res.items || []).map(item => {
            const _ = this._transform(item, parentFolder);
            this.flatFolderMap.set(item.id, _);
            return _;
        });
        return items;
    }

    navToFolder(folder: any, $event: MouseEvent) {
        $event.stopPropagation();
        this.router.navigate(['space', this.spaceId, 'folder', folder.origin.id]);
    }

}
