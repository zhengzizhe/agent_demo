import { Node, mergeAttributes } from '@tiptap/core'

/**
 * 文档链接扩展
 * 支持链接到其他文档
 */
export const DocumentLink = Node.create({
  name: 'documentLink',

  addOptions() {
    return {
      HTMLAttributes: {},
    }
  },

  group: 'inline',

  inline: true,

  selectable: false,

  atom: true,

  addAttributes() {
    return {
      documentId: {
        default: null,
        parseHTML: element => element.getAttribute('data-document-id'),
        renderHTML: attributes => {
          if (!attributes.documentId) {
            return {}
          }
          return {
            'data-document-id': attributes.documentId,
          }
        },
      },
      documentName: {
        default: null,
        parseHTML: element => element.getAttribute('data-document-name'),
        renderHTML: attributes => {
          if (!attributes.documentName) {
            return {}
          }
          return {
            'data-document-name': attributes.documentName,
          }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'span[data-type="document-link"]',
      },
    ]
  },

  renderHTML({ node, HTMLAttributes }) {
    return [
      'span',
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {
        'data-type': 'document-link',
        class: 'document-link',
      }),
      `📄 ${node.attrs.documentName || '未命名文档'}`,
    ]
  },

  addCommands() {
    return {
      setDocumentLink: (attributes) => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs: attributes,
        })
      },
    }
  },
})


