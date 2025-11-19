import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import {
    DocService,
    DocsOverlayService,
    DocsPermissionService,
    EntityService,
    RouterService,
    UserService
} from '@/pages/docs/services';
import { CanDeactivateGuard } from './deactivateGuard';
import { ActivateGuard } from './activateGuard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('../pages/home/home.page').then((m) => m.HomePage),
        children: [
            {
                path: '',
                loadComponent: () => import('../pages/home/children/home-main/home-main.page').then((m) => m.HomeMainPage)
            },
            {
                path: 'share',
                loadComponent: () => import('../pages/home/children/share/share.page').then((m) => m.SharePage)
            },
            {
                path: 'folder/:id',
                canActivate: [ActivateGuard],
                data: { id: 'folder' },
                loadComponent: () => import('../pages/folder/folder.page').then((m) => m.FolderPage)
            }
        ]
    },
    {
        path: 'space/:id',
        data: { id: 'space' },
        loadComponent: () => import('../pages/space/space.page').then((m) => m.SpacePage),
        children: [
            {
                path: '',
                loadComponent: () => import('../pages/space/children/home/space-home.page').then((m) => m.SpaceHomePage)
            },
            {
                path: 'folder/:id',
                data: { id: 'folder' },
                canActivate: [ActivateGuard],
                loadComponent: () => import('../pages/folder/folder.page').then((m) => m.FolderPage)
            },
            {
                path: 'tags',
                loadComponent: () => import('../pages/space/children/tags/space-tags.page').then((m) => m.SpaceTagsPage)
            }
        ]
    },
    {
        path: 'document/:id',
        data: { id: 'document', reuse: true },
        loadComponent: () => import('../pages/document/document.page').then((m) => m.DocumentPage),
        canActivate: [ActivateGuard],
        canDeactivate: [CanDeactivateGuard]
    },
    {
        path: 'document-version/:id',
        data: { id: 'document-version', reuse: true },
        loadComponent: () => import('../pages/document/document-version-page/document-version-page').then((m) => m.DocumentVersionPage),
        canActivate: [ActivateGuard]
    },
    {
        path: 'no-auth',
        data: { id: 'no-auth' },
        loadComponent: () => import('../pages/no-auth/noAuth.page').then((m) => m.NoAuthPage)
    }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes)
    ],
    providers: [
        EntityService,
        RouterService,
        UserService,
        DocService,
        DocsPermissionService,
        DocsOverlayService,
        ActivateGuard,
        CanDeactivateGuard
    ],
    exports: [RouterModule]
})
export class DocsAppModule {
    constructor(
        private iconRegistry: MatIconRegistry,
        private sanitizer: DomSanitizer
    ) {
        this.iconRegistry.addSvgIconSet(
            this.sanitizer.bypassSecurityTrustResourceUrl('https://at.alicdn.com/t/c/font_4682833_rtaeywsxz0j.js')
        );
        this.iconRegistry.addSvgIconSet(
            this.sanitizer.bypassSecurityTrustResourceUrl('https://at.alicdn.com/t/c/font_4682833_xx54lvcm76c.js')
        );
    }

}
