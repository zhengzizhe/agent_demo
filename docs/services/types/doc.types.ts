import { IDlUser, IDocumentEntity, IEntity } from '@/pages/docs/services';

export interface IDocConfigs {
    pageWidth: 'default' | 'wide' | 'full' | 'auto';
    fontSize: 'smaller' | 'small' | 'default' | 'large' | 'larger';
}

export enum DocBlockDiffMode {
    Update = 'Update',
    Delete = 'Delete',
    Insert = 'Insert',
}
export type DocBlockDiffModeStr = `${DocBlockDiffMode}`

export const DocBehaviors = {
    subscribe: 'subscribe',
    like: 'like',
    favorite: 'favorite'
} as const

export type DocBehaviorType = typeof DocBehaviors[keyof typeof DocBehaviors]

export interface ITag {
    id: string;
    name: string;
    color: string;
    backColor: string
    parentId?: string
    children?: ITag[]

    [key: string]: any
}

export interface IDocVersionInfo {
    id: string;
    // 版本名称
    name: string
    // 版本文档名称
    docName: string;
    description?: string;
    icon: IEntity['icon']
    banner: IDocumentEntity['banner']
    // 版本snapshot base64数据
    data: string
    version: string;
    publishedAt: number
    publisher: IDlUser
}
