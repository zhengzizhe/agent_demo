import { IDlTabItem, IFileOpButtonItem } from '@/pages/docs/components';

export const FileOpButton_removeRecent: IFileOpButtonItem = {
    className: 'btn-removeRecent',
    icon: 'bc_minus-circle',
    label: '从主页列表删除',
    value: 'removeRecent',
    onClick() {
        this.entityService.removeFromHomeView(Array.from(this.checkedList)).then(res => {
            this.render.success('已移除');
            this.itemList = this.itemList.filter(item => !this.checkedList.has(item.id));
            this.pageQueryParams.totalCount -= this.checkedList.size;
            this.clearChecked();
        });
    }
};

export const HomeFileTabs: IDlTabItem[] = [
    {
        id: 'recent',
        label: '最近访问'
    },
    {
        id: 'favorite',
        label: '我收藏的',
        // disable: true
    }
];
