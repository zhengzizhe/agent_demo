import { Component, Input } from '@angular/core';
import { NgForOf, NgIf } from '@angular/common';
import { AvatarPipe, CsesRenderService } from '@ccc/cses-common';
import {
    DocsOverlayService,
    DocsPermissionService,
    ICollaborator,
    IDlContextMenuItem,
    PermissionRole
} from '@/pages/docs/services';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { RoleLabelPipe } from './role-labl.pipe';
import { NzMenuItemComponent } from 'ng-zorro-antd/menu';
import { PERMISSION_OPTIONS } from './const';
import { RoleOptionMenu } from '@/pages/docs/components/share-modal/role-option-menu';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
    selector: 'share-collaborators-manage',
    template: `
        @for (item of collaborators; track item.user.id) {
            <div class="collaborator-item">
                <div>
                    @if (item.user.isUser) {
                        <img class="avatar" [src]="item.user.id | avatar" />
                    } @else {
                        <i class="CSES cses-section icon-dept"></i>
                    }
                    <span class="info">
                        <span class="label">
                            <span class="name">{{ item.user.name }}</span>
                            <span class="org" *ngIf="item.user.isUser">({{ item.user.deptName }}/{{ item.user.orgName }})</span>
                        </span>
                        <span class="prop" *ngIf="!item.direct">继承自父级文件夹/空间</span>
                    </span>
                </div>

                @if (item.role === PermissionRole.owner || maxRole < PermissionRole.manager) {
                    <span class="role">
                         <span>{{ item.role | roleLabel }}</span>
                    </span>
                } @else {
                    <span class="role dl-btn-hover" nz-dropdown nzTrigger="click"
                          [class.active]="activeItem == item"
                          (click)="onItemClick($event, item)"
                          nzOverlayClassName="dl-overlay-z-high">
                         <span>{{ item.role | roleLabel }}</span>
                         <i class="bf_icon bf_xiajaintou"></i>
                    </span>
                }
            </div>
        }
    `,
    styles: [`
        :host {
            width: 100%;
            display: flex;
            flex-direction: column;
            gap: 8px;

            .collaborator-item {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 8px 0;

                > div:first-child {
                    display: flex;
                    align-items: center;
                    gap: 8px;

                    .icon-dept, .avatar {
                        width: 28px;
                        height: 28px;
                        overflow: hidden;
                        border-radius: 50%;
                    }

                    .icon-dept {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #fff;
                        background: var(--cs-primary-color);
                    }

                    .info {
                        display: flex;
                        flex-direction: column;
                        gap: 4px;

                        .label {
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            line-height: 18px;

                            .name {
                                font-size: 14px;
                            }

                            .org {
                                font-size: 10px;
                                color: var(--cs-font-color-2);
                            }
                        }

                        .prop {
                            font-size: 10px;
                            color: var(--cs-font-color-3);
                            line-height: 12px;
                        }
                    }

                }

                .role {
                    padding: 0 4px;
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    height: 28px;

                    > * {
                        pointer-events: none;
                    }

                    > i {
                        transition: transform .2s ease-in-out;
                    }

                    &.active {
                        > i {
                            transform: rotate(180deg);
                        }
                    }

                }
            }
        }
    `],
    imports: [
        NgForOf,
        AvatarPipe,
        NgIf,
        NzDropdownMenuComponent,
        RoleLabelPipe,
        NzDropDownDirective,
        NzMenuItemComponent,
        RoleOptionMenu
    ],
    standalone: true
})
export class ShareCollaboratorsManage {
    @Input({ required: true })
    entityId: string;

    @Input({ required: true })
    collaborators: ICollaborator[] = [];

    @Input({ required: true })
    maxRole: PermissionRole;

    protected activeItem: ICollaborator;

    constructor(
        public render: CsesRenderService,
        private permissionService: DocsPermissionService,
        private overlayService: DocsOverlayService
    ) {
    }

    ngOnInit() {

    }

    getRoleOptions(item: ICollaborator) {
        const res: IDlContextMenuItem[] =  PERMISSION_OPTIONS.slice(1).filter(option => option.value <= this.maxRole).map(option => ({
            ...option,
            id: option.label,
            name: option.label,
            active: item.role === option.value
        }))
        if(this.maxRole >= PermissionRole.manager) {
            res.push({
                id: '删除',
                name: 'delete',
                label: '删除',
                value: 'delete',
                className: 'dl-error-btn'
            })
        }
        return res;
    }

    onItemClick(evt: MouseEvent, item: ICollaborator) {
        evt.stopPropagation();
        this.activeItem = item;
        const target = evt.target as HTMLElement;
        const {cpr, close} = this.overlayService.showContextMenu({
            target,
            menus: this.getRoleOptions(item),
            hasBackdrop: false,
            panelClass: 'dl-overlay-z-high',
        })
        cpr.instance.onMenuClick.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(evt => {
            switch (evt.value) {
                case 'delete':
                    this.onRemove();
                    break;
                default:
                    this.onRoleChange(evt.value as PermissionRole);
                    break;
            }
            this.activeItem = null
            close();
        })
        cpr.instance.destroyRef.onDestroy(() => {
            this.activeItem = null
        })
    }

    onRoleChange($event: PermissionRole) {
        if (this.maxRole < PermissionRole.manager) return;
        const item = this.activeItem;
        if (item.role === $event) return;
        this.permissionService.modifyCollaboratorRole({
            id: this.entityId,
            user: item.user,
            role: $event
        }).then(v => {
            item.role = $event;
            this.render.info('协作者权限变更');
        }).catch(e => {
            this.render.error(e.error || '修改权限失败');
        });
    }

    onRemove() {
        if (this.maxRole < PermissionRole.manager) return;
        const item = this.activeItem;
        this.permissionService.removeCollaborator(this.entityId, item.user).then(v => {
            this.collaborators = this.collaborators.filter(c => c.user.id !== item.user.id);
            this.render.info('已移除协作者');
        });
    }

    protected readonly PermissionRole = PermissionRole;


}
