import { Injectable } from '@angular/core';
import { DocMessageService } from '@ccc/blockcraft';
import { CsesRenderService } from '@ccc/cses-common';

@Injectable()
export class CsesDocMessageService implements DocMessageService {
    constructor(public readonly message: CsesRenderService
    ) {
    }

    success(message: string): void {
        this.message.success(message);
    }

    error(message: string): void {
        this.message.error(message);
    }

    info(message: string): void {
        this.message.info(message);
    }

    warn(message: string): void {
        this.message.warning(message);
    }
}
