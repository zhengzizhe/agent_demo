import { Pipe } from '@angular/core';

export enum SyncStatus {
    connecting,
    connected,
    disconnected ,
    localSynced,
    localStoring,
    Syncing,
    remoteSynced,
}

@Pipe({
    name: 'syncStatus',
    standalone: true
})
export class SyncStatusPipe {
    transform(status: SyncStatus): string {
        switch (status) {
            case SyncStatus.connecting:
                return '正在连接云端';
            case SyncStatus.connected:
                return '已连接云端';
            case SyncStatus.disconnected:
                return '已离线';
            case SyncStatus.localStoring:
                return '离线中，已本地存储';
            case SyncStatus.localSynced:
                return '本地已同步';
            case SyncStatus.Syncing:
                return '正在同步...';
            case SyncStatus.remoteSynced:
                return '云端已同步';

            default:
                return '异常状态';
        }
    }
}
