import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { DlTabComponent, IDlTabItem } from '@/pages/docs/components';
import {
    EditorFindReplaceComponent
} from './editor-find-replace/editor-find-replace';
import { EditorOutlineComponent } from './editor-outline/editor-outline';
import { EditorResourceComponent } from './editor-resource/editor-resource';
import { NgIf } from '@angular/common';
import { IDocDetail } from '@/pages/docs/services';

const asideTabs: IDlTabItem[] = [
    {
        id: 'outline',
        label: '',
        icon: 'bc_liebiao',
        name: '目录'
    },
    {
        id: 'task',
        label: '',
        name: '任务',
        icon: 'bc_calendar-minus'
    },
    {
        id: 'resource',
        label: '',
        name: '资源',
        icon: 'bc_fuzhilianjie'
    },
    {
        id: 'search',
        label: '',
        name: '搜索/替换',
        icon: 'bc_file-search-02'
    }
];

@Component({
    selector: 'doc-aside-left',
    templateUrl: 'doc-aside-left.html',
    styleUrls: ['doc-aside-left.scss'],
    imports: [
        DlTabComponent,
        EditorFindReplaceComponent,
        EditorOutlineComponent,
        EditorResourceComponent,
        NgIf
    ],
    standalone: true,
})
export class DocAsideLeftComponent {
    @Input()
    isShowAside = false;

    @Input({required: true})
    docDetail: IDocDetail

    @Input({ required: true })
    doc: BlockCraft.Doc;

    @Input({required: true})
    scrollToBlock: (id: string) => void;

    @Output()
    isShowAsideChange = new EventEmitter<boolean>();

    asideTabs = asideTabs;

    @Input()
    activeAsideTabIdx = 0;

    @Output()
    activeAsideTabIdxChange = new EventEmitter<number>();

    onExpandAsideChange(isShowAside: boolean) {
        this.isShowAsideChange.emit(this.isShowAside = isShowAside);
    }
}
