import { Pipe } from '@angular/core';
import { PermissionRole } from '@/pages/docs/services';

@Pipe({
    name: 'roleLabel',
    standalone: true
})
export class RoleLabelPipe {
    transform(role: PermissionRole): string {
        switch (role) {
            case PermissionRole.owner:
                return '归属人';
            case PermissionRole.manager:
                return '可管理';
            case PermissionRole.editor:
                return '可编辑';
            case PermissionRole.reader:
                return '可阅读';
        }
    }
}
