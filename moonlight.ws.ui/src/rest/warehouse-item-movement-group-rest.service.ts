import {Injectable} from '@angular/core';
import {concatMap, Observable} from 'rxjs';
import {concatUrlSegments} from '../util/url.util';
import {AbstractRestService} from './abstract-rest.service';
import {
    WarehouseItemMovementGroup,
    WarehouseItemMovementGroupFilter,
    WarehouseItemMovementGroupPage
} from './model/warehouse-item-movement-group';

@Injectable()
export class WarehouseItemMovementGroupRestService extends AbstractRestService<WarehouseItemMovementGroup, WarehouseItemMovementGroupPage> {

    public constructor() {
        super("warehouse-item-movement-group");
    }

    public getWarehouseItemMovementGroup(id: number): Observable<WarehouseItemMovementGroup> {
        return this.getEntity(id);
    }

    public getWarehouseItemMovementGroupPage(filter?: WarehouseItemMovementGroupFilter): Observable<WarehouseItemMovementGroupPage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");

            if (filter.filterDraft !== undefined) {
                query.push(`filter.draft=${filter.filterDraft}`);
            }

            if (filter.pageNumber !== undefined) {
                query.push(`pageNumber=${filter.pageNumber}`);
            }
            if (filter.pageSize !== undefined) {
                query.push(`pageSize=${filter.pageSize}`);
            }
            if (filter.sort) {
                query.push(`sort=${filter.sort}`);
            }
            if (filter.fetch) {
                query.push(`fetch=${encodeURIComponent(filter.fetch)}`);
            }
        }
        return this.getPage(...query);
    }

    public postWarehouseItemMovementGroup(warehouseItemMovementGroup: WarehouseItemMovementGroup): Observable<WarehouseItemMovementGroup> {
        return this.postEntity(warehouseItemMovementGroup);
    }

    public finalizeWarehouseItemMovementGroup(id: number): Observable<WarehouseItemMovementGroup> {
        const url = concatUrlSegments(this.restConfig.restUrl, this.resourcePath, id, "finalize");
        return this.keycloakProvider.refreshIfNeeded().pipe(
            concatMap(() => this.httpClient.post(url, null, this.getHttpOptions()) as Observable<WarehouseItemMovementGroup>)
        );
    }
}

