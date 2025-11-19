import {IDlTabItem, IMenu} from "../../components";

export const SPACE_ROUTES: IMenu[] = [
  {
    title: '首页',
    route: '',
    icon: 'bc_file-multiple',
  },
  // {
  //   title: '关系图谱',
  //   route: '/chart',
  //   icon: 'bc_data-01',
  // },
  // {
  //   title: '任务',
  //   route: '/tasks',
  //   icon: 'bc_check-ok',
  // },
  {
    title: '标签',
    route: 'tags',
    icon: 'bc_a-shuqian',
  },
  // {
  //   title: '分享内容',
  //   route: '/shared',
  //   icon: 'bc_share',
  // },
]

export const ClassifyDocTabs: IDlTabItem[] = [
  {
    id: 'all',
    label: '所有类型',
  },
  {
    id: 'doc',
    label: '文档',
  },
  // {
  //   id: 'sheet',
  //   label: '表格',
  // },
  // {
  //   id: 'mind',
  //   label: '思维导图',
  // }
]

