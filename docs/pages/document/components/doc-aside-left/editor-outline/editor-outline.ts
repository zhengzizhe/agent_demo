import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input } from '@angular/core';
import { BlockNodeType, nextTick } from '@ccc/blockcraft';
import * as Y from 'yjs';
import { NgIf } from '@angular/common';

interface OutlineItem {
    id: string;
    name: string;
    depth: number;
    expand: boolean;
    hidden: boolean;
    active?: boolean;
}

@Component({
    selector: 'dl-editor-outline',
    templateUrl: './editor-outline.html',
    styleUrls: ['./editor-outline.scss'],
    standalone: true,
    imports: [
        NgIf
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditorOutlineComponent {
    @Input({ required: true })
    doc: BlockCraft.Doc;

    @Input({ required: true })
    docTitle: string = '';

    @Input({ required: true })
    scrollToBlock: (id: string) => void;

    outline: OutlineItem[] = [];
    headingBlockMap = new Map<string, OutlineItem>();

    observe = (events) => {
        nextTick().then(() => {
            this.dynamicObserve(events);
        });
    };

    constructor(
        readonly cdr: ChangeDetectorRef
    ) {
    }

    searchInsertIndex(insertId: string) {
        // @ts-expect-error
        const rootIds = this.doc.root._childrenIds;
        const startIdx = rootIds.findIndex(id => insertId === id);
        // 比对outline
        for (let i = startIdx; i < rootIds.length; i++) {
            const id = rootIds[i];
            if (!this.headingBlockMap.has(id)) continue;
            return this.outline.findIndex(v => v.id === id);
        }
        return this.outline.length;
    }

    ngOnInit() {
        if (this.doc?.isInitialized) {
            this.initOutline();
            this.bindObserve();
        } else {
            this.doc.afterInit(() => {
                this.initOutline();
                this.bindObserve();
            });
        }
    }

    ngOnDestroy() {
        this.doc.yBlockMap.unobserveDeep(this.observe);
    }

    initOutline() {
        this.doc.root.getChildrenBlocks().forEach((block, i) => {
            if (block.nodeType !== BlockNodeType.editable || ['code'].includes(block.flavour)) return;
            if (typeof block.props['heading'] === 'number' || typeof block.props['heading'] === 'string') {
                const heading = typeof block.props['heading'] === 'string' ? parseInt(block.props['heading']) : block.props['heading'];
                const item = {
                    id: block.id,
                    name: block.textContent(),
                    depth: heading - 1,
                    expand: true,
                    hidden: false
                };
                this.outline.push(item);
                this.headingBlockMap.set(block.id, item);
            }
        });
        this.cdr.markForCheck();
    }

    bindObserve() {
        this.doc.yBlockMap.observeDeep(this.observe);
    }

    deleteItem(id: string) {
        if (!this.headingBlockMap.has(id)) return;
        this.headingBlockMap.delete(id);
        this.outline.splice(this.outline.findIndex(v => v.id === id), 1);
    }

    insertItem(item: OutlineItem) {
        const startIdx = this.searchInsertIndex(item.id);
        item.hidden = this.findParentItem(startIdx, item.depth)?.expand === false;
        this.outline.splice(startIdx, 0, item);
        this.headingBlockMap.set(item.id, item);
    }

    dynamicObserve(events: Y.YEvent<any>[]) {
        for (const { path, changes, target } of events) {

            // at top level, it`s mean that block is created or deleted
            if (!path.length) {
                // 处理删除
                // changes.keys.forEach((change, key) => {
                //     if (change.action === 'delete') {
                //         this.deleteItem(key);
                //     }
                // });

                continue;
            }

            const blockId = path[0] as string;
            const keyProp = path[1];

            if (keyProp === 'children') {

                if (target instanceof Y.Text) {
                    // 处理Text变化
                    if (this.headingBlockMap.has(blockId)) {
                        this.headingBlockMap.get(blockId).name = target.toString();
                    }

                    continue;
                }

                // 处理新增
                if (blockId !== this.doc.rootId) continue;

                if (changes.deleted.size) {
                    Array.from(changes.deleted).reduce((prev, curr) => {
                        prev.push(...curr.content['arr']);
                        return prev;
                    }, []).forEach(id => {
                        this.deleteItem(id);
                    });
                }

                let r = 0;

                for (const change of changes.delta) {
                    if (change.insert) {
                        for (let i = 0; i < change.insert.length; i++) {
                            const id = change.insert[i];
                            const bm = this.doc.yBlockMap.get(id);
                            const heading = bm.get('props').get('heading');
                            if (bm.get('nodeType') !== BlockNodeType.editable || !heading) continue;

                            // 如果已有，说明是替换位置. 直接重新计算
                            if (this.headingBlockMap.has(id)) {
                                this.outline = [];
                                this.initOutline();
                                return;
                            }

                            const item = {
                                id,
                                name: (bm.get('children') as unknown as Y.Text).toString(),
                                depth: <number>heading - 1,
                                expand: true,
                                hidden: false
                            };
                            this.insertItem(item);
                        }
                    } else if (change.retain) {
                        r += change.retain;
                    }
                }
                continue;

            }

            // heading属性变化
            if (keyProp === 'props') {
                if (!changes.keys.has('heading')) continue;

                const heading = (typeof target.get('heading') === 'string' ? parseInt(target.get('heading')) : target.get('heading')) || 0;
                // 已存在
                if (this.headingBlockMap.has(blockId)) {
                    if (heading <= 0) {
                        this.deleteItem(blockId);
                    } else {
                        const item = this.headingBlockMap.get(blockId)!;
                        item.depth = heading - 1;
                    }
                } else {
                    const bm = target.parent;
                    const item: OutlineItem = {
                        id: blockId,
                        name: (bm.get('children') as unknown as Y.Text).toString(),
                        depth: heading - 1,
                        expand: true,
                        hidden: false
                    };
                    this.insertItem(item);
                }
            }
        }

        this.cdr.markForCheck();
    };

    findParentItem(idx: number, depth: number) {
        while (--idx > 0) {
            if (this.outline[idx - 1].depth < depth) return this.outline[idx];
        }
        return null;
    }

    onExpandBtnClick($event: MouseEvent, id: string) {
        $event.stopPropagation();
        let itemIdx = this.outline.findIndex(v => v.id === id);
        const parentItem = this.outline[itemIdx];
        const expand = !parentItem.expand;
        parentItem.expand = expand;

        while (itemIdx < this.outline.length - 1) {
            itemIdx++;
            const item = this.outline[itemIdx];
            if (item.depth <= parentItem.depth) break;
            item.hidden = !expand;
            item.expand = expand;
        }
        this.cdr.markForCheck();
    }

    onItemClick($event: MouseEvent, item: OutlineItem) {
        $event.stopPropagation();
        this.scrollToBlock?.(item.id);
        this.outline.forEach(v => v.active = false);
        item.active = true;
        this.cdr.markForCheck();
    }

    onTitleNavClick() {
        this.outline.forEach(v => v.active = false);
        this.doc.scrollContainer.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }
}
