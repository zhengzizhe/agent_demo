import { IEntity } from '@/pages/docs/services';

export type IEntitiesUpdate = {
    type: 'delete'
    entities: IEntity[]
    parentId: string
} | {
    type: 'new'
    parentId: string
    entities: IEntity[]
} | {
    type: 'move'
    entities: IEntity[]
    targetId: string
} | {
    type: 'update'
    entities: IEntity[]
    filed: string
}
