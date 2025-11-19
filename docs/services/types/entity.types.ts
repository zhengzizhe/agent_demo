import { PermissionRole } from './permission.types';

export interface IDlUser {
    id: string;
    name: string;
    userId?: string;
    userName?: string;
    deptName?: string;
    orgName?: string;
}

export enum EntityType {
    Space = 'space',
    Folder = 'folder',
    Document = 'doc',
    File = 'file'
}

export enum EntityTypeName {
    space = '空间',
    folder = '文件夹',
    doc = '文档',
}

export type EntityTypeStr = `${EntityType}`;

export type DocumentType = 'text' | 'image' | 'video' | 'audio' | 'pdf' | 'ppt' | 'excel' | 'word' | 'other'
export type SpaceType = 'personal' | 'enterprise' | 'cses' | 'custom'

export interface IEntityLocalUser {
    role: PermissionRole;
    userId?: string;
    userName?: string;
    behavior?: {
        favorite?: boolean;
        subscribe?: boolean;
        liked?: boolean;
    }
}

export interface IEntity {
    nodeType: EntityTypeStr;
    id: string;
    name: string;
    icon?: {
        type: string
        value: string
    };
    creator?: IDlUser;
    owner?: IDlUser;
    editor?: IDlUser;

    [key: string]: any;
}

export interface ISpaceEntity extends IEntity {
    spaceType: SpaceType;
}

export interface IFolderEntity extends IEntity {
    createdAt: number;
    updatedAt?: number;
    spaceId: string;
    subCount: number;
    subFolderCount?: number
    localUser?: IEntityLocalUser;

    [key: string]: any;
}

export interface ISpaceDetail extends ISpaceEntity {
    localUser: IEntityLocalUser;
}

export interface IFolderDetail extends IFolderEntity {
    spaceId: string;
    spaceType: SpaceType;
    localUser: IEntityLocalUser;
    description: string;
}

export interface IDocumentEntity extends IEntity {
    spaceId: string;
    createdAt: number;
    updatedAt: number;
    openedAt: number;
    editedAt: number;
    documentType: DocumentType;
    share?: boolean;
    template?: boolean;
    localUser?: IEntityLocalUser;
}

export interface IDocDetail extends IDocumentEntity {
    // 创建人ID
    userId: string;
}

