import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    EventEmitter, Inject,
    Input,
    Output
} from '@angular/core';
import { DocBehaviors, EntityService, IDlBaseDialog, IDlUser, IDocDetail } from '@/pages/docs/services';
import { AppContext } from '@ccc/core-common';
import { AvatarPipe, CsesContextService } from '@ccc/cses-common';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';

@Component({
    selector: 'dl-doc-like-dialog',
    templateUrl: './doc-like-dialog.html',
    styleUrls: ['./doc-like-dialog.scss'],
    standalone: true,
    imports: [
        AvatarPipe,
        NzTooltipDirective
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlDocLikeDialogComponent implements IDlBaseDialog {
    @Input()
    docDetail: IDocDetail;

    get liked() {
        return this.docDetail?.localUser?.behavior?.liked;
    }

    @Output()
    onClose = new EventEmitter();

    likedUserList: Pick<IDlUser, 'userId' | 'userName'>[] = [];
    likedTotal = 0;

    isLoading = false;

    constructor(
        readonly destroyRef: DestroyRef,
        private entityService: EntityService,
        private cdr: ChangeDetectorRef,
        @Inject(AppContext) public ctx: CsesContextService
    ) {
    }

    async ngOnInit() {
        await this.getLikeUserList();
        this.cdr.markForCheck();
    }

    onClick($event: any) {
        if (this.isLoading) return;
        this.isLoading = true;
        this.entityService.setLikeEntity(this.docDetail.id, !this.liked).then(() => {
            this.docDetail.localUser.behavior.liked = !this.liked;
            // if(this.liked) {
            //     this.likedUserList.push({
            //         userId: this.ctx.userInfo.userId,
            //         userName: this.ctx.userInfo.userName,
            //     })
            //     this.likedTotal += 1
            // } else {
            this.getLikeUserList();
            // this.likedUserList = this.likedUserList.filter(user => user.userId !== this.ctx.userInfo.userId)
            // this.likedTotal -= 1
            // }
            // this.getLikeUserList()
            // this.cdr.markForCheck()
        }).catch(e => {
            console.error(e.error || '点赞失败');
        }).finally(() => {
            this.isLoading = false;
        });
    }

    getLikeUserList() {
        this.entityService.getBehaviorDetail({
            id: this.docDetail.id,
            behaviorType: DocBehaviors.like
        }).then(res => {
            this.likedUserList = res.users;
            this.likedTotal = res.count;
            this.cdr.markForCheck();
        });
    }
}
