import { IDlContextMenuItem } from '@/pages/docs/services';

export enum OpTypes {
    share = 'share',
    copyLink = 'copyLink',
    move = 'move',
    detail = 'detail',
    permission = 'permission',
    delete = 'delete',
    duplicate = 'duplicate',
    bindTag = 'bindTag',
    favorite = 'favorite',
    cancelFavorite = 'cancelFavorite',
    restore = 'restore',
}

export type OpTypesStr = `${OpTypes}`;

export interface IOpMenuItem extends IDlContextMenuItem {
    name: OpTypesStr
}
