import { inject, Injectable } from '@angular/core';
import Viewer from 'viewerjs';
import { DOC_MESSAGE_SERVICE_TOKEN, DocAttachmentInfo, DocFileService } from '@ccc/blockcraft';
import { FileService, ObjectU } from '@ccc/core-common';
import { FileBucket } from '@ccc/cses-common';

@Injectable()
export class CsesDocFileService extends DocFileService {
    constructor(
        private fileService: FileService
    ) {
        super();
    }

    msgService = inject(DOC_MESSAGE_SERVICE_TOKEN);

    private _uploadFile = (file: File): Promise<string> => {
        return new Promise((resolve, reject) => {
            this.fileService.uploadFile(file, (e) => {
            }, FileBucket(file.name))
                .then(({ successes }) => {
                    const info = ObjectU.objectExcludeFields(successes.at(0), ['origin', 'ossAccount', 'ossParts', 'ossUploadId', 'ossRequestId']);
                    resolve(this.fileService.getFilePath(info));
                })
                .catch(reason => {
                    reject(reason);
                });
        });
    };

    uploadImg(file: File): Promise<string> {
        return this._uploadFile(file);
    }

    uploadAttachment(file: File): Promise<DocAttachmentInfo> {
        if (file.size > 1024 * 1024 * 20) {
            this.msgService.warn('超过20MB，文件过大');
            return Promise.reject('文件过大');
        }

        return this._uploadFile(file).then(url => {
            return {
                name: file.name,
                size: file.size,
                type: file.type,
                url
            };
        });
    }

    previewAttachment() {
    }

    previewImg(options: Record<string, unknown>): void {
        const el = options['el'];
        const title = options['title'];
        if (el instanceof HTMLElement) {
            const img = el instanceof HTMLImageElement ? el : el.querySelector('img');
            if (!img) return;
            const viewer = new Viewer(el, {
                title: [0, () => (typeof title === 'string') ? title : img.src],
                inline: false,
                zIndex: 9999998,
                ready: () => {
                    viewer.show();
                },
                stop: () => {
                    viewer.destroy();
                },
                toolbar: {
                    prev: 0,
                    next: 0,
                    zoomIn: 4,
                    zoomOut: 4,
                    oneToOne: 4,
                    reset: 4,
                    play: {
                        show: 0,
                        size: 'large'
                    },
                    rotateLeft: 4,
                    rotateRight: 4,
                    flipHorizontal: 4,
                    flipVertical: 4
                },
                ...options
            });
        }
    }

}
