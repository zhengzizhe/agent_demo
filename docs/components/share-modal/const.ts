import { PermissionRole } from '@/pages/docs/services';

export const PERMISSION_OPTIONS: {
    value: PermissionRole,
    label: string,
}[] = [
    {
        value: PermissionRole.owner,
        label: '归属人'
    },
    {
        value: PermissionRole.manager,
        label: '可管理'
    },
    {
        value: PermissionRole.editor,
        label: '可编辑'
    },
    {
        value: PermissionRole.reader,
        label: '可阅读'
    }
];
