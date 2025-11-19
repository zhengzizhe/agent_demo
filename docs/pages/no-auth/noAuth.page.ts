import { Component, DestroyRef, Inject } from '@angular/core';
import { CsesButtonComponent, CsesContextService, CsesRenderService } from '@ccc/cses-common';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NzInputModule } from 'ng-zorro-antd/input';
import { DOC_SHARE_PWD_REG, DocsPermissionService, EntityAccessCode, RouterService } from '@/pages/docs/services';
import { ActivatedRoute } from '@angular/router';
import { AppContext } from '@ccc/core-common';
import { DlButton } from '@/pages/docs/components';

@Component({
    selector: 'no-auth-page',
    templateUrl: 'noAuth.page.html',
    styleUrls: ['./noAuth.page.scss'],
    standalone: true,
    imports: [
        CsesButtonComponent,
        NgIf,
        FormsModule,
        NzInputModule,
        DlButton
    ]
})
export class NoAuthPage {

    protected originUri: string = '';
    protected code: EntityAccessCode;
    protected entityId: string = '';
    protected pwd: string = '';

    protected ownerName = ''
    protected currentUser = this.ctx.userInfo

    constructor(
        private permissionService: DocsPermissionService,
        private router: RouterService,
        public readonly render: CsesRenderService,
        private destroyRef: DestroyRef,
        private activatedRoute: ActivatedRoute,
        @Inject(AppContext) public ctx: CsesContextService,
    ) {
        const { id, code, uri, owner, ownerid } = activatedRoute.snapshot.queryParams;
        this.code = parseInt(code) as EntityAccessCode;
        this.entityId = id;
        this.originUri = uri;
        this.ownerName = owner
    }

    ngOnInit() {
        this.router.layoutService.setTitle(`申请访问`)
    }

    backHomePage() {
        const curTab = this.router.layoutService.currentTab
        this.router.navigateToNewTab('').then(() => {
            this.router.layoutService.closeTab(curTab)
        })
    }

    async onPwdConfirm() {
        if (!this.pwd || !DOC_SHARE_PWD_REG.test(this.pwd)) return this.render.warning('密码格式不正确');
        try {
            const res = await this.permissionService.accessVerifyPassword(this.entityId, this.pwd);
            if (res.code === 0) {
                this.router.layoutService.navigateTo(this.originUri)
                // const curTab = this.router.layoutService.currentTab
                // this.router.navigateToNewTab(this.originUri).then(() => {
                //     this.router.layoutService.closeTab(curTab)
                // })
            }
        } catch (e) {
            return this.render.warning(e.error || '验证失败');
        }
    }

    protected readonly EntityAccessCode = EntityAccessCode;
}
