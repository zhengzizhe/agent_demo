import { CsesHttpService } from '@ccc/cses-common';
import { IBaseResponse } from '@/pages/docs/services/types';
import { EmptyError, firstValueFrom, lastValueFrom, Subject, take } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export class HttpService {
    constructor(
        protected readonly http: CsesHttpService
    ) {
    }

    async _post<T extends IBaseResponse>(url: string, params?: any, cancel$?: Subject<void>): Promise<T> {
        const req$ = this.http.post(url, params).pipe(
            cancel$ ? takeUntil(cancel$) : (src) => src
        );

        try {
            const res = await firstValueFrom(req$);
            if (!res.isSuccess) throw new Error(res);
            return res;
        } catch (e) {
            if (e instanceof EmptyError) {
                // 取消的情况
                console.warn('请求被取消');
                return Promise.reject({ canceled: true });
            }
            throw e;
        }
    }

    async runSequentially(tasks: Array<() => Promise<any>>) {
        const results = [];
        for (const task of tasks) {
            try {
                const value = await task();
                results.push({ status: 'fulfilled', value });
            } catch (reason) {
                results.push({ status: 'rejected', reason });
            }
        }
        return results;
    }
}
