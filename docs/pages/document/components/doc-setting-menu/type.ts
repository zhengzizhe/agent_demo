import { PermissionRole } from '@/pages/docs/services';

export interface IDocSettingMenuItem {
    id: string
    name: string;
    tooltip?: string
    label: string;
    value: any
    icon?: string;
    type: 'switch' | 'dropdown' | 'none';
    children?: IDocSettingMenuItem[];
    // 上面设置分割线
    divider?: boolean;
    active?: boolean;
    role: PermissionRole
}
