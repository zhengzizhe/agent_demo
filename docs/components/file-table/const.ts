import { IFileTableHeadItem } from './types';
import { IFileOpButtonItem } from '@/pages/docs/components';

export const fileTableHead_documentType: IFileTableHeadItem = {
    value: 'documentType',
    label: '所有类型',
    type: 'filter',
    activeFilter: null,
    filterOptions: [
        {
            value: null,
            label: '所有类型'
        },
        {
            value: 'text',
            label: '文档'
        }
    ]
};

export const fileTableHead_owner: IFileTableHeadItem = {
    value: 'owner',
    label: '不限归属',
    type: 'filter',
    activeFilter: null,
    filterOptions: [
        {
            value: null,
            label: '不限归属'
        },
        {
            value: 'self',
            label: '归属于我'
        }
    ]
};

export const fileTableHead_owner_none: IFileTableHeadItem = {
    value: 'owner',
    label: '归属人',
    type: 'none'
};

export const fileTableHead_timeSort: IFileTableHeadItem = {
    value: 'timeSort',
    label: '修改时间',
    type: 'filter',
    activeFilter: 'editedAt',
    filterOptions: [
        {
          value: 'editedAt',
          label: '修改时间',
        },
        {
            value: 'updatedAt',
            label: '更新时间'
        },
        {
            value: 'createdAt',
            label: '创建时间'
        }
    ]
};

export const fileTableHead_user_ops: IFileTableHeadItem = {
    value: 'timeSort',
    label: '最近打开',
    type: 'filter',
    activeFilter: 'openedAt',
    filterOptions: [
        {
            value: 'openedAt',
            label: '最近打开'
        },
        {
            value: 'editedAt',
            label: '最近编辑'
        }
    ]
};

export const FileTableHeader: IFileTableHeadItem[] = [
    fileTableHead_documentType,
    fileTableHead_owner,
    fileTableHead_timeSort
];

export const FileOpButton_clearChecked: IFileOpButtonItem = {
    className: 'btn-clear',
    icon: 'bc_quxiaoxuanzhong',
    label: '取消选中',
    value: 'clearChecked',
    onClick() {
        this.clearChecked();
    }
};

export const FileOpButton_deleteDocs: IFileOpButtonItem = {
    className: 'btn-delete',
    icon: 'bc_shanchu',
    label: '删除',
    value: 'deleteDocs',
    onClick() {
        this.deleteDocs(this.getCheckedItems()).then(() => {
            this.clearChecked();
        })
    }
};

export const FileOpButton_moveDocs: IFileOpButtonItem = {
    className: 'btn-move',
    icon: 'bc_file-up',
    label: '移动',
    value: 'moveDocs',
    onClick() {
        this.moveEntities(this.getCheckedItems())
    }
};
