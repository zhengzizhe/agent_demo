import { BlockNodeType, EditableBlockNative, generateId } from '@ccc/blockcraft';
import { IBlockSchemaOptions } from '@ccc/blockcraft/framework/block-std/schema/block-schema';
import {
    GuidanceBlockComponent
} from './guidance.block';

export interface GuidanceBlockModel extends EditableBlockNative {
    flavour: 'guidance',
    nodeType: BlockNodeType.editable,
    props: {
    } & EditableBlockNative['props']
}

export const GuidanceBlockSchema: IBlockSchemaOptions<GuidanceBlockModel> = {
    flavour: 'guidance',
    nodeType: BlockNodeType.editable,
    component: GuidanceBlockComponent,
    createSnapshot: () => {
        return {
            id: generateId(),
            flavour: 'guidance',
            nodeType: BlockNodeType.void,
            props: {
                depth: 0
            },
            meta: {},
            children: []
        }
    },
    metadata: {
        version: 1,
        label: "分页符",
        svgIcon: "bc_fengexian-color",
        icon: "bc_icon bc_fengexian-color"
    }
}

declare global {
    namespace BlockCraft {
        interface IBlockComponents {
            guidance: GuidanceBlockComponent
        }

        interface IBlockCreateParameters {
            guidance: []
        }
    }
}
