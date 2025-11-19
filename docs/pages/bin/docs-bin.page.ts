import { Component, Input, ViewChild } from '@angular/core';
import {
    IEntityTableOpButtonItem,
    IFileTableHeadItem,
    MixEntityTableComponent
} from '@/pages/docs/components';
import {
    DocsOverlayService,
    EntityType, IDlContextMenuItem,
    IEntity, IPagingListResponse
} from '@/pages/docs/services';
import { CsesHttpService, CsesRenderService } from '@ccc/cses-common';
import { HttpService } from '@/pages/docs/services/http.service';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

const tableHead: IFileTableHeadItem[] = [
    {
        value: 'nodeType',
        label: '所有类型',
        type: 'filter',
        activeFilter: null,
        filterOptions: [
            {
                value: null,
                label: '所有类型'
            },
            {
                value: EntityType.Document,
                label: '文档'
            },
            {
                value: EntityType.Folder,
                label: '文件夹'
            }
        ]
    },
    {
        type: 'none',
        value: 'name',
        label: '所有者'
    },
    {
        type: 'none',
        value: 'remain',
        label: '剩余时间'
    }
];

@Component({
    selector: 'docs-trash-page',
    templateUrl: './docs-bin.page.html',
    styleUrls: ['./docs-bin.page.scss'],
    imports: [
        MixEntityTableComponent
    ],
    standalone: true,
    providers: [
        DocsOverlayService
    ],
    host: {
        '[class.dl-page-main]': 'true'
    }
})
export class DocsBinPage {
    fileTableQueryParams = {};
    fileTableOpMenus: IEntityTableOpButtonItem[] = [
        {
            label: '恢复',
            icon: 'bc_huifu1',
            value: 'restore',
            name: 'restore'
        },
        {
            label: '彻底删除',
            icon: 'bc_shanchu',
            value: 'delete',
            name: 'delete',
            className: 'dl-error-btn'
        }
    ];
    tableHead = tableHead;
    tableRequester = (params) => {
        return this.httpService._post<IPagingListResponse<IEntity>>('/doc/trash/list', params);
    };
    tableItemDropDownMenu: IDlContextMenuItem[] = [
        {
            label: '恢复',
            icon: 'bc_huifu1',
            id: 'restore',
            name: 'restore'
        },
        {
            label: '彻底删除',
            icon: 'bc_shanchu',
            id: 'delete',
            name: 'delete',
            className: 'dl-error-btn'
        }
    ];

    httpService = new HttpService(this.csesHttp);

    @ViewChild('mixEntityTable') mixEntityTable: MixEntityTableComponent;

    constructor(
        protected readonly csesHttp: CsesHttpService,
        private render: CsesRenderService,
        private iconRegistry: MatIconRegistry,
        private sanitizer: DomSanitizer
    ) {
        this.iconRegistry.addSvgIconSet(
            this.sanitizer.bypassSecurityTrustResourceUrl('https://at.alicdn.com/t/c/font_4682833_xx54lvcm76c.js')
        );
    }

    refresh() {
        this.mixEntityTable?.refresh();
    }

    onContextMenuClick(evt: { menu: IDlContextMenuItem; entity: IEntity }) {
        switch (evt.menu.id) {
            case 'restore':
                this.restore([evt.entity.id]);
                break;
            case 'delete':
                this.delete([evt.entity.id]);
                break;
        }
    }

    restore(ids: string[]) {
        this.httpService._post(`/doc/trash/recover`, { ids }).then(res => {
            if (!res.failIds) {
                this.render.success(ids.length + '项已恢复');
                this.mixEntityTable.removeItems(ids);
            } else {
                this.render.warning(res.successIds + '项已恢复，' + res.failIds + '项恢复失败: ' + res.failedMessage[0].errorMessage);
                const failedIds = new Set(res.failedMessage.map(msg => msg.id));
                this.mixEntityTable.removeItems(ids.filter(id => !failedIds.has(id)));
            }
        }).catch(e => {
            this.render.error(e.message || '恢复出错');
        });
    }

    delete(ids: string[]) {
        this.render.warnConfirm({
            title: '彻底删除',
            content: '彻底删除后，文件将无法恢复，确定删除吗？',
            beforeOpen: () => {
            },
            buttons: [
                {
                    label: '确定',
                    type: 'primary',
                    onClick: () => {
                        this.httpService._post(`/doc/trash/delete`, { ids }).then(res => {
                            if (!res.failIds) {
                                this.render.info(ids.length + '项已删除');
                                this.mixEntityTable.removeItems(ids);
                            } else {
                                this.render.warning(res.successIds + '项已删除，' + res.failIds + '项失败: ' + res.failedMessage[0].errorMessage);
                                const failedIds = new Set(res.failedMessage.map(msg => msg.id));
                                this.mixEntityTable.removeItems(ids.filter(id => !failedIds.has(id)));
                            }
                        }).catch(e => {
                            this.render.error(e.message || '删除出错');
                        });
                    }
                }
            ]
        });
    }

    onOpButtonClick(evt: IEntityTableOpButtonItem) {
        const checkedIds = this.mixEntityTable.getCheckedIds();
        switch (evt.value) {
            case 'restore':
                this.restore(checkedIds);
                break;
            case 'delete':
                this.delete(checkedIds);
                break;
        }
    }
}
