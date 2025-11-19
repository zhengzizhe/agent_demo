import { DocumentType, EntityType, EntityTypeStr } from '../../services';
import { EntityTableComponent, MixEntityTableComponent } from '@/pages/docs/components';

export interface IFilesQueryParams {
    documentType?: DocumentType | null;
    timeSort?: 'createdAt' | 'updatedAt' | 'openedAt';
    owner?: null | string;
    nodeType?: EntityType | EntityTypeStr
}

export interface IEntityTableOpButtonItem {
    label: string;
    icon: string;
    value: string;
    className?: string
    [key: string]: any
}

export interface IFileOpButtonItem extends IEntityTableOpButtonItem {
    onClick: (this: EntityTableComponent) => void
}

export interface IFilterOption {
    value: any;
    label: string;
}

export type IFileTableHeadItem =  {
    value: keyof IFilesQueryParams;
    label: string;
    type: 'sort' | 'filter' | 'none';
    sort?: 'asc' | 'desc';
    activeFilter?: IFilterOption['value'];
    filterOptions?: IFilterOption[];
    active?: boolean;
} | {
    value: string;
    label: string;
    type: 'none';
}
