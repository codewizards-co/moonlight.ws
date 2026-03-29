import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AbstractRestService} from './abstract-rest.service';
import {
    WarehouseItemMovement,
    WarehouseItemMovementFilter,
    WarehouseItemMovementPage
} from './model/warehouse-item-movement';

@Injectable()
export class WarehouseItemMovementRestService extends AbstractRestService<WarehouseItemMovement, WarehouseItemMovementPage> {

    public constructor() {
        super("warehouse-item-movement");
    }

    public getWarehouseItemMovement(id: number): Observable<WarehouseItemMovement> {
        return this.getEntity(id);
    }

    public getWarehouseItemMovementPage(filter?: WarehouseItemMovementFilter): Observable<WarehouseItemMovementPage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");
            if (filter.filterWarehouseId) {
                query.push(`filter.warehouseId=${filter.filterWarehouseId}`);
            }
            if (filter.filterWarehouseErc) {
                query.push(`filter.warehouseErc=${filter.filterWarehouseErc}`);
            }
            if (filter.filterWarehouseItemId) {
                query.push(`filter.warehouseItemId=${filter.filterWarehouseItemId}`);
            }
            if (filter.filterWarehouseItemErc) {
                query.push(`filter.warehouseItemErc=${filter.filterWarehouseItemErc}`);
            }
            if (filter.filterSku) {
                query.push(`filter.sku=${encodeURIComponent(filter.filterSku)}`);
            }

            if (filter.filterCreatedFromIncl !== undefined) {
                query.push(`filter.createdFromIncl=${encodeURIComponent(filter.filterCreatedFromIncl)}`);
            }
            if (filter.filterCreatedToExcl !== undefined) {
                query.push(`filter.createdToExcl=${encodeURIComponent(filter.filterCreatedToExcl)}`);
            }

            if (filter.filterChangedFromIncl !== undefined) {
                query.push(`filter.changedFromIncl=${encodeURIComponent(filter.filterChangedFromIncl)}`);
            }
            if (filter.filterChangedToExcl !== undefined) {
                query.push(`filter.changedToExcl=${encodeURIComponent(filter.filterChangedToExcl)}`);
            }

            if (filter.filterBookedFromIncl !== undefined) {
                query.push(`filter.bookedFromIncl=${filter.filterBookedFromIncl}`);
            }
            if (filter.filterBookedToExcl !== undefined) {
                query.push(`filter.bookedToExcl=${filter.filterBookedToExcl}`);
            }

            if (filter.filterBooked !== undefined) {
                query.push(`filter.booked=${filter.filterBooked}`);
            }

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

    public postWarehouseItemMovement(warehouseItemMovement: WarehouseItemMovement): Observable<WarehouseItemMovement> {
        return this.postEntity(warehouseItemMovement);
    }
}

