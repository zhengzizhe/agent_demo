export type MentionType = 'user' | 'doc'

export interface IMentionData {
    id: string
    name: string

    [key: string]: any
}

export interface IMentionResponse {
    list: IMentionData[]

    [key: string]: any
}

export interface IMentionRequest {
    (keyword: string, type: MentionType): Promise<IMentionResponse>
}
