import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { DocsPermissionService, EntityAccessCode, RouterService } from '@/pages/docs/services';
import { CsesRenderService } from '@ccc/cses-common';

@Injectable()
export class ActivateGuard implements CanActivate {

    constructor(
        private permissionService: DocsPermissionService,
        private router: RouterService,
        private render: CsesRenderService
    ) {
    }

    async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const id = route.params['id'];

        const res = await this.permissionService.accessCheck(id);
        switch (res.code) {
            case EntityAccessCode.AUTHORIZED:
                return true;
            case EntityAccessCode.NEED_PASSWORD:
            case EntityAccessCode.NO_AUTH:
                setTimeout(() => {
                    this.router.navigateToNewTab(`no-auth?id=${id}&code=${res.code}&uri=${state.url}&owner=${res.owner.name}&ownerid=${res.owner.id}`);
                })
                return false;
            case EntityAccessCode.NOT_FOUND:
                throw res;
            default:
                this.render.error(res.desc || '未知错误');
                return false;
        }

        // try {
        //
        // } catch (e) {
        //     this.render.error(e.error);
        //     return false;
        // }

    }

}
