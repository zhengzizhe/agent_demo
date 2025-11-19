import { Inject, Injectable } from '@angular/core';
import { CsesContextService, CsesHttpService } from '@ccc/cses-common';
import { AppContext } from '@ccc/core-common';
import { IOrgTreeDataItem } from '@ccc/cses-common/src/components/cses-personnel-tree/cses-personnel-tree.const';
import { HttpService } from './http.service';

@Injectable()
export class UserService extends HttpService {

    private flatOrgTree: Map<string, IOrgTreeDataItem> = new Map();

    constructor(
        @Inject(AppContext) public readonly ctx: CsesContextService,
        protected readonly http: CsesHttpService
    ) {
        super(http);
    }

    async searchInOrganize(keyword: string) {
        const userInfo = this.ctx.userInfo;
        if (!this.flatOrgTree.has(userInfo.companyId)) {
            this.flatOrgTree.clear();
            const res = await this._post('/Company/readAllChildren', {
                orgId: userInfo.companyId,
                isReadUser: true
            });
            const item = res.items[0] as IOrgTreeDataItem;
            const flat = (item: IOrgTreeDataItem) => {
                item['companyId'] = userInfo.companyId;
                this.flatOrgTree.set(item.id, item);
                item.children?.forEach(flat);
            };
            flat(item);
        }

        const list: IOrgTreeDataItem[] = [];
        this.flatOrgTree.forEach(v => {
            if (v.name.includes(keyword) || v.deptName?.includes(keyword) || v.orgName?.includes(keyword)) {
                list.push(v);
            }
        });
        return list;
    }

}
