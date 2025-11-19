import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { CheckBtn } from '@/pages/docs/components';
import { EntityService, IEntity } from '@/pages/docs/services';
import { MatIcon } from '@angular/material/icon';
import { NgTemplateOutlet } from '@angular/common';
import { FolderTreeViewNodeComponent } from '@/pages/docs/components/folder-tree-view/folder-tree-view-node';
import { Subject } from 'rxjs';

export interface ITreeNode {
    origin: IEntity;
    id: string;
    parentId: string;
    children: ITreeNode[];
    depth: number;
    hasChild?: boolean;
    isSelected?: boolean;
    isExpanded?: boolean;
    isLoading?: boolean;
    isDisabled?: boolean;
    isLockedDescendantsDisabled?: boolean;
}

@Component({
    selector: 'dl-folder-tree-view',
    templateUrl: 'folder-tree-view.html',
    styleUrls: ['folder-tree-view.scss'],
    imports: [
        CheckBtn,
        MatIcon,
        NgTemplateOutlet,
        FolderTreeViewNodeComponent
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlFolderTreeView {
    private _parentEntity: IEntity;

    @Input({ required: true })
    set parentEntity(val: IEntity) {
        if (val !== this._parentEntity) {
            this._parentEntity = val;
            this.init();
        }
    }

    get parentEntity() {
        return this._parentEntity;
    }

    @Input()
    draggable = true;

    @Input()
    useCheckBtn = false;

    @Input()
    multiSelectable = false;

    @Input()
    set disabledIds(ids: Set<string>) {
        this._disabledIds = ids;
        this._disabledIds.forEach(id => this.setDisabled(id, true, false));
    }
    get disabledIds() {
        return this._disabledIds;
    }

    @Input()
    set lockedChildrenDisabledIds(ids: Set<string>) {
        this._lockedChildrenDisabledSet = ids;
        this._lockedChildrenDisabledSet.forEach(id => this.setDisabled(id, true, true));
    }
    get lockedChildrenDisabledIds() {
        return this._lockedChildrenDisabledSet;
    }

    @Output()
    selectedChange = new EventEmitter<ITreeNode[]>();

    constructor(
        readonly entityService: EntityService,
        readonly cdr: ChangeDetectorRef
    ) {
    }

    nodes: ITreeNode[] = [];
    flattenedNodes = new Map<string, ITreeNode>();

    selectedNodes = new Set<ITreeNode>();

    cancelToken = new Subject();

    private _disabledIds = new Set<string>();
    // 锁定子节点
    private _lockedChildrenDisabledSet = new Set<string>();

    ngOnInit() {
    }

    setDisabled(id: string, disabled: boolean, deep = false) {
        if (!disabled) {
            this._lockedChildrenDisabledSet.delete(id);
            this._disabledIds.delete(id)
        } else {
            deep && this._lockedChildrenDisabledSet.add(id);
            this._disabledIds.add(id);
        }

        const node = this.flattenedNodes.get(id);
        if (node) {
            node.isDisabled = disabled;
            if (deep) {
                node.isLockedDescendantsDisabled = disabled;
                node.children.forEach(e => this.setDisabled(e.id, disabled, deep));
            }
        }
    }

    init() {
        this.nodes = [];
        this.flattenedNodes.clear();
        this.queryFolderList(null).then(nodes => {
            this.nodes = nodes;
        });
    }

    async queryFolderList(parent: ITreeNode | null) {
        const res = await this.entityService.getFolderList({ id: parent?.id || this.parentEntity.id }, this.cancelToken);
        const nodes = res.items.map(entity => {
            entity['hasChildren'] = !!entity.subFolderCount
            const item = this.transformer(entity, parent);
            this.flattenedNodes.set(item.id, item);
            return item;
        });
        this.cdr.markForCheck();
        return nodes;
    }

    transformer = (entity: IEntity, parent: ITreeNode | null) => {
        if(this._lockedChildrenDisabledSet.has(parent?.id)) {
            this._lockedChildrenDisabledSet.add(entity.id);
            this._disabledIds.add(entity.id)
        }
        return {
            origin: entity,
            id: entity.id,
            parentId: parent?.id,
            children: [],
            depth: (parent?.depth ?? -1) + 1,
            hasChild: entity['hasChildren'] ?? true,
            isDisabled: this._disabledIds.has(entity.id),
            isLockedDescendantsDisabled: this._lockedChildrenDisabledSet.has(entity.id)
        } as ITreeNode;
    };

    onToggleExpand(node: ITreeNode) {
        if (node.isExpanded) {
            node.children = [];
            node.isExpanded = false;
            this.cdr.markForCheck();
            return;
        }

        node.isLoading = true;
        this.cdr.markForCheck();
        this.queryFolderList(node).then(nodes => {
            node.children = nodes;
            node.hasChild = !!nodes.length;
            node.isLoading = false;
            node.isExpanded = true;
            this.cdr.markForCheck();
        });
    }

    onNodeClick(node: ITreeNode) {
        if (node.isSelected) {
            node.isSelected = false;
            this.selectedNodes.delete(node);
            // this.cdr.markForCheck();
        } else {
            if (!this.multiSelectable) this.clearSelection();
            node.isSelected = true;
            this.selectedNodes.add(node);
            // this.cdr.markForCheck();
        }
        this.selectedChange.emit(Array.from(this.selectedNodes));
    }

    clearSelection() {
        this.selectedNodes.forEach(n => n.isSelected = false);
        this.selectedNodes.clear();
    }

    getNodePath(node: ITreeNode) {
        const res: ITreeNode[] = [node];
        let _node = node;
        while (_node.parentId) {
            _node = this.flattenedNodes.get(_node.parentId)!;
            res.unshift(_node);
        }
        return res;
    }
}
