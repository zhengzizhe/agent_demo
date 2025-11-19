export interface IBaseResponse {
    serverName: string;
    isSuccess: boolean;
    error?: string;

    [key: string]: any;
}

export interface IBaseListResponse<T = any> extends IBaseResponse {
    items: T[];
}

export interface IPagingListResponse<T = any> extends IBaseListResponse<T> {
    totalCount: number;
    pageSize?: number;
    pageNumber?: number;
}

export interface IBasePageQueryParams {
    pageNumber?: number;
    pageSize?: number;
    totalCount?: number;

    [key: string]: any;
}

export interface IBaseErrorResponse {
    error: string;

    [key: string]: any;
}
