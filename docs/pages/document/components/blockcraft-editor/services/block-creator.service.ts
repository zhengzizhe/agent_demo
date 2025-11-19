import { Injectable, Injector } from '@angular/core';
import { ComponentPortal } from '@angular/cdk/portal';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BlockCreatorService, DOC_FILE_SERVICE_TOKEN, DOC_MESSAGE_SERVICE_TOKEN } from '@ccc/blockcraft';
import { IBlockSchemaOptions } from '@ccc/blockcraft/framework/block-std/schema/block-schema';
import { Overlay } from '@angular/cdk/overlay';
import {
    EmbedFrameCreator
} from '../components/embed-frame-creator';

@Injectable()
export class CsesBlockCreatorService extends BlockCreatorService {

    fileService = this.injector.get(DOC_FILE_SERVICE_TOKEN);
    // messageService = this.injector.get(DOC_MESSAGE_SERVICE_TOKEN);

    constructor(private injector: Injector) {
        super();
    }

    async getParamsByScheme<T extends IBlockSchemaOptions>(schema: T): Promise<BlockCraft.BlockCreateParameters<T['flavour']> | null> {
        const params: any = [];
        switch (schema.flavour) {
            case 'attachment':
                const fileList = await this.fileService.inputFiles();
                const file = fileList[0];
                // if (file.type.startsWith('image/')) {
                //     this.messageService.warn('请上传非图片文件');
                //     return null;
                // }
                const attachmentInfo = await this.fileService.uploadAttachment(file);
                params.push(attachmentInfo);
                break;
            case 'image':
                const imgFileList = await this.fileService.inputFiles('image/*');
                const imgFile = imgFileList[0];
                const imgSrc = await this.fileService.uploadImg(imgFile);
                params.push(imgSrc);
                break;
            case 'juejin-embed':
            case 'figma-embed':
                const urlRes = await this.onShowFrameInputPad(schema);
                params.push(urlRes);
                break;
            case 'bookmark':
                const bookmarkUrl = await this.onShowFrameInputPad(schema);
                params.push(bookmarkUrl);
                break;
        }

        return params;
    }

    private onShowFrameInputPad(schema: IBlockSchemaOptions) {
        const overlay = this.injector.get(Overlay);
        const ovr = overlay.create({
            positionStrategy: overlay.position().global().centerHorizontally().centerVertically(),
            scrollStrategy: overlay.scrollStrategies.reposition(),
            hasBackdrop: true,
            backdropClass: 'cdk-overlay-transparent-backdrop'
        });
        const cpr = ovr.attach(new ComponentPortal(EmbedFrameCreator));
        cpr.setInput('schema', schema);
        ovr.backdropClick().pipe(takeUntilDestroyed(cpr.instance.destroyer)).subscribe(() => ovr.dispose());

        return new Promise((resolve, reject) => {
            cpr.instance.onSubmit.pipe(takeUntilDestroyed(cpr.instance.destroyer)).subscribe((url: string) => {
                ovr.dispose();
                resolve(url);
            });

            cpr.instance.onCancel.pipe(takeUntilDestroyed(cpr.instance.destroyer)).subscribe(() => {
                ovr.dispose();
                reject();
            });
        });
    }

}
