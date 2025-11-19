import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { IDocSettingMenuItem } from '@/pages/docs/pages/document/components/doc-setting-menu/type';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { NgTemplateOutlet } from '@angular/common';
import { NzPopoverDirective } from 'ng-zorro-antd/popover';
import { DocService, IDocConfigs, IDocDetail, PermissionRole } from '@/pages/docs/services';
import { Date2Pipe } from '@/pipes/date2.pipe';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';

@Component({
    selector: 'doc-setting-menu',
    templateUrl: 'doc-setting-menu.html',
    styleUrls: ['doc-setting-menu.scss'],
    standalone: true,
    imports: [
        NzDropDownDirective,
        NzDropdownMenuComponent,
        NgTemplateOutlet,
        NzPopoverDirective,
        NzTooltipDirective
    ],
    host: {
        class: 'dl-dropdown-menu'
    },
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocSettingMenuComponent {
    @Input({ required: true })
    configs!: IDocConfigs;

    @Input({ required: true })
    role: PermissionRole = PermissionRole.reader;

    private _docDetail: IDocDetail;
    @Input({ required: true })
    set docDetail(val: IDocDetail) {
        if (!val) return;
        this._docDetail = val;
    }

    get docDetail() {
        return this._docDetail;
    }

    @Output()
    readonly menuClick = new EventEmitter<IDocSettingMenuItem>();

    menuItems: IDocSettingMenuItem[] = MENU_ITEMS;

    constructor(
        private docService: DocService,
        readonly cdr: ChangeDetectorRef
    ) {
    }

    ngOnInit() {
        this.menuItems = this.menuItems.filter(item => item.role <= this.role);
        if (this.role < PermissionRole.manager) return;
        this.docService.getVersionList(this.docDetail.id).then(res => {
            if (!res.items.length) return;
            const prevIndex = this.menuItems.findIndex(item => item.id === 'saveVersion');
            if (prevIndex < 0) return;
            const item = structuredClone(VERSION_LIST_MENU_ITEM);
            item.children = res.items.map(item => ({
                id: item.id,
                name: 'version',
                label: item.name,
                role: PermissionRole.manager,
                value: item.version,
                type: 'none',
                tooltip: `${item.publisher?.name} 保存于${Date2Pipe.prototype.transform(item.publishedAt)}`
            } as IDocSettingMenuItem));
            this.menuItems.splice(prevIndex + 1, 0, item);
            this.cdr.markForCheck();
        });
    }

    itemClick(item: IDocSettingMenuItem) {
        this.menuClick.emit(item);
    }

}

const MENU_ITEMS: IDocSettingMenuItem[] = [
    {
        id: 'pageWidth',
        name: 'pageWidth',
        label: '页宽设置',
        icon: 'bc_yekuanshezhi1',
        value: 'pageWidth',
        type: 'dropdown',
        role: PermissionRole.reader,
        children: [
            {
                id: 'pageWidth-default',
                name: 'pageWidth',
                label: '默认',
                value: 'default',
                type: 'none',
                role: PermissionRole.reader
            },
            {
                id: 'pageWidth-wide',
                name: 'pageWidth',
                label: '较宽',
                value: 'wide',
                type: 'none',
                role: PermissionRole.reader
            },
            {
                id: 'pageWidth-full',
                name: 'pageWidth',
                label: '全宽',
                value: 'full',
                type: 'none',
                role: PermissionRole.reader
            }
        ]
    },
    {
        id: 'fontSize',
        name: 'fontSize',
        label: '字体大小',
        icon: 'bc_wenben',
        value: 'fontSize',
        type: 'dropdown',
        role: PermissionRole.reader,
        children: [
            {
                id: 'fontSize-smaller',
                name: 'fontSize',
                label: '更小',
                value: 'smaller',
                type: 'none',
                role: PermissionRole.reader
            },
            {
                id: 'fontSize-small',
                name: 'fontSize',
                label: '小号',
                value: 'small',
                type: 'none',
                role: PermissionRole.reader
            },
            {
                id: 'fontSize-default',
                name: 'fontSize',
                label: '默认',
                value: 'default',
                type: 'none',
                role: PermissionRole.reader
            },
            {
                id: 'fontSize-large',
                name: 'fontSize',
                label: '较大',
                value: 'large',
                type: 'none',
                role: PermissionRole.reader
            },
            {
                id: 'fontSize-larger',
                name: 'fontSize',
                label: '更大',
                value: 'larger',
                type: 'none',
                role: PermissionRole.reader
            }
        ]
    },
    {
        divider: true,
        id: 'bindTags',
        name: 'bindTags',
        label: '绑定标签',
        value: 'bindTags',
        type: 'none',
        icon: 'bc_a-shuqian',
        role: PermissionRole.manager
    },
    {
        id: 'move',
        name: 'move',
        label: '移动到',
        value: 'move',
        type: 'none',
        icon: 'bc_file-up',
        role: PermissionRole.manager
    },
    {
        divider: true,
        id: 'saveVersion',
        name: 'saveVersion',
        label: '另存为新版本',
        icon: 'bc_lingcunweixinbanben',
        value: 'saveVersion',
        role: PermissionRole.manager,
        type: 'none'
    },
    {
        divider: true,
        id: 'export',
        name: 'export',
        label: '下载为',
        icon: 'bc_xiazai',
        value: 'export',
        type: 'dropdown',
        role: PermissionRole.reader,
        children: [
            {
                id: 'export-pdf',
                name: 'export',
                label: 'PDF',
                value: 'pdf',
                type: 'none',
                role: PermissionRole.reader
            }
            // {
            //     id: 'export-img',
            //     name: 'export',
            //     label: '图片',
            //     value: 'img',
            //     type: 'none'
            // },
            // {
            //     id: 'export-html',
            //     name: 'export',
            //     label: 'HTML',
            //     value: 'html',
            //     type: 'none'
            // },
            // {
            //     id: 'export-snapshot',
            //     name: 'export',
            //     label: '快照',
            //     value: 'snapshot',
            //     type: 'none'
            // },
        ]
    },
    // {
    //     id: 'print',
    //     name: 'print',
    //     label: '打印',
    //     icon: 'bc_xiazai',
    //     value: 'print',
    //     type: 'none',
    //     role: PermissionRole.reader
    // }
];

const VERSION_LIST_MENU_ITEM: IDocSettingMenuItem = {
    id: 'versionList',
    name: 'versionList',
    label: '历史版本',
    value: 'versionList',
    icon: 'bc_lishibanben',
    type: 'dropdown',
    role: PermissionRole.manager,
    children: []
};

