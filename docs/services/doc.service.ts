import { ComponentRef, Injectable } from '@angular/core';
import { IMentionData } from '@w/blockflow-editor';
import { lastValueFrom, Subject, take } from 'rxjs';
import { CsesHttpService, CsesRenderService } from '@ccc/cses-common';
import { Overlay } from '@angular/cdk/overlay';
import {
    DocBehaviorType,
    EntityType,
    IBaseListResponse,
    IBaseResponse,
    IDocVersionInfo
} from '@/pages/docs/services/types';
import { PersonService } from '@/services/person.service';
import { UserService } from './user.service';
import { HttpService } from '@/pages/docs/services/http.service';
import { IBlockSnapshot, NativeBlockModel } from '@ccc/blockcraft';
import { EntityService } from '@/pages/docs/services/entity.service';
import { UserAction, UserCardComponent } from '@/components/user-card/user-card.component';
import { ComponentPortal } from '@angular/cdk/portal';

@Injectable()
export class DocService extends HttpService {
    constructor(
        protected readonly http: CsesHttpService,
        public readonly render: CsesRenderService,
        public readonly overlay: Overlay,
        public readonly personService: PersonService,
        readonly userService: UserService,
        private entityService: EntityService
    ) {
        super(http);
    }

    async getMentionData(keyword: string, type: 'user' | 'doc') {
        if (type === 'user') return this.findUsers(keyword);
        return this.findDocByKeyword(keyword);
    }

    async findDocByKeyword(keyword: string): Promise<{ list: IMentionData[] }> {
        if (!keyword) {
            const res = await this.entityService.getHomeDocView({
                pageNumber: 1,
                pageSize: 10,
                timeSort: 'openedAt'
            });
            return { list: res.items };
        }
        const res = await this.entityService.globalKeywordSearch({
            pageNumber: 1,
            pageSize: 10,
            keyword,
            filters: ['title'],
            nodeTypes: [EntityType.Document]
        });
        return { list: res.items };
    }

    private defaultMentionUserList?: any[];

    async findUsers(keyword: string): Promise<{ list: IMentionData[] }> {
        if (!keyword)
            return {
                list: this.defaultMentionUserList ||= [...this.personService.personMap.values()].filter(v => v.deptId === this.userService.ctx.userInfo.deptId)
            };
        const list: IMentionData[] = [];
        this.personService.personMap.forEach((v, k) => {
            if (v.userName?.includes(keyword)) list.push(v as any);
        });
        return { list };
    }

    addSyncedFlag = async (docId: string, synced = true) => {
        return this._post<IBaseResponse & { result: boolean }>('/doc/document/sync/save', {
            id: docId,
            isSync: synced
        });
    };

    getSyncedFlag = async (docId: string) => {
        return this._post<IBaseResponse & { result: boolean }>('/doc/document/sync/find', { id: docId });
    };

    checkMigrateDataExist(docId: string) {
        return this._post<IBaseResponse & { result: boolean }>(`/doc/document/checkExist`, { id: docId });
    }

    getMigrateData(docId: string) {
        return this._post(`/doc/document/read`, { id: docId });
    };

    storeMigrateData = async (docId: string, data: any) => {
        const res = await lastValueFrom(this.http.post(`/doc/document/save`, { id: docId, data }));
        return res.isSuccess;
    };

    diffStoreSnapshot = async (docId: string, blocks: any[], cancelToke$?: Subject<void>) => {
        // blocks.forEach((v: IDiffBlockData) => {
        //     if (v.mode === 'Update') {
        //         delete v.mode
        //         tempData[v.id] = v
        //     } else {
        //         delete tempData[v.id];
        //     }
        // });
        return this._post<IBaseResponse & { result: boolean }>(`/doc/document/edit`, { docId, blocks }, cancelToke$);
    };

    getBlockSnapshot = async (docId: string, blockId: string, recursive = true) => {
        // const res = JSON.parse(JSON.stringify(tempData))
        // const snapshot =  res[docId]
        // console.log('-----', res, snapshot);
        // if (!snapshot) return null;
        //
        // const setChildren = (block: NativeBlockModel) => {
        //     if (block.nodeType !== 'editable') {
        //         block.children = block.children?.map(id => {
        //             const child = res[id];
        //             setChildren(child);
        //             return child;
        //         }) || [];
        //     } else {
        //         block.children = block['content'] || [];
        //     }
        //     delete block['content'];
        //     delete block['text'];
        //     delete block['parentId']
        // };
        // setChildren(snapshot);
        // return snapshot as IBlockSnapshot;
        const res = await this._post<IBaseResponse & Record<string, NativeBlockModel>>(`/doc/document/block/read`, {
            docId,
            blockId,
            recursive
        });
        const snapshot = res[blockId];
        if (!snapshot) return null;
        const setChildren = (block: NativeBlockModel) => {
            if (block.nodeType !== 'editable') {
                block.children = block.children?.map(id => {
                    const child = res[id];
                    setChildren(child);
                    return child;
                }) || [];
            } else {
                block.children = block['content'] || [];
            }
            delete block['content'];
            delete block['text'];
        };
        setChildren(snapshot);
        return snapshot as IBlockSnapshot;
    };

    showUserCard(userId: string, target: HTMLElement, actions?: UserAction[]) {
        const userInfo = this.personService.getPerson(userId);
        if (!userInfo) return;
        const ovr = this.overlay.create({
            positionStrategy: this.overlay.position().flexibleConnectedTo(target).withPositions([
                { originX: 'start', originY: 'bottom', overlayX: 'start', overlayY: 'top' },
                { originX: 'start', originY: 'top', overlayX: 'start', overlayY: 'bottom' }
            ]).withPush(false),
            hasBackdrop: true,
            backdropClass: 'cdk-overlay-transparent-backdrop'
        });
        const dialog: ComponentRef<UserCardComponent> = ovr.attach(new ComponentPortal(UserCardComponent));
        dialog.setInput('userInfo', userInfo);
        dialog.setInput('infos', [{ name: '部门', value: userInfo.deptName }, {
            name: '职位',
            value: userInfo.orgName
        }]);
        actions?.length && dialog.setInput('actions', actions);
        ovr.backdropClick().pipe(take(1)).subscribe(() => ovr.dispose());
    }

    createVersion(params: { id: string, name: string, data: Uint8Array, description?: string }) {
        return this._post(`/doc/version/create`, {
            ...params,
            data: btoa(String.fromCharCode(...params.data))
        });
    };

    getVersionList(id: string) {
        return this._post<IBaseListResponse<IDocVersionInfo>>(`/doc/version/list`, { id });
    }

    readVersion(id: string, version: string) {
        return this._post<IBaseResponse & IDocVersionInfo>(`/doc/version/read`, { id, version });
    }

}

// const tempData: Record<string, IDiffBlockData> = {};
