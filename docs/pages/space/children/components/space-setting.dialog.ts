import { Component, DestroyRef, EventEmitter, Input, Output } from '@angular/core';
import { IDlBaseDialog, ISpaceDetail, PermissionRole } from '@/pages/docs/services';
import { DlTagTreeComponent } from '@/pages/docs/components';

export type SpaceSettingDialogTab = 'base' | 'tags'

@Component({
    selector: 'dl-space-setting-dialog',
    templateUrl: './space-setting.dialog.html',
    styleUrls: ['./space-setting.dialog.scss'],
    imports: [
        DlTagTreeComponent
    ],
    standalone: true
})
export class SpaceSettingDialog implements IDlBaseDialog {
    @Input({ required: true })
    spaceDetail: ISpaceDetail;

    @Input()
    activeTab: SpaceSettingDialogTab = 'base';

    @Output()
    onClose: EventEmitter<void> = new EventEmitter();

    tabs = [
        {
            id: 'base',
            name: '基本信息'
        },
        {
            id: 'tags',
            name: '标签配置'
        }
    ];

    baseForm = {
        name: '',
        description: ''
    };

    constructor(
        readonly destroyRef: DestroyRef
    ) {

    }

    ngOnInit() {
        this.baseForm = {
            name: this.spaceDetail.name,
            description: this.spaceDetail.description || ''
        };
    }

    onSwitchTab(item: typeof this.tabs[number]) {
        this.activeTab = item.id as SpaceSettingDialogTab;
    }

    protected readonly PermissionRole = PermissionRole;
}
