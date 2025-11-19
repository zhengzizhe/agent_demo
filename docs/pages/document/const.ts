import { IDlContextMenuItem } from '@/pages/docs/services';

export enum EditMode {
    edit = 'edit',
    read = 'read'
}

export interface IEditModeItem extends IDlContextMenuItem{
    value: EditMode;
}

export const EditModeList: IEditModeItem[] = [
    {
        id: EditMode.read,
        name: '阅读模式',
        value: EditMode.read,
        label: '阅读',
        icon: 'bc_chakan'
    },
    {
        id: EditMode.edit,
        name: '编辑模式',
        value: EditMode.edit,
        label: '编辑',
        icon: 'bc_bianjimoshi'
    }
];
