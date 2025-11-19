import { IOpMenuItem } from '@/pages/docs/services/types';

export const OpMenuItem_share: IOpMenuItem = {
    id: 'share',
    name: 'share',
    label: '分享',
    icon: 'bc_share',
}

export const OpMenuItem_copyLink: IOpMenuItem = {
    id: 'copyLink',
    name: 'copyLink',
    label: '复制链接',
    icon: 'bc_fuzhilianjie',
    divide: true
}

export const OpMenuItem_duplicate: IOpMenuItem = {
    id: 'duplicate',
    name: 'duplicate',
    label: '创建副本',
    icon: 'bc_file-multiple',
}

export const OpMenuItem_move: IOpMenuItem = {
    id: 'move',
    name: 'move',
    label: '移动到',
    icon: 'bc_file-up',
}

export const OpMenuItem_bindTag: IOpMenuItem = {
    id: 'bindTag',
    name: 'bindTag',
    label: '绑定标签',
    icon: 'bc_a-shuqian',
}

export const OpMenuItem_favourite: IOpMenuItem = {
    id: 'favorite',
    name: 'favorite',
    label: '收藏',
    icon: 'bc_shoucang',
}

export const OpMenuItem_cancelFavorite: IOpMenuItem = {
    id: 'cancelFavorite',
    name: 'cancelFavorite',
    label: '取消收藏',
    icon: 'bc_shoucang-fill',
}

export const OpMenuItem_detail: IOpMenuItem = {
    id: 'detail',
    name: 'detail',
    label: '简介',
    icon: 'bc_check-square-broken'
}

export const OpMenuItem_permission: IOpMenuItem = {
    id: 'permission',
    name: 'permission',
    label: '权限设置',
    icon: 'bc_settings'
}

export const OpMenuItem_delete: IOpMenuItem = {
    id: 'delete',
    name: 'delete',
    label: '删除',
    icon: 'bc_shanchu',
    className: 'dl-error-btn'
}
