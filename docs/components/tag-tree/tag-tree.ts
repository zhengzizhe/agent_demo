import { Component, ComponentRef, EventEmitter, Injector, Input, Output, ViewChild } from '@angular/core';
import type { Edge, Node, Layout } from '@swimlane/ngx-graph';
import { NgxGraphModule, GraphComponent } from '@swimlane/ngx-graph';
import * as shape from 'd3-shape';
import { NgForOf, NgIf, NgStyle } from '@angular/common';
import { EntityService, ITag } from '@/pages/docs/services';
import { DagreNodesOnlyLayout } from './customDagreNodesOnly';
import { Overlay } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { TagCreateDialog } from '@/pages/docs/components/tag-tree/tag-create-dialog';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CsesRenderService } from '@ccc/cses-common';
import { DlSpinComponent, DlTagComponent } from '@/pages/docs/components';

@Component({
    selector: 'dl-tag-tree',
    templateUrl: './tag-tree.html',
    styleUrls: ['./tag-tree.scss'],
    standalone: true,
    imports: [NgxGraphModule, NgIf, NgForOf, NgStyle, DlSpinComponent, DlTagComponent]
})
export class DlTagTreeComponent {
    @Input({required: true}) sourceId: string = '';
    @Input() selectAble = false;
    @Input() selectedTagSet = new Set<string>();
    @Input() readonly = false;

    @ViewChild('graphComponent', { read: GraphComponent }) graphComponent!: GraphComponent;

    @Output()
    tagClick = new EventEmitter<ITag>();

    tagTree: ITag;
    flatTagMap: Map<string, ITag> = new Map<string, ITag>();
    isLoading = false;

    public nodes: Node[] = [];
    public links: Edge[] = [];
    public layoutSettings = {
        orientation: 'TB',
        ranker: 'tight-tree'
    };
    public curve: any = shape.curveLinear;
    public layout: Layout = new DagreNodesOnlyLayout();
    canvasCtx = document.createElement('canvas').getContext('2d');

    constructor(
        private overlay: Overlay,
        private entityService: EntityService,
        private render: CsesRenderService
    ) {
        this.canvasCtx.font = '16px \'Arial\', \'Helvetica\', \'sans-serif\'';
        // this.tagTree = {
        //     'isSuccess': true,
        //     'serverName': 'CSES-38deMac-Pro.local',
        //     'id': '68f1b9904f691c141e51cd79',
        //     'name': '中企云链',
        //     'color': '#32f76f',
        //     'sortOrder': 0,
        //     'children': [
        //         {
        //             'id': '68f1b9c04f691c141e51cd7a',
        //             'parentId': '68f1b9904f691c141e51cd79',
        //             'name': '文档库',
        //             'color': '#32f76f',
        //             'sortOrder': 0,
        //             'children': []
        //         },
        //         {
        //             'id': '68f1bac44f691c141e51cd7b',
        //             'parentId': '68f1b9904f691c141e51cd79',
        //             'name': '文档',
        //             'color': '#32f76f',
        //             'sortOrder': 0,
        //             'children': []
        //         },
        //         {
        //             'id': '68f1baca4f691c141e51cd7c',
        //             'parentId': '68f1b9904f691c141e51cd79',
        //             'name': '文件夹',
        //             'color': '#32f76f',
        //             'sortOrder': 0,
        //             'children': []
        //         },
        //         {
        //             'id': '68f1bacf4f691c141e51cd7d',
        //             'parentId': '68f1b9904f691c141e51cd79',
        //             'name': '空间',
        //             'color': '#32f76f',
        //             'sortOrder': 0,
        //             'children': [
        //                 {
        //                     'id': '68f214bde7074c546ffd1381',
        //                     'parentId': '68f1bacf4f691c141e51cd7d',
        //                     'name': '子文档库',
        //                     'color': '#32f76f',
        //                     'sortOrder': 0,
        //                     'children': [
        //                         {
        //                             'id': '68f214c9e7074c546ffd1382',
        //                             'parentId': '68f214bde7074c546ffd1381',
        //                             'name': '子文档库1',
        //                             'color': '#32f76f',
        //                             'sortOrder': 0,
        //                             'children': []
        //                         }
        //                     ]
        //                 }
        //             ]
        //         }
        //     ]
        // };
    }

    async ngOnInit() {
        if (!this.sourceId) return;
        this.isLoading = true;
        const res = await this.entityService.getTagTreeBySpace({ id: this.sourceId });
        delete res.parentId;
        this.tagTree = res;
        this.isLoading = false;

        this.flatAddTagToGraph(this.tagTree);
    }

    ngOnDestroy() {
    }

    forceUpdateGraph() {
        this.graphComponent?.update();
        setTimeout(() => {
            this.graphComponent?.update();
        }, 0);
    }

    flatAddTagToGraph = (tag: ITag, deepLevel = Infinity) => {
        this.addTagToGraph(tag);

        if (tag.children?.length && deepLevel > 0) {
            tag.children.forEach(child => {
                this.flatAddTagToGraph(child, deepLevel - 1);
            });
        }
    };

    addTagToGraph = (tag: ITag, expanded = true) => {
        const node: Node = {
            id: tag.id,
            label: tag.name,
            data: {
                backgroundColor: tag.backColor || '#f3f2f0',
                c: tag.color,
                width: this.canvasCtx.measureText(tag.name).width + 12,
                parentId: tag.parentId,
                expanded,
                hasChild: !!tag.children?.length
            }
        };

        if (tag.parentId) {
            const edge: Edge = {
                source: tag.parentId,
                target: tag.id,
                label: '',
                data: {}
            };
            this.links.push(edge);
        }

        this.nodes.push(node);
        this.flatTagMap.set(tag.id, tag);
    };

    deleteFromGraph = (id: string) => {
        const tag = this.flatTagMap.get(id);
        if (!tag) return;
        const collapsedIds = this.getDescendantsIds(tag!, new Set<string>([id]));
        this.nodes = this.nodes.filter(node => !node.data.parentId || (!collapsedIds.has(node.data.parentId) && node.id !== id));
        this.links = this.links.filter(link => !collapsedIds.has(link.source) && !collapsedIds.has(link.target));

        const parentFlag = this.flatTagMap.get(tag.parentId);
        if (parentFlag) {
            parentFlag.children = parentFlag.children?.filter(v => v.id !== id);
        }
        const parent = this.nodes.find(v => v.id === tag.parentId);
        if (parent) {
            parent.data.hasChild = !!parentFlag?.children?.length;
        }
        this.flatTagMap.delete(id);
    };

    createTagEditDialog = (evt: MouseEvent) => {
        const nodeEle = (evt.target as HTMLElement).closest('.cardContainer');
        const overlayRef = this.overlay.create({
            hasBackdrop: true,
            backdropClass: 'cdk-overlay-transparent-backdrop',
            positionStrategy: this.overlay.position().flexibleConnectedTo(nodeEle).withPositions([
                {
                    originX: 'end',
                    originY: 'top',
                    overlayX: 'start',
                    overlayY: 'top'
                }
            ])
        });
        const cpr: ComponentRef<TagCreateDialog> = overlayRef.attach(new ComponentPortal(TagCreateDialog, null, this.entityService.injector));
        overlayRef.backdropClick().pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(() => {
            overlayRef.dispose();
        });
        return {
            overlayRef,
            cpr
        };
    };

    onNodeAdderClick(evt: MouseEvent, node: Node) {
        const { cpr, overlayRef } = this.createTagEditDialog(evt);
        cpr.instance.create.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(res => {
            const form = {
                ...res,
                parentId: node.id,
                spaceId: this.sourceId
            };
            cpr.setInput('isSpinning', true);
            this.entityService.createTag(form).then(res => {
                overlayRef.dispose();
                form['id'] = res.result;

                const tag = this.flatTagMap.get(node.id);
                if (tag) {
                    tag.children ||= [];
                    tag.children?.push(form as unknown as ITag);
                    node.data.hasChild = !!tag.children?.length;
                }

                this.addTagToGraph(form as unknown as ITag);
                this.forceUpdateGraph()
            }).catch(e => {
                cpr.setInput('isSpinning', false);
                this.render.error(e.error || '标签创建失败');
            });
        });
    }

    getDescendantsIds(tag: ITag, set = new Set<string>()) {
        if (tag.children?.length) {
            tag.children.forEach(child => {
                set.add(child.id);
                this.getDescendantsIds(child, set);
            });
        }

        return set;
    }

    getPaths(tag: ITag, path = [tag.id]) {
        if (tag.parentId) {
            path.unshift(tag.parentId);
            this.getPaths(this.flatTagMap.get(tag.parentId)!, path);
        }
        return path;
    }

    onToggleExpand(node: Node) {
        node.data.expanded = !node.data.expanded;

        const sourceId = node.id;
        const tag = this.flatTagMap.get(sourceId);

        if (node.data.expanded) {
            tag.children?.forEach(child => {
                this.addTagToGraph(child, false);
            });
        } else {
            const collapsedIds = this.getDescendantsIds(tag!, new Set<string>([sourceId]));
            this.nodes = this.nodes.filter(node => !node.data.parentId || !collapsedIds.has(node.data.parentId));
            this.links = this.links.filter(link => !collapsedIds.has(link.source));
        }
        this.forceUpdateGraph()
    }

    onNodeTagClick(evt: MouseEvent, node: Node) {
        if (this.readonly) {
            this.tagClick.emit(this.flatTagMap.get(node.id)!);
            return;
        }

        const { cpr, overlayRef } = this.createTagEditDialog(evt);
        cpr.instance.tag = this.flatTagMap.get(node.id);
        cpr.instance.deleteAble = !!node.data.parentId;
        cpr.instance.delete.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(() => {
            cpr.instance.isSpinning = true;
            this.entityService.deleteTag({ id: node.id, spaceId: this.sourceId }).then(() => {
                overlayRef.dispose();
                this.deleteFromGraph(node.id);
                this.graphComponent.update();
            }).catch(e => {
                cpr.instance.isSpinning = false;
                this.render.error(e.error || '标签删除失败');
            });
        });
        cpr.instance.update.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(form => {
            cpr.instance.isSpinning = true;
            this.entityService.modifyTag({ id: node.id, ...form, spaceId: this.sourceId }).then(() => {
                overlayRef.dispose();
                node.data.c = form.color;
                node.data.backgroundColor = form.backColor
                node.label = form.name;
                node.data.width = this.canvasCtx.measureText(form.name).width + 12;
                const tag = this.flatTagMap.get(node.id);
                if (tag) {
                    tag.name = form.name;
                    tag.color = form.color;
                    tag.backColor = form.backColor
                }

                this.forceUpdateGraph()
            }).catch(e => {
                cpr.instance.isSpinning = false;
                this.render.error(e.error || '标签更新失败');
            });
        });
    }

    getSelectedTags() {
        return Array.from(this.selectedTagSet).map(id => this.flatTagMap.get(id)!);
    }
}
