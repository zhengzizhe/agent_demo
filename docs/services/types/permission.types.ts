import { IDlUser } from './entity.types';

export enum VisibilityRange {
    PERSONAL = 1,
    COLLABORATOR = 2,
    ENTERPRISE_AND_COLLABORATOR = 3,
    EVERYONE = 4
}

export enum PermissionRole {
    owner = 1000,
    manager = 800,
    editor = 600,
    reader = 400,
    visitor = 200,
    none = 0
}

export interface IAccessConfig {
    visibility: VisibilityRange;
    visibilityRole: PermissionRole;
    password?: string;
    needPassword?: boolean;
    company?: {
        id: string;
        name: string;
    }
}

export enum EntityAccessCode {
    AUTHORIZED = 0,
    NEED_PASSWORD = 1,
    NO_AUTH = 2,
    NOT_FOUND = 3
}

export class IAccessCheckResult {
    code: EntityAccessCode;
    owner: IDlUser
    desc: string
}

export interface ICollaborator {
    // 相对于文档的身份
    role: PermissionRole
    // 组织架构的角色
    user: {
        type: string,
        id: string;
        name: string;
        userId?: string;
        userName?: string;
        orgName: string,
        orgId: string,
        deptName?: string,
        deptId?: string,
        isUser: boolean
    }
    // 是否是继承（false为继承）
    direct?: boolean
}
