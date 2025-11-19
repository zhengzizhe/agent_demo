import { Injectable } from '@angular/core';
import {
    NavigationCancel, NavigationEnd, NavigationError, NavigationStart,
    Router
} from '@angular/router';
import { BehaviorSubject, filter, first, Subject } from 'rxjs';
import { LayoutService } from '@/layout/layout.service';
import { ElectronService } from '@/app/core/services';

export const PAGE_ROUTE_PREFIX = '/main/docs';

@Injectable()
export class RouterService {
    refreshPage$ = new Subject();

    sharedData: Record<string, any> = {};
    sharedDataChange$ = new BehaviorSubject<Record<string, any>>({});

    // private _routeStack: string[] = [];

    get currentUrl() {
        return this.ngRouter.url;
    }

    constructor(
        readonly ngRouter: Router,
        public readonly layoutService: LayoutService,
    ) {
    }

    ngOnInit() {
    }

    setSharedData(key: string, data: any) {
        this.sharedData[key] = data;
        this.sharedDataChange$.next(this.sharedData);
    }

    deleteSharedData(key: string) {
        delete this.sharedData[key];
    }

    // 以 / 开头，则以绝对路径为准。没有则相对于文档库起始页
    navigateByUri(uri: string) {
        uri = uri.startsWith('/')
            ? uri
            : (PAGE_ROUTE_PREFIX + '/' + uri).replace(/\/$/, '');

        return new Promise(async (resolve, reject) => {
            this.listenNextNavigation().subscribe(e => {
                if(e instanceof NavigationEnd) {
                    resolve(true)
                    return
                }
                if(e instanceof NavigationCancel || e instanceof NavigationError) {
                    reject(e)
                }
            })
            this.layoutService.navigateTo(uri);
        })
    }

    navigate(path: any[]) {
        const uri = (PAGE_ROUTE_PREFIX + '/' + path.join('/')).replace(/\/$/, '');
        this.layoutService.navigateTo(uri);
    }

    // 以 / 开头，则以绝对路径为准。没有则相对于文档库起始页
    navigateToNewTab(uri: string, title?: string) {
        return new Promise(async (resolve, reject) => {
            this.listenNextNavigation().subscribe(e => {
                if(e instanceof NavigationEnd) {
                    resolve(true)
                    return
                }
                if(e instanceof NavigationCancel || e instanceof NavigationError) {
                    reject(e)
                }
            })
            this.layoutService.navigateToNewTab(
                uri.startsWith('/')
                    ? uri
                    : (PAGE_ROUTE_PREFIX + '/' + uri).replace(/\/$/, ''),
                title
            );
        })
    }

    listenNextNavigation() {
        return this.ngRouter.events.pipe(
            filter(e => e instanceof NavigationEnd || e instanceof NavigationCancel || e instanceof NavigationError),
            first())
    }

    navigateToDoc(id: string, name?: string, from?: {
        page?: 'space' | 'home' | 'folder' | 'share' | 'doc',
        spaceId?: string,
        folderId?: string,
    }) {
        let uri = PAGE_ROUTE_PREFIX + '/document/' + id;
        // if (from) {
        //     uri += `?from=${from.page}`;
        //     if (from.spaceId) {
        //         uri += `&spaceId=${from.spaceId}`;
        //     }
        //     if (from.folderId) {
        //         uri += `&folderId=${from.folderId}`;
        //     }
        // }

        return new Promise(async (resolve, reject) => {
            this.listenNextNavigation().subscribe(e => {
                if(e instanceof NavigationEnd) {
                    resolve(true)
                    return
                }
                if(e instanceof NavigationCancel || e instanceof NavigationError) {
                    reject(e)
                }
            })
            this.layoutService.navigateToNewTab(uri, name || '文档')
        })
    }

    // back() {
    //     this._routeStack.pop();
    //     if (this._routeStack.length >= 1) {
    //         const r = this._routeStack.pop();
    //         if (!r.startsWith(PAGE_ROUTE_PREFIX)) {
    //             this._routeStack = [];
    //             this.navigateToNewTab('');
    //         }
    //         this.navigateToNewTab(r);
    //     } else {
    //         this.navigateToNewTab('');
    //     }
    // }

    setCurrentTabTitle(title: string) {
        this.layoutService.setTitle(title);
    }

    refreshPage() {
        this.refreshPage$.next(true);
    }
}
