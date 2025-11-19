import { CsesHttpService, CsesRenderService } from '@ccc/cses-common';
import { lastValueFrom, Subject } from 'rxjs';
import { EventEmitter, Injectable, Injector, Output } from '@angular/core';
import {
    ISpaceEntity,
    IBasePageQueryParams,
    IBaseResponse,
    IPagingListResponse,
    ISpaceDetail,
    IBaseListResponse,
    IFolderEntity,
    IEntity,
    IFolderDetail,
    IDocumentEntity,
    ICreateFolderForm,
    ICreateDocumentForm,
    IDocDetail, SpaceType, EntityType, DocBehaviorType, IDlUser, ITag, PermissionRole
} from './types';
import { FolderCreateDialogComponent } from '../components/folder-create-dialog/folder-create-dialog';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DocCreateDialogComponent } from '../components/doc-create-dialog/doc-create-dialog';
import { merge } from 'rxjs';
import { IEntitiesUpdate } from '@/pages/docs/services/types/update.types';
import { HttpService } from './http.service';
import { DocsPermissionService } from '@/pages/docs/services/permission.service';
import { DeleteConfirmDialog, DlEntityMoveDialog, EntityDetailDialogComponent } from '@/pages/docs/components';
import { DlTagBindDialog } from '@/pages/docs/components/tag-bind-dialog/tag-bind-dialog';
import { DocsOverlayService } from '@/pages/docs/services/overlay.service';

const SPACE_TYPE_SORT: SpaceType[] = ['personal', 'enterprise', 'cses', 'custom'];

@Injectable()
export class EntityService extends HttpService {

    @Output()
    entitiesUpdate$ = new Subject<IEntitiesUpdate>();

    constructor(
        protected readonly http: CsesHttpService,
        public readonly render: CsesRenderService,
        public readonly overlayService: DocsOverlayService,
        private permissionService: DocsPermissionService,
        readonly injector: Injector
    ) {
        super(http);
    }

    // get overlay() {
    //     return this.overlayService.overlay
    // }

    public spaceList: ISpaceEntity[] = [];

    public async getSpaceList(params?: IBasePageQueryParams) {
        const res = await this._post<IPagingListResponse<ISpaceEntity>>('/doc/space/list', params);
        this.spaceList = res.items.sort(
            (a, b) => SPACE_TYPE_SORT.indexOf(a.spaceType) - SPACE_TYPE_SORT.indexOf(b.spaceType)
        );
        return this.spaceList;
    }

    isSelfSpace(spaceId: string) {
        return this.spaceList.some(item => item.id === spaceId);
    }

    // -------------------- 获取列表 --------------------
    getFolderList(params: { id: string } & IBasePageQueryParams, cancelToken?: Subject<any>) {
        return this._post<IPagingListResponse<IFolderEntity>>('/doc/folder/list', params, cancelToken);
    }

    getHomeDocView(params: IBasePageQueryParams, cancelToken?: Subject<any>) {
        return this._post<IPagingListResponse<IDocumentEntity>>('/doc/home/view/list', params, cancelToken);
    }

    getSharingDocList(params: IBasePageQueryParams) {
        return this._post<IPagingListResponse<IDocumentEntity>>('/doc/document/sharing/list', params);
    }

    getSharingFolderList(params: IBasePageQueryParams) {
        return this._post<IPagingListResponse<IFolderEntity>>('/doc/folder/sharing/list', params);
    }

    // 获取空间/文件夹下的文档列表
    getDocList(params: { id: string } & IBasePageQueryParams) {
        return this._post<IPagingListResponse<IDocumentEntity>>('/doc/document/list', params);
    }

    // 查询行为文件夹列表
    getBehaviorFolderList(params: { behaviorType: DocBehaviorType } & IBasePageQueryParams) {
        return this._post<IPagingListResponse<IFolderEntity>>('/doc/behavior/folder/list', params);
    }

    // 查询行为文档列表
    getBehaviorDocList(params: { behaviorType: DocBehaviorType } & IBasePageQueryParams) {
        return this._post<IPagingListResponse<IDocumentEntity>>('/doc/behavior/document/list', params);
    }

    // 获取标签列表
    getTagDocList(params: { id: string } & IBasePageQueryParams) {
        return this._post<IPagingListResponse<IDocumentEntity>>('/doc/tag/document/list', params);
    }

    getTagFolderList(params: { id: string } & IBasePageQueryParams) {
        return this._post<IPagingListResponse<IFolderEntity>>('/doc/tag/folder/list', params);
    }

    // -------------------- 获取列表 END --------------------

    // -------------------- 增删改查 --------------------

    getEntityPath(id: string, startId: string) {
        return this._post<IBaseListResponse<IEntity>>('/doc/folder/path', {
            startId,
            endId: id
        });
    }

    getSpaceDetail(id: string) {
        return this._post<IBaseResponse & ISpaceDetail>('/doc/space/detail', { id });
    }

    getFolderDetail(id: string) {
        return this._post<IBaseResponse & IFolderDetail>('/doc/folder/detail', { id });
    }

    getDocumentDetail(id: string) {
        return this._post<IBaseResponse & IDocDetail>('/doc/document/view', { id });
    }

    newFolder(params: ICreateFolderForm) {
        return this._post<IBaseResponse>('/doc/folder/create', params);
    }

    newDocument(params: ICreateDocumentForm) {
        return this._post<IBaseResponse & { result: string }>('/doc/document/create', params);
    }

    createDocument(parent: string): Promise<IEntity> {
        if (!parent) return;
        let isLoading = false;
        return new Promise((resolve, reject) => {
            const { cpr: dialog, overlayRef: ovr } = this.overlayService.showDialog(DocCreateDialogComponent, {
                onClose: () => {
                    if (isLoading) return false;
                    reject('取消创建文档');
                    return true;
                }
            });

            dialog.instance.onOk.pipe(takeUntilDestroyed(dialog.instance.destroyRef)).subscribe(async form => {
                if (isLoading) return;
                dialog.setInput('isLoading', isLoading = true);

                try {
                    const res = await this.newDocument({
                        ...form,
                        targetId: parent
                    });
                    if (!res?.isSuccess) {
                        dialog.setInput('isLoading', isLoading = false);
                        reject('新建失败');
                        return;
                    }

                    // 提前做一次check，防止后台数据同步稍慢
                    setTimeout(() => {
                        this.permissionService.accessCheck(res.result).then(() => {
                            setTimeout(() => {
                                resolve({
                                    id: res.result,
                                    name: form.name,
                                    nodeType: EntityType.Document
                                });
                                dialog.setInput('isLoading', isLoading = false);
                                ovr.dispose();
                            }, 300);
                        }).catch(e => {
                            dialog.setInput('isLoading', isLoading = false);
                            reject(e);
                        });
                    }, 300);
                } catch (e) {
                    dialog.setInput('isLoading', isLoading = false);
                    this.render.error(e.error || '');
                    reject(e);
                }
            });
        });
    }

    createFolder(parent: string, spaceId: string): Promise<IFolderEntity> {
        if (!parent) return;
        let isLoading = false;
        return new Promise((resolve, reject) => {

            const { cpr: dialog, overlayRef: ovr } = this.overlayService.showDialog(FolderCreateDialogComponent, {
                hasBackdrop: true,
                onClose: () => {
                    if (isLoading) return false;
                    reject('取消创建文件夹');
                    return true;
                }
            });

            dialog.instance.onOk.pipe(takeUntilDestroyed(dialog.instance.destroyRef)).subscribe(async form => {
                if (isLoading) return;
                dialog.setInput('isLoading', isLoading = true);

                try {
                    const res = await this.newFolder({
                        ...form,
                        targetId: parent
                    });
                    if (!res?.isSuccess) {
                        dialog.setInput('isLoading', isLoading = false);
                        reject('新建文件夹失败');
                        return;
                    }
                    setTimeout(() => {
                        this.permissionService.accessCheck(res.result).then(() => {
                            setTimeout(() => {
                                this.render.success('新建文件夹成功');
                                resolve({
                                    id: res.result,
                                    name: form.name,
                                    nodeType: EntityType.Folder,
                                    spaceId,
                                    subCount: 0,
                                    createdAt: Date.now()
                                });
                                dialog.setInput('isLoading', isLoading = false);
                                ovr.dispose();
                            }, 300);
                        }).catch(e => {
                            dialog.setInput('isLoading', isLoading = false);
                            reject(e);
                        });
                    }, 300);
                } catch (e) {
                    dialog.setInput('isLoading', isLoading = false);
                    this.render.error(e.error || '');
                    reject(e);
                }

            });
        });
    }

    async removeFromHomeView(ids: string[]) {
        return await this._post<IBaseResponse>('/doc/home/view/remove', { ids });
    }

    async deleteDocs(ids: string[], recycle = true) {
        return await this._post<IBaseResponse>('/doc/document/remove', { ids, recycle });
    }

    async deleteFolder(folder: IEntity, recycle = true) {
        return await this._post<IBaseResponse>('/doc/folder/delete', { id: folder.id, recycle });
    }

    async tryDeleteFolder(folder: IEntity) {
        return new Promise((resolve, reject) => {
            const { cpr, overlayRef } = this.overlayService.showDialog(DeleteConfirmDialog, {
                hasBackdrop: true,
                transparentBackdrop: true,
                onClose: () => {
                    if (cpr.instance.isLoading) return false;
                    reject({ isCancel: true });
                    return true;
                }
            });
            cpr.setInput('title', '删除文件夹');
            cpr.setInput('message', '是否确认删除文件夹“' + folder.name + '”？');
            cpr.instance.onConfirm.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(async form => {
                cpr.setInput('isLoading', true);
                try {
                    const res = await this.deleteFolder(folder, form.recycle);
                    overlayRef.dispose();
                    resolve(res);
                } catch (e) {
                    cpr.setInput('isLoading', false);
                    reject(e);
                } finally {
                }
            });

        });
    }

    async tryDeleteDocs(docList: IEntity[]) {
        return new Promise((resolve, reject) => {
            const { cpr, overlayRef } = this.overlayService.showDialog(DeleteConfirmDialog, {
                hasBackdrop: true,
                transparentBackdrop: true,
                onClose: () => {
                    if (cpr.instance.isLoading) return false;
                    reject({ isCancel: true });
                    return true;
                }
            });
            cpr.setInput('title', '删除文档');
            cpr.setInput('message', `是否确认删除${docList.length === 1 ?
                ('文档“' + (docList[0].name || '未命名文档') + '”')
                : ('“' + (docList[0].name || '未命名文档') + '”等' + docList.length + '个文档')
            }？`);
            cpr.instance.onConfirm.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(async form => {
                try {
                    cpr.setInput('isLoading', true);
                    const res = await this.deleteDocs(docList.map(doc => doc.id), form.recycle);
                    overlayRef.dispose();
                    resolve(res);
                } catch (e) {
                    cpr.setInput('isLoading', false);
                    reject(e);
                } finally {
                }
            });
        });
    }

    modifyDocMeta(id: string, params: Partial<Pick<IDocDetail, 'name' | 'icon'>>) {
        return this._post<IBaseResponse>('/doc/document/modify', { id, ...params });
    }

    modifyFolderMeta(id: string, params: Partial<Pick<IFolderDetail, 'name' | 'description' | 'icon'>>) {
        return this._post<IBaseResponse>('/doc/folder/modify', { id, ...params });
    }

    private _moveEntities(params: { ids: string[], targetId: string }) {
        return this._post<IBaseResponse & {
            successIds: number,
            failIds: number,
            failedMessage: { errorMessage: string, id: string }[]
        }>('/doc/common/batchMove', params);
    }

    moveEntities(entityList: IEntity[], entityType: EntityType): Promise<{
        successIds: string[],
        successEntities: IEntity[],
        failIds: string[],
        targetPath: IEntity[],
        failedMessage: { errorMessage: string, id: string }[]
    }> {
        let isLoading = false;
        return new Promise((resolve, reject) => {
            const { cpr: dialog, overlayRef: ovr } = this.overlayService.showDialog(DlEntityMoveDialog, {
                onClose: () => {
                    if (isLoading) return false;
                    reject('取消移动');
                    return true;
                }
            });

            dialog.setInput('entityList', entityList);
            dialog.setInput('entityType', entityType);

            merge(dialog.instance.onClose, ovr.backdropClick()).pipe(takeUntilDestroyed(dialog.instance.destroyRef)).subscribe(() => {
                if (isLoading) return;
                ovr.dispose();
                reject('取消移动');
            });

            dialog.instance.onOk.pipe(takeUntilDestroyed(dialog.instance.destroyRef)).subscribe(async path => {
                if (isLoading) return;
                if (!path.length) {
                    reject('请选择目标文件夹');
                    return;
                }
                dialog.setInput('isLoading', isLoading = true);

                const targetId = path.at(-1)!.id;

                const ids = entityList.map(v => v.id);

                try {
                    const res = await this._moveEntities({ ids, targetId: targetId });

                    const failIds = new Set(res.failedMessage.map(msg => msg.id));
                    const successIds = ids.filter(id => !failIds.has(id));
                    const successEntities = entityList.filter(v => !failIds.has(v.id));

                    entityList.forEach(entity => {
                        entity.nodeType = entityType;
                    });

                    setTimeout(() => {
                        resolve({
                            successIds,
                            failIds: [...failIds],
                            targetPath: path,
                            successEntities,
                            failedMessage: res.failedMessage
                        });
                        if (successIds.length) {
                            this.entitiesUpdate$.next({
                                type: 'move',
                                entities: successEntities,
                                targetId: targetId
                            });
                        }
                        ovr.dispose();
                    }, 300);
                } catch (e) {
                    dialog.setInput('isLoading', isLoading = false);
                    resolve({
                        successIds: [],
                        failIds: ids,
                        targetPath: [],
                        successEntities: [],
                        failedMessage: [{errorMessage: e.error, id: ids[0]}]
                    })
                }

            });

        });
    }

    showEntityDetail(entityDetail: IFolderEntity | IDocumentEntity, localUserRole: PermissionRole, entityType: EntityType, onDetailChange?: (form: Partial<IFolderDetail | IDocDetail>) => void) {
        const { cpr } = this.overlayService.showDialog(EntityDetailDialogComponent);
        cpr.setInput('entityDetail', entityDetail);
        cpr.setInput('entityType', entityType);
        cpr.setInput('localUserRole', localUserRole);
        cpr.instance.detailChange.pipe(takeUntilDestroyed(cpr.instance.destroyRef)).subscribe(form => {
            onDetailChange?.(form);
        });
    }

    // -------------------- 增删改 END --------------------

    // -------------------- 搜索相关 --------------------
    async globalKeywordSearch(params: IBasePageQueryParams & {
        keyword: string,
        filters?: Array<'title' | 'tag'>,
        nodeTypes?: EntityType[]
    }, cancelToken?: Subject<void>) {
        return await this._post<IPagingListResponse<IEntity>>('/doc/search/keyword', params, cancelToken);
    }

    setLikeEntity(id: string, value: boolean) {
        return this._post<IBaseResponse>('/doc/behavior/like', { id, isCancel: !value });
    }

    setFavoriteEntity(id: string, value: boolean) {
        return this._post<IBaseResponse>('/doc/behavior/favorite', { id, isCancel: !value });
    }

    getBehaviorDetail(params: { id: string, behaviorType: DocBehaviorType }) {
        return this._post<IBaseResponse & { count: number, users: IDlUser[] }>(`/doc/behavior/info`, params);
    }

    //------------------- 搜索相关 END --------------------

    // -------------------- 标签相关 --------------------
    createTag(params: Omit<ITag, 'id'> & { parentId: string, spaceId: string }) {
        return this._post<IBaseResponse & { result: string }>('/doc/tag/create', params);
    }

    modifyTag(params: Partial<Pick<ITag, 'id' | 'name' | 'color'> & { spaceId: string }>) {
        return this._post<IBaseResponse>('/doc/tag/update', params);
    }

    deleteTag(params: { id: string, spaceId: string }) {
        return this._post<IBaseResponse>('/doc/tag/delete', params);
    }

    // getChildrenTags(params: { id: string }) {
    //     return this._post<IBaseListResponse<ITag>>('/doc/tag/find', params);
    // }
    //
    // getTagTree(params: { id: string }) {
    //     return this._post<IBaseListResponse<ITag>>('/api/tag/findTree', params);
    // }

    getTagTreeBySpace(params: { id: string }) {
        return this._post<IBaseResponse & ITag>('/doc/tag/getSpaceTag', params);
    }

    bindTag(params: { id: string, tagId: string }) {
        return this._post<IBaseResponse>('/doc/tag/bind', params);
    }

    unbindTag(params: { id: string, tagId: string }) {
        return this._post<IBaseResponse>('/doc/tag/notBind', params);
    }

    getTagsByEntity(params: { id: string }) {
        return this._post<IBaseListResponse<ITag>>('/doc/tag/getNodeTag', params);
    }

    modifyEntityTags(entity: IEntity): Promise<ITag[]> {
        const { cpr } = this.overlayService.showDialog(DlTagBindDialog);
        cpr.instance.entity = entity;
        return new Promise((resolve, reject) => {
            cpr.instance.destroyRef.onDestroy(() => {
                resolve(cpr.instance.tags);
            });
        });
    }

    // ------------------- 标签相关 END --------------------

    // ------------------- 回收站 --------------------
    getRecycleBinList(params: IBasePageQueryParams) {
        return this._post<IBaseListResponse<IEntity>>('/doc/trash/list', params);
    }


}
