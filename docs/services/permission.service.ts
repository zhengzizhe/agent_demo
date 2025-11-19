import { Injectable, Injector } from '@angular/core';
import { HttpService } from './http.service';
import { CsesHttpService, CsesRenderService } from '@ccc/cses-common';
import {
    EntityAccessCode, EntityType,
    IAccessCheckResult,
    IAccessConfig, IBaseListResponse,
    IBaseResponse, ICollaborator, IDlUser, IEntity,
    PermissionRole
} from '@/pages/docs/services/types';
import { ShareModalComponent } from '@/pages/docs/components/share-modal/share-modal';
import { DocsOverlayService } from './overlay.service';

export const DOC_SHARE_PWD_REG = /^[^\u4e00-\u9fa5]{6,18}$/;

@Injectable()
export class DocsPermissionService extends HttpService {
    constructor(
        protected readonly http: CsesHttpService,
        private overlayService: DocsOverlayService,
        private render: CsesRenderService
    ) {
        super(http);
    }

    accessCheck(id: string) {
        return this._post<IBaseResponse & IAccessCheckResult>('/doc/access/check', { id });
    }

    accessVerifyPassword(id: string, password: string) {
        return this._post<IBaseResponse & { code: EntityAccessCode }>('/doc/access/verify', { id, password });
    }

    getShareConfig(id: string) {
        return this._post<IBaseResponse & IAccessConfig>('/doc/share/record', { id });
    }

    // 修改可见性
    modifyShareVisibility(id: string, config: Pick<IAccessConfig, 'visibility' | 'visibilityRole'>) {
        return this._post<IBaseResponse & IAccessConfig>('/doc/common/changeVisibility', { id, ...config });
    }

    modifySharePassword(id: string, config: Pick<IAccessConfig, 'password' | 'needPassword'>) {
        return this._post<IBaseResponse>('/doc/share/changePassword', { resourceId: id, ...config });
    }

    getCollaboratorList(id: string) {
        return this._post<IBaseListResponse<ICollaborator>>('/doc/common/collaborator/list', { id });
    }

    addCollaborators(id: string, collaborators: ICollaborator[]) {
        return this._post<IBaseResponse>('/doc/common/collaborator/add', { id, collaborators });
    }

    modifyCollaboratorRole(params: { id: string, user: ICollaborator['user'], role: PermissionRole }) {
        return this._post<IBaseResponse>('/doc/common/collaborator/update', params);
    }

    removeCollaborator(id: string, user: ICollaborator['user']) {
        return this._post<IBaseResponse>('/doc/common/collaborator/remove', { id, user });
    }

    async getEntityLocalUserRole(id: string) {
        try {
            const res = await this._post<IBaseResponse & { role: PermissionRole; }>('/doc/permission', { id });
            return res.role;
        } catch (e) {
            this.render.error(e.error || '获取权限失败');
            return PermissionRole.visitor
        }
    }

    async showShareModal(params: { entity: IEntity, entityType: EntityType, owner: IDlUser, localUserRole: PermissionRole }) {
        const { entity, entityType, owner, localUserRole } = params;
        let accessConfig: IAccessConfig;
        try {
            accessConfig = await this.getShareConfig(entity.id);
        } catch (e) {
            this.render.error(e.error || '获取分享信息失败');
            throw new Error(e);
        }
        const { cpr } = this.overlayService.showDialog(ShareModalComponent);

        cpr.setInput('entity', entity);
        cpr.setInput('entityType', entityType);
        cpr.setInput('owner', owner);
        cpr.setInput('localUserRole', localUserRole);
        cpr.setInput('accessConfig', accessConfig);
    }


}
