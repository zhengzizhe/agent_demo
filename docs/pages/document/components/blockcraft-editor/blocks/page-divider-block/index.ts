import { PageDividerBlockComponent } from './page-divider.block';
import { BlockNodeType, generateId, NoEditableBlockNative } from '@ccc/blockcraft';
import { IBlockSchemaOptions } from '@ccc/blockcraft/framework/block-std/schema/block-schema';

export interface PageDividerBlockModel extends NoEditableBlockNative {
  flavour: 'page-divider',
  nodeType: BlockNodeType.void,
  props: {
    style?: string
    size?: string
  }
}

export const PageDividerBlockSchema: IBlockSchemaOptions<PageDividerBlockModel> = {
  flavour: 'page-divider',
  nodeType: BlockNodeType.void,
  component: PageDividerBlockComponent,
  createSnapshot: () => {
    return {
      id: generateId(),
      flavour: 'page-divider',
      nodeType: BlockNodeType.void,
      props: {},
      meta: {},
      children: []
    }
  },
  metadata: {
    version: 1,
    label: "分页符",
    svgIcon: "bc_fenyefu",
    icon: "bc_icon bc_fenyefu"
  }
}

declare global {
  namespace BlockCraft {
    interface IBlockComponents {
      'page-divider': PageDividerBlockComponent
    }

    interface IBlockCreateParameters {
      'page-divider': []
    }
  }
}
