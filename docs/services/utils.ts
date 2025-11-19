import { EntityTypeStr, IEntityLocalUser, OpTypesStr, PermissionRole } from './types';
import {
    OpMenuItem_bindTag, OpMenuItem_cancelFavorite,
    OpMenuItem_copyLink, OpMenuItem_delete,
    OpMenuItem_detail, OpMenuItem_duplicate, OpMenuItem_favourite,
    OpMenuItem_move,
    OpMenuItem_permission,
    OpMenuItem_share
} from '@/pages/docs/services/const';

export const DOC_LINK_HOST = 'http://doc.cses7.com/';

export const isEntityLink = (link: string) => {
    return /^http:\/\/doc\.cses7\.com\/(doc|folder)\//.test(link)
}

export const copyEntityLink = (id: string, type: EntityTypeStr) => {
    return navigator.clipboard.writeText(`${DOC_LINK_HOST}${type}/${id}`);
};

export const parseEntityLink = (link: string) => {
    if(!isEntityLink(link)) return null;
    const [type, id] = link.replace(DOC_LINK_HOST, '').split('/');
    switch (type) {
        case 'doc':
            return `document/${id}`;
        case 'folder':
            return `folder/${id}?from=share&start=${id}`;
        default:
            return null;
    }
};

export const isDocVersionLink = (link: string) => {
    return /^http:\/\/doc\.cses7\.com\/docv\/(\d|\w)+\?version=/.test(link)
}

export const copyDocVersionLink = (id: string, version: string) => {
    return navigator.clipboard.writeText(`${DOC_LINK_HOST}docv/${id}?version=${version}`);
};

export const parseDocVersionLink = (link: string) => {
    if(!isDocVersionLink(link)) return null;
    const [id, query] = link.replace(DOC_LINK_HOST + 'docv/', '').split('?');
    return `document-version/${id}?version=${query.split('=')[1]}`;
};

export const getFilteredOpMenuList = (role: PermissionRole, localUser: IEntityLocalUser, disableOps?: OpTypesStr[]) => {
    const opMenus = [
        OpMenuItem_share,
        OpMenuItem_copyLink,
        OpMenuItem_bindTag,
        OpMenuItem_move,
        OpMenuItem_detail,
        OpMenuItem_delete
    ];

    opMenus.splice(4, 0, localUser?.behavior?.favorite ? OpMenuItem_cancelFavorite : OpMenuItem_favourite);

    const opMenuRoleMap = {
        [OpMenuItem_share.id]: PermissionRole.none,
        [OpMenuItem_copyLink.id]: PermissionRole.none,
        [OpMenuItem_duplicate.id]: PermissionRole.editor,
        [OpMenuItem_bindTag.id]: PermissionRole.manager,
        [OpMenuItem_move.id]: PermissionRole.manager,
        [OpMenuItem_detail.id]: PermissionRole.none,
        [OpMenuItem_permission.id]: PermissionRole.manager,
        [OpMenuItem_delete.id]: PermissionRole.owner,
        [OpMenuItem_favourite.id]: PermissionRole.none,
        [OpMenuItem_cancelFavorite.id]: PermissionRole.none
    };

    const opMenuList = opMenus.filter(item => !disableOps?.includes(item.id as OpTypesStr) && opMenuRoleMap[item.id] <= role);
    const divideIndex = opMenuList.findIndex(item => item.id === OpMenuItem_detail.id) - 1;
    opMenuList[divideIndex].divide = true;
    return opMenuList;
};
