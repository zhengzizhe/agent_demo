import { Component, DestroyRef, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';
import {
    BlockNodeType, debounce, DeltaOperation,
    EditableBlockComponent,
    FakeRange,
    nextTick,
    performanceTest,
    STR_ZERO_WIDTH_SPACE,
    deltaToString
} from '@ccc/blockcraft';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { DlButton } from '@/pages/docs/components';
import { AsyncPipe, NgIf } from '@angular/common';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';

interface IMatched {
    block: BlockCraft.BlockComponent,
    index: number,
    length: number,
    fakeRange: FakeRange
}

@Component({
    selector: 'dl-editor-find-replace',
    templateUrl: 'editor-find-replace.html',
    styleUrls: ['editor-find-replace.scss'],
    imports: [
        FormsModule,
        DlButton,
        NgIf,
        AsyncPipe,
        NzTooltipDirective
    ],
    standalone: true
})
export class EditorFindReplaceComponent {
    @Input({ required: true })
    doc!: BlockCraft.Doc;

    @ViewChild('findInput', { read: ElementRef }) findInput!: ElementRef<HTMLInputElement>;

    findText = '';
    replaceText = '';

    matchIndex = 0;
    matchedList: IMatched[] = [];

    matchedBlockMap = new Map<string, IMatched[]>();

    regFlag = 'g';
    matchReg: RegExp = new RegExp(this.findText, this.regFlag);
    regFlagList = [
        // { name: 'g', value: 'g', checked: true, title: '是否整段匹配' },
        { name: 'i', value: 'i', checked: false, title: '是否忽略大小写' }
        // { name: 'm', value: 'm', checked: false, title: '多行匹配' }
    ];

    constructor(
        private destroyRef: DestroyRef
    ) {
    }

    ngOnInit() {
        // 检测新增和删除节点变化
        this.doc.onChildrenUpdate$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(evt => {
            if (!this.findText) return;
            this.cancelHighlight();

            nextTick().then(() => {

                evt.transactions.forEach(t => {
                    if (t.deleted) {
                        const parentBlock = t.block;
                        const childIds = parentBlock.childrenIds;
                        // 如果有已匹配的block的父block是有删除操作的block, 需要删除已匹配的
                        for (const m of this.matchedBlockMap.values()) {
                            if (m[0].block.parentId === parentBlock.id && !childIds.includes(m[0].block.id)) {
                                this.clearOldMatchesMark(m[0].block.id);
                            }
                        }
                    }

                    if (t.inserted) {
                        t.inserted.forEach(block => {
                            if (!this.doc.isEditable(block)) return;

                            // 如果是新增的元素，需要重新查找
                            const matches = this._matchBlockText(block);
                            if (!matches?.length) return;
                            this.matchedBlockMap.set(block.id, matches);
                        });
                    }
                });

                this.resortMatches();
                this.highlightCurrent(false);

            });

        });

        // 检测文本变化
        this.doc.onTextUpdate$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(evt => {
            if (!this.findText) return;

            if (!this.matchedList.length && !this.matchedBlockMap.size) {
                this.findAll();
                return;
            }

            this.cancelHighlight();
            nextTick().then(() => {
                evt.transactions.forEach(t => {
                    const block = t.block;
                    this.clearOldMatchesMark(block.id);
                    this._matchBlockText(block);
                });
                this.resortMatches();
                this.highlightCurrent(false);
            });
        });
    }

    ngAfterViewInit() {
        this.findInput.nativeElement.focus();
    }

    ngOnDestroy() {
        this.clearAll();
    }

    onFlagChange(item: typeof this.regFlagList[number]) {
        this.clearAll()
        item.checked = !item.checked;
        this.regFlag = 'g' + this.regFlagList.filter(i => i.checked).map(i => i.value).join('');
        this.matchReg = new RegExp(this.findText, this.regFlag);
        this.findAll();
    }

    private _matchBlockText = (block: EditableBlockComponent) => {
        const text = deltaToString(block.textDeltas(), STR_ZERO_WIDTH_SPACE);
        if (!text) return null;
        const matches = text.matchAll(this.matchReg);
        const res: typeof this.matchedList[number][] = [];
        for (const match of matches) {
            const fakeRange = this.doc.selection.createFakeRange({
                from: {
                    blockId: block.id,
                    index: match.index,
                    length: this.findText.length,
                    type: 'text'
                },
                to: null
            }, { bgColor: 'rgba(255, 198, 10, 0.4)' });

            res.push({
                fakeRange,
                index: match.index,
                length: this.findText.length,
                block: block
            });
        }
        if (res.length) {
            this.matchedBlockMap.set(block.id, res);
        }
        return res;
    };

    clearOldMatchesMark(id: string) {
        const matches = this.matchedBlockMap.get(id);
        if (!matches) return;
        matches.forEach(m => m.fakeRange.destroy());
        this.matchedBlockMap.delete(id);
    }

    resortMatches() {
        // 重新排序，先DFS排出已匹配节点顺序
        const ids: string[] = [];
        const find = (b: BlockCraft.BlockComponent) => {
            const childIds = b.childrenIds;
            for (const childId of childIds) {
                if (this.matchedBlockMap.has(childId)) {
                    ids.push(childId);
                    continue;
                }
                const b = this.doc.getBlockById(childId);
                if (b.nodeType === BlockNodeType.block) {
                    find(b);
                }
            }
        };
        find(this.doc.root);

        const matchedList: IMatched[] = [];
        for (const id of ids) {
            const list = this.matchedBlockMap.get(id);
            if (list) matchedList.push(...list);
        }
        this.matchedList = matchedList;
    }

    clearAll() {
        this.matchIndex = 0;
        this.matchedList.forEach(m => {
            m.fakeRange.destroy();
        });
        this.matchedList = [];
    }

    @performanceTest()
    findAll() {
        if (!this.findText) return;

        const find = (b: BlockCraft.BlockComponent) => {
            b.getChildrenBlocks().forEach(b => {
                if (b.nodeType === 'void') return;
                if (b.nodeType === 'editable') {
                    const matches = this._matchBlockText(b as EditableBlockComponent);
                    matches?.length && this.matchedList.push(...matches);
                } else {
                    find(b);
                }
            });
        };
        find(this.doc.root);

        if (this.matchedList.length) {
            this.highlightCurrent();
        }
    }

    findNext() {
        this.findByStep(1);
    }

    findPrev() {
        this.findByStep(-1);
    }

    findByStep(step: 1 | -1) {
        if (!this.matchedList.length) return;
        this.cancelHighlight();
        this.matchIndex += step;
        if (this.matchIndex >= this.matchedList.length) {
            this.matchIndex = 0;
        }
        if (this.matchIndex < 0) {
            this.matchIndex = this.matchedList.length - 1;
        }
        this.highlightCurrent();
    }

    cancelHighlight() {
        this.matchedList[this.matchIndex]?.fakeRange.setColor({ bgColor: 'rgba(255, 198, 10, 0.4)' });
    }

    highlightCurrent(withScroll = true) {
        if (!this.matchedList.length) return;
        if (this.matchIndex >= this.matchedList.length) {
            this.matchIndex = this.matchedList.length - 1;
        }
        const match = this.matchedList[this.matchIndex];
        match.fakeRange.setColor({ bgColor: 'rgba(245, 74, 69, .4)' });
        withScroll && match.block.hostElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'center' });
    }

    replaceOne() {
        const match = this.matchedList[this.matchIndex];
        this.doc.crud.transact(() => {
            this._replace(match);
        });
        // this.matchedList.splice(this.matchIndex, 1)
        // if (this.matchedList.length) {
        //   this.highlightCurrent()
        // }
    }

    private _replace(match: IMatched) {
        if (!this.doc.isEditable(match.block)) return;
        match.block.yText.delete(match.index, match.length);
        match.block.yText.insert(match.index, this.replaceText);
        // match.block.replaceText(match.index, match.length, this.replaceText)
        // match.fakeRange.destroy()
    }

    replaceAll() {
        this.doc.crud.transact(() => {
            this.matchedBlockMap.forEach((matched, bid) => {
                const delta: DeltaOperation[] = [];
                let r = 0;
                // 将不连续匹配组装成delta
                matched.forEach(m => {
                    delta.push({
                        retain: m.index - r
                    });
                    delta.push({
                        delete: m.length
                    });
                    delta.push({
                        insert: this.replaceText
                    });
                    r = m.index + m.length;
                });
                const block = this.doc.getBlockById(bid);
                if (!this.doc.isEditable(block)) return;
                block.applyDeltaOperations(delta);
            });
            this.matchedList.forEach(v => {
                v.fakeRange.destroy();
            });
            this.matchedList = [];
            this.matchedBlockMap.clear();
        });
    }

    timer: any;
    onFindTextChange = debounce(() => {
        if (this.timer) {
            clearTimeout(this.timer);
            this.timer = null;
        }
        this.clearAll();
        if (!this.findText) return;
        if (this.findText.length < 3) {
            this.timer = setTimeout(() => {
                this.matchReg = new RegExp(this.findText, this.regFlag);
                this.findAll();
            }, 1000);
        } else {
            this.matchReg = new RegExp(this.findText, this.regFlag);
            this.findAll();
        }
    }, 500);


}
