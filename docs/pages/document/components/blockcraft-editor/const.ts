import {
    AttachmentBlockSchema,
    BlockquoteBlockSchema, BookmarkBlockSchema,
    BulletBlockSchema, CalloutBlockSchema, CaptionBlockSchema, CodeBlockSchema, DividerBlockSchema,
    EmbedConverter, FigmaEmbedBlockSchema, ImageBlockSchema,
    InlineManager, JuejinEmbedBlockSchema, MermaidBlockSchema, MermaidTextareaBlockSchema,
    OrderedBlockSchema,
    ParagraphBlockSchema,
    RootBlockSchema,
    SchemaManager, TableBlockSchema, TableCellBlockSchema, TableRowBlockSchema, TodoBlockSchema
} from '@ccc/blockcraft';
import {
    PageDividerBlockSchema
} from './blocks/page-divider-block';

export const TASK_EMBED_CONVERTER: EmbedConverter = {
    toView: (embed) => {
        const span = document.createElement('span');
        span.textContent = embed.insert['task'] as string;
        InlineManager.setAttrs(span, embed.attributes);
        return span;
    },
    toDelta: (ele) => {
        return {
            insert: { task: ele.textContent! },
            attributes: {
                'd:taskId': ele.getAttribute('data-task-id')!
            }
        };
    }
};

export const MENTION_EMBED_CONVERTER: EmbedConverter = {
    toView: (embed) => {
        const span = document.createElement('span');
        span.textContent = embed.insert['mention'] as string;
        InlineManager.setAttrs(span, embed.attributes!);
        return span;
    },
    toDelta: (ele) => {
        return {
            insert: { mention: ele.textContent! },
            attributes: InlineManager.getAttrs(ele)
        };
    }
};

export const OLD_LINK_EMBED_CONVERTER: EmbedConverter = {
    toView: (embed) => {
        const a = document.createElement('a');
        a.textContent = embed.insert['link'] as string;
        a.target = '_blank';
        a.href = embed.attributes?.['d:href'] as string;
        InlineManager.setAttrs(a, embed.attributes!);
        return a;
    },
    toDelta: (ele) => {
        return {
            insert: { link: ele.textContent! },
            attributes: InlineManager.getAttrs(ele)
        };
    }
};

TableCellBlockSchema.metadata.excludeChildren.push('page-divider')

export const SchemaStore = new SchemaManager([
    RootBlockSchema, ParagraphBlockSchema,
    OrderedBlockSchema, BulletBlockSchema, TodoBlockSchema,
    BlockquoteBlockSchema, CalloutBlockSchema, CodeBlockSchema,
    DividerBlockSchema,

    PageDividerBlockSchema,

    ImageBlockSchema, MermaidBlockSchema, MermaidTextareaBlockSchema,
    TableBlockSchema, TableRowBlockSchema, TableCellBlockSchema, AttachmentBlockSchema, BookmarkBlockSchema,
    FigmaEmbedBlockSchema, JuejinEmbedBlockSchema,
    CaptionBlockSchema,

]);
