import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    ElementRef, EventEmitter, Input, Output,
    ViewChild
} from '@angular/core';
import { KeyValuePipe, NgForOf, NgIf } from '@angular/common';
import { AvatarPipe } from '@ccc/cses-common';
import { debounceTime, fromEvent } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IOrgTreeDataItem } from '@ccc/cses-common/src/components/cses-personnel-tree/cses-personnel-tree.const';
import { NzDropDownDirective, NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { ICollaborator, PermissionRole, UserService } from '@/pages/docs/services';
import { PERMISSION_OPTIONS } from './const';
import { RoleLabelPipe } from './role-labl.pipe';
import { RoleOptionMenu } from '@/pages/docs/components/share-modal/role-option-menu';
import { DlButton } from '@/pages/docs/components';

@Component({
    selector: 'share-search-modal',
    template: `
        <div class="collaborators-inputer" style="height: unset;">
            <span class="tag" *ngFor="let item of selectedItems; trackBy: trackByFn">
                @if (item.isUser) {
                    <img class="avatar" [src]="item.id | avatar" />
                } @else {
                    <i class="CSES cses-section icon-dept"></i>
                }
                <span class="name">{{ item.name }}</span>
                <span class="info" *ngIf="item.isUser">({{ item.deptName }}/{{ item.orgName }})</span>
                <i class="bf_icon bf_guanbi btn-remove" (click)="removeSelected(item)"></i>
            </span>
            <input type="text" placeholder="搜索用户或部门" (compositionstart)="isComposing = true"
                   (compositionend)="isComposing = false" (keydown)="onInputKeydown($event)" #input />

            <span class="role-selector dl-btn-hover" nz-dropdown nzTrigger="click" [nzDropdownMenu]="optionMenu"
                  nzOverlayClassName="dl-overlay-z-high" *ngIf="selectedItems.length">
                <span class="role-name">{{ permissionRole | roleLabel }}</span>
                <i class="bc_icon bc_xiajaintou"></i>
            </span>
        </div>
        <!--        <div style="display: flex; align-items: center; gap: 4px; height: 25px;" [hidden]="searchResult.length">-->
        <!--            <span [class]="['btn-radio','bf_icon', sendNotice ? 'bf_xuanzhong-fill' : 'bf_weixuanzhong']"-->
        <!--                  [class.checked]="sendNotice" (click)="sendNotice = !sendNotice"></span>-->
        <!--            发送通知-->
        <!--        </div>-->
        <!--        <textarea placeholder="添加备注" [hidden]="searchResult.length"></textarea>-->

        <div class="search-result" *ngIf="searchResult.length">
            <div class="user-item search-item" [class.selecting]="selectingIdx === idx"
                 [class.active]="_selectedIdSet.has(item.id)" [class.disabled]="disabledIdsSet.has(item.id)"
                 (mousedown)="$event.preventDefault(); toggleSelected(item)"
                 *ngFor="let item of searchResult; index as idx; trackBy: trackByFn">
                @if (item.isUser) {
                    <img class="avatar" [src]="item.id | avatar" />
                } @else {
                    <i class="CSES cses-section icon-dept"></i>
                }
                <span class="name">{{ item.name }}</span>
                <span class="info" *ngIf="item.isUser">({{ item.deptName }}/{{ item.orgName }})</span>
            </div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 8px;">
            <button dl-button (click)="cancel.emit()">取消</button>
            <button dl-button type="primary" (click)="onConfirm()" [disabled]="!selectedItems.length">确认</button>
        </div>

        <nz-dropdown-menu #optionMenu="nzDropdownMenu">
            <role-option-menu [roleOptions]="roleOptions" [(role)]="permissionRole">
            </role-option-menu>
        </nz-dropdown-menu>
    `,
    styles: [`
        :host {
            position: relative;
            width: 100%;
            display: flex;
            flex-direction: column;
            gap: 16px;

            > textarea {
                height: 154px;
                width: 100%;
                outline: none;
                border-radius: 4px;
                border: 1px solid #e6e6e6;
                padding: 8px;
                resize: none;
            }

            .icon-dept {
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                overflow: hidden;
                color: #fff;
                background: var(--cs-primary-color);
            }

            .avatar {
                border-radius: 50%;
                overflow: hidden;
            }

            .role-selector {
                position: absolute;
                top: 3px;
                right: 8px;
                display: flex;
                align-items: center;
                gap: 4px;
                padding: 0 4px;
                height: 28px;
            }
        }

        .collaborators-inputer {
            position: relative;
            padding: 5px 76px 5px 0;
            flex-wrap: wrap;

            > .tag {
                display: flex;
                padding: 0 4px;
                height: 24px;
                align-items: center;
                gap: 4px;
                border-radius: 4px;
                background: #F5F5F5;

                > .icon-dept {
                    width: 20px;
                    height: 20px;
                    font-size: 14px;
                }

                > .avatar {
                    width: 20px;
                    height: 20px;
                }

                .name {
                    font-size: 14px;
                    color: #333;
                }

                .info {
                    font-size: 10px;
                    color: #999;
                }

                .btn-remove {
                    color: #999;
                    cursor: pointer;
                }
            }
        }

        .btn-radio {
            color: #999;
            cursor: pointer;

            &.checked {
                color: var(--cs-primary-color);
            }
        }

        .search-result {
            width: 100%;
            display: flex;
            flex-direction: column;
            gap: 4px;
            background: #fff;
            height: 154px + 32px + 25px;
            overflow-y: auto;

            .search-item {
                display: flex;
                align-items: center;
                gap: 8px;
                height: 44px;
                line-height: 20px;
                border-radius: 4px;
                padding: 8px;
                cursor: pointer;

                &.active {
                    background: #f5f5f5;

                    > span {
                        color: var(--cs-primary-color);
                    }
                }

                &.disabled {
                    opacity: .7;
                    cursor: not-allowed;
                }

                &:hover, &.selecting {
                    background: rgba(245, 245, 245, 0.8);
                }

                > .icon-dept {
                    width: 28px;
                    height: 28px;
                    font-size: 20px;
                }

                > .avatar {
                    width: 28px;
                    height: 28px;
                }

                .name {
                    font-size: 14px;
                    color: #333;
                }

                .info {
                    font-size: 10px;
                    color: #999;
                }
            }
        }
    `],
    imports: [
        NgForOf,
        AvatarPipe,
        NgIf,
        KeyValuePipe,
        NzDropdownMenuComponent,
        RoleLabelPipe,
        NzDropDownDirective,
        RoleOptionMenu,
        DlButton
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShareSearchModalComponent {

    private _collaborators: ICollaborator[] = []
    @Input({required: true})
    set collaborators(val: ICollaborator[]) {
        this._collaborators = val;
        this.disabledIdsSet = new Set(val.map(item => item.user.id));
    }
    get collaborators() {
        return this._collaborators;
    }

    @Input({ required: true })
    maxRole: PermissionRole;

    @Output() cancel = new EventEmitter<void>();
    @Output() confirm = new EventEmitter<{
        collaborators: ICollaborator[]
        sendNotice: boolean;
    }>();

    @ViewChild('input', { read: ElementRef }) input: ElementRef<HTMLInputElement>;

    trackByFn = (index, item) => item.id;

    isComposing = false;

    protected roleOptions = [];

    permissionRole = PermissionRole.reader;
    sendNotice = false;

    protected disabledIdsSet = new Set<string>()
    protected _selectedIdSet: Set<string> = new Set();
    protected _selectedItems: IOrgTreeDataItem[] = [];

    @Input()
    get selectedItems() {
        return this._selectedItems;
    }

    set selectedItems(val: IOrgTreeDataItem[]) {
        this._selectedItems = val;
        this._selectedIdSet = new Set(val.map(item => item.id));
        this.cdr.markForCheck();
    }

    _selectingIdx = 0;
    get selectingIdx() {
        return this._selectingIdx;
    }

    set selectingIdx(val) {
        this._selectingIdx = val;
        this.cdr.markForCheck();
        requestAnimationFrame(() => {
            this.host.nativeElement.querySelector(`.search-item.selecting`)?.scrollIntoView({ block: 'nearest' });
        });
    }

    searchResult: Array<IOrgTreeDataItem> = [];

    constructor(
        private readonly destroyRef: DestroyRef,
        private cdr: ChangeDetectorRef,
        private host: ElementRef<HTMLElement>,
        private userService: UserService
    ) {
    }

    ngOnInit() {
        this.roleOptions = PERMISSION_OPTIONS.slice(1).filter(option => option.value <= this.maxRole);
    }

    ngAfterViewInit() {
        this.input.nativeElement.focus();
        fromEvent(this.input.nativeElement, 'input').pipe(takeUntilDestroyed(this.destroyRef), debounceTime(200)).subscribe(e => {
            this.onInput();
        });
    }

    onInput() {
        if (this.isComposing) return;
        const value = this.input.nativeElement.value;
        this.selectingIdx = 0;
        if (!value) {
            this.searchResult = [];
            this.cdr.markForCheck();
            return;
        }

        this.userService.searchInOrganize(value).then(res => {
            this.searchResult = res;
            this.cdr.markForCheck();
        });
    }

    onInputKeydown(e: KeyboardEvent) {
        if (this.isComposing) return;
        switch (e.key) {
            case 'Enter':
                const item = this.searchResult[this.selectingIdx];
                if (!item) break;
                this.toggleSelected(item);
                break;
            case 'ArrowUp':
                e.preventDefault();
                this.selectingIdx = this.selectingIdx > 0 ? this.selectingIdx - 1 : this.searchResult.length - 1;
                break;
            case 'ArrowDown':
                e.preventDefault();
                this.selectingIdx = this.selectingIdx < this.searchResult.length - 1 ? this.selectingIdx + 1 : 0;
                break;
            case 'Backspace':
                if (this.input.nativeElement.selectionStart === 0 && this.input.nativeElement.selectionEnd === 0) {
                    const item = this.selectedItems.pop();
                    this._selectedIdSet.delete(item.id);
                    this.cdr.markForCheck();
                }
                break;
        }
    }

    toggleSelected(item: IOrgTreeDataItem) {
        if(this.disabledIdsSet.has(item.id)) return
        if (this._selectedIdSet.has(item.id)) {
            this.removeSelected(item);
        } else {
            this.addSelected(item);
        }
    }

    addSelected(item: IOrgTreeDataItem) {
        if (this._selectedIdSet.has(item.id)) return;
        this.input.nativeElement.value = ''
        this._selectedIdSet.add(item.id);
        this._selectedItems.push(item);
        requestAnimationFrame(() => {
            this.cdr.detectChanges();
        });
    }

    removeSelected(item: IOrgTreeDataItem) {
        if (!this._selectedIdSet.has(item.id)) return;
        this._selectedIdSet.delete(item.id);
        this._selectedItems.splice(this._selectedItems.findIndex(v => v.id === item.id), 1);
        requestAnimationFrame(() => {
            this.cdr.detectChanges();
        });
    }

    onConfirm() {
        this.confirm.emit({
            collaborators: this._selectedItems.map(item => {
                return {
                    user: {
                        id: item.id,
                        name: item.name,
                        type: item.type,
                        orgName: item.orgName,
                        orgId: item.orgId,
                        deptName: item.deptName,
                        deptId: item.deptId,
                        userId: item.userId,
                        userName: item.userName,
                        isUser: item.isUser,
                        companyId: item['companyId']
                    },
                    role: this.permissionRole
                };
            }),
            sendNotice: this.sendNotice
        });
    }
}
