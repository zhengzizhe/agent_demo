import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    ElementRef,
    Input,
    ViewChild
} from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { DlToggleHead } from '@/pages/docs/components';
import { KeyValuePipe } from '@angular/common';
import { FileSizePipe } from './file-size.pipe';
import Viewer from 'viewerjs';
import { NgxMasonryModule, NgxMasonryOptions } from 'ngx-masonry';
import * as Y from 'yjs';
import { downloadFile } from '@ccc/blockcraft';

interface IDocIMageResource {
    id: string,
    url: string,
}

interface IDocBookmarkResource {
    id: string,
    title?: string
    url: string
    image?: string
    icon?: string
}

interface IDocAttachmentResource {
    id: string,
    name: string
    url: string
    size: number
    icon: string
    type: string
}

@Component({
    selector: 'dl-editor-resource',
    templateUrl: './editor-resource.html',
    styleUrls: ['./editor-resource.scss'],
    imports: [
        MatIcon,
        DlToggleHead,
        KeyValuePipe,
        FileSizePipe,
        NgxMasonryModule
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditorResourceComponent {
    @Input({ required: true })
    doc: BlockCraft.Doc;

    imagesMap = new Map<string, IDocIMageResource>();
    bookmarksMap = new Map<string, IDocBookmarkResource>();
    attachmentsMap = new Map<string, IDocAttachmentResource>();

    isShowImages: boolean = true;
    isShowBookmarks: boolean = true;
    isShowAttachments: boolean = true;

    @ViewChild('imgList', { read: ElementRef }) imgList: ElementRef<HTMLElement>;

    masonryOptions: NgxMasonryOptions = {
        gutter: 8,
        // columnWidth: 120,
        itemSelector: '.img-item'
        // horizontalOrder: true
    };

    keepOrder = () => 0;

    constructor(
        readonly cdr: ChangeDetectorRef,
        readonly destroyRef: DestroyRef
    ) {
    }

    ngOnInit() {
        this.initResource();
    }

    ngAfterViewInit() {
        const viewer = new Viewer(this.imgList.nativeElement, {});
        this.destroyRef.onDestroy(() => {
            viewer.destroy();
        })
    }

    initResource() {
        const getResource = (block: BlockCraft.BlockComponent) => {
            this.block2Resource(block);
            if (block.nodeType === 'block') {
                block.getChildrenBlocks().forEach(getResource);
            }
        };

        this.doc.root.getChildrenBlocks().forEach(block => {
            getResource(block);
        });
        this.cdr.markForCheck();
    }

    block2Resource(block: BlockCraft.BlockComponent) {
        switch (block.flavour) {
            case 'image':
                this.imagesMap.set(block.id, {
                    id: block.id,
                    url: (block as BlockCraft.IBlockComponents['image']).props.src
                });
                break;
            case 'bookmark':
                this.bookmarksMap.set(block.id, {
                    id: block.id,
                    ...(block as BlockCraft.IBlockComponents['bookmark']).props
                });
                break;
            case 'attachment': {
                const nameSplit = (block as BlockCraft.IBlockComponents['attachment']).props.name.split('.');
                this.attachmentsMap.set(block.id, {
                    id: block.id,
                    ...(block as BlockCraft.IBlockComponents['attachment']).props,
                    name: nameSplit[0],
                    type: nameSplit.at(-1)
                });
            }
                break;
        }
    }

    dynamicObserve(events: Y.YEvent<any>[]) {
        for (const { path, changes, target } of events) {

            // at top level, it`s mean that block is created or deleted
            if (path.length) {
                // 处理删除
                continue;
            }

            changes.keys.forEach((change, key) => {
                if (change.action === 'delete') {
                    this.imagesMap.delete(key);
                    this.bookmarksMap.delete(key);
                    this.attachmentsMap.delete(key);
                } else {
                }
            });
        }

        this.cdr.markForCheck();
    };

    onDownloadFile($event: MouseEvent, value: IDocAttachmentResource) {
        $event.stopPropagation();
        downloadFile(value.url, value.name);
    }

    onPreviewFile($event: MouseEvent, value: IDocAttachmentResource) {
        $event.stopPropagation();
        window.open(value.url, '_blank');
    }
}
