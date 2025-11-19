import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { AvatarPipe } from '@ccc/cses-common';
import { NgIf } from '@angular/common';
import { IDlUser } from '@/pages/docs/services';

@Component({
    selector: 'dl-user-profile',
    templateUrl: './user-profile.html',
    styleUrls: ['./user-profile.scss'],
    imports: [
        AvatarPipe,
        NgIf
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DlUserProfileComponent {

    @Input({ required: true })
    user!: IDlUser;

    @Input()
    avatarSize = 28;


}
