import { Component, DestroyRef, EventEmitter, Inject, Input, Output } from '@angular/core';
import {
    AvatarPipe,
    CsesButtonComponent,
    CsesContextService,
    CsesPersonnelTreeSelector,
    CsesRenderService
} from '@ccc/cses-common';
import { NzInputModule } from 'ng-zorro-antd/input';
import { AsyncPipe, NgForOf, NgIf } from '@angular/common';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { FormsModule } from '@angular/forms';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { BehaviorSubject } from 'rxjs';
import { PathPipe } from '@/pages/doc-library/pipes/path.pipe';
import { AppContext, IUserInfo } from '@ccc/core-common';
import { ShareSearchModalComponent } from './search-modal';
import { ShareCollaboratorsManage } from './collaborators-manage';
import {
    copyEntityLink,
    DocsPermissionService, EntityType,
    IAccessConfig,
    ICollaborator, IDlBaseDialog,
    IDlUser, IEntity,
    PermissionRole,
    VisibilityRange
} from '@/pages/docs/services';

const VISIBILITY_RANGE_LIST: { label: string, value: VisibilityRange }[] = [
    {
        label: '未开启',
        value: VisibilityRange.PERSONAL
    },
    {
        label: '平台公开可见',
        value: VisibilityRange.EVERYONE
    }
];

export const DOC_SHARE_PWD_REG = /^(?!.*\p{Script=Han})(?!.*\s).{6,18}$/u;

type shareTabRoute = 'main' | 'search' | 'manage';

@Component({
    selector: 'share-modal',
    templateUrl: 'share-modal.html',
    styleUrls: ['./share-modal.scss'],
    standalone: true,
    imports: [
        CsesPersonnelTreeSelector,
        NzInputModule,
        NgForOf,
        NzDropDownModule,
        NgIf,
        NzSwitchModule,
        FormsModule,
        NzButtonModule,
        CsesButtonComponent,
        PathPipe,
        AvatarPipe,
        AsyncPipe,
        ShareSearchModalComponent,
        ShareCollaboratorsManage
    ]
})
export class ShareModalComponent implements IDlBaseDialog {
    @Input({ required: true })
    entity: IEntity;

    @Input({ required: true })
    entityType: EntityType;

    @Input({ required: true })
    owner: IDlUser;

    @Input({ required: true })
    localUserRole: PermissionRole;

    @Output()
    onClose = new EventEmitter<void>();

    private _accessConfig: IAccessConfig = {
        visibilityRole: PermissionRole.reader,
        visibility: VisibilityRange.COLLABORATOR
    };

    @Input({ required: true })
    set accessConfig(val: IAccessConfig) {
        this._accessConfig = val;
        this.visibilityRangeList.splice(1, 0, ...[
            {
                label: (val.company?.name || this.ctx.userInfo.companyName) + '及协作者',
                value: VisibilityRange.ENTERPRISE_AND_COLLABORATOR
            }
        ]);
        this.activeVisibilityRange = this.visibilityRangeList.find(item => item.value === val.visibility) || this.visibilityRangeList[0];
        this.passwordData = {
            value: val.password || '',
            isOpen: !!val.password,
            isError: false,
            isUpdated: false
        }
    }

    get accessConfig() {
        return this._accessConfig;
    }

    @Output() onCollaboratorsChange = new EventEmitter<IUserInfo[]>();

    protected readonly VISIBILITY_RANGE_LIST = VisibilityRange;
    protected readonly visibilityRangeList = [...VISIBILITY_RANGE_LIST];
    protected tabRoute$ = new BehaviorSubject<shareTabRoute[]>(['main']);

    protected readonly PermissionRole = PermissionRole;

    protected activeVisibilityRange: typeof this.visibilityRangeList[number] = this.visibilityRangeList[0];
    protected passwordData = {
        value: '',
        isOpen: false,
        isError: false,
        isUpdated: true
    };

    protected collaboratorsList: ICollaborator[];

    dropdownMenuVisible = {
        visibilityRole: false,
        visibility: false
    };

    constructor(
        private permissionService: DocsPermissionService,
        private render: CsesRenderService,
        @Inject(AppContext) public ctx: CsesContextService,
        readonly destroyRef: DestroyRef
    ) {
    }

    async onBackRoute() {
        const v = this.tabRoute$.value.slice(1);
        if (v[0] === 'manage') await this.getCollaborators();
        this.tabRoute$.next(this.tabRoute$.value.slice(1));
    }

    async onTabRoute(route: shareTabRoute) {
        if (route === 'manage') {
            // if (this.localUserRole < PermissionRole.manager) return;
            await this.getCollaborators();
        }
        this.tabRoute$.next([route, ...this.tabRoute$.value]);
    }

    onSearchInputFocus() {
        this.onTabRoute('search');
    }

    async getCollaborators() {
        const res = await this.permissionService.getCollaboratorList(this.entity.id);
        this.collaboratorsList = res.items;
    }

    onInviteCollaboratorsSure(params: any) {
        this.permissionService.addCollaborators(this.entity.id, params.collaborators).then(res => {
            this.render.success('协作人已添加');
            this.onBackRoute();
        }).catch(e => {
            this.render.error(e.error || '添加失败');
        });
    }

    onVisibilityRangeChange(range: typeof this.visibilityRangeList[number]) {
        if (this.activeVisibilityRange.value === range.value) return;

        this.permissionService.modifyShareVisibility(this.entity.id, {
            visibility: range.value,
            visibilityRole: this.accessConfig.visibilityRole
        }).then(() => {
            this.render.info('访问权限变更');
            this.activeVisibilityRange = range;
            this.accessConfig.visibility = range.value;

            this.dropdownMenuVisible.visibility = false;

            if (this.activeVisibilityRange.value <= VisibilityRange.COLLABORATOR) {
                this.onSwitchPassword(false);
            }
        });
    }

    onVisibilityRoleRangeChange(role: PermissionRole) {
        this.permissionService.modifyShareVisibility(this.entity.id, {
            visibility: this.accessConfig.visibility,
            visibilityRole: role
        }).then(() => {
            this.render.info('访问权限变更');
            this.accessConfig.visibilityRole = role;
            this.dropdownMenuVisible.visibilityRole = false;
        });
    }

    async onSwitchPassword(v: boolean) {
        if (!v) {
            // 如果此时已设置密码
            if (this.accessConfig.needPassword) {
                await this.permissionService.modifySharePassword(this.entity.id, {
                    password: '',
                    needPassword: false
                });
                this.render.success('密码已移除');
            }
            this.accessConfig.password = '';
            this.accessConfig.needPassword = false;
            this.passwordData.value = '';
        } else {
            this.passwordData.isError = false;
            this.passwordData.isUpdated = true;
            this.passwordData.value = Math.random().toString(36).slice(2, 8);
        }
        this.passwordData.isOpen = v;
    }

    onCheckPassword(e: string) {
        this.passwordData.isUpdated = true;
        this.passwordData.isError = !DOC_SHARE_PWD_REG.test(e);
    }

    onPasswordConfirm() {
        if (this.passwordData.isError || !this.passwordData.isUpdated) return;
        const pwd = this.passwordData.value.trim();
        this.permissionService.modifySharePassword(this.entity.id, {
            password: pwd,
            needPassword: this.passwordData.isOpen
        }).then(v => {
            this.render.success('密码设置成功');
            this.passwordData.isUpdated = false;
            this.passwordData.value = this.accessConfig.password = pwd;
            this.accessConfig.needPassword = this.passwordData.isOpen;
        });
    }

    copyLink() {
        copyEntityLink(this.entity.id, this.entityType).then(() => {
            this.render.success('链接已复制');
        });
    }

}
