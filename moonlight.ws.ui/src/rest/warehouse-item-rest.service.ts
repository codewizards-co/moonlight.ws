import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AbstractRestService} from './abstract-rest.service';
import {WarehouseItem, WarehouseItemFilter, WarehouseItemPage} from './model/warehouse-item';

@Injectable()
export class WarehouseItemRestService extends AbstractRestService<WarehouseItem, WarehouseItemPage> {

    public constructor() {
        super("warehouse-item");
    }

    public getWarehouseItem(id: number): Observable<WarehouseItem> {
        return this.getEntity(id);
    }

    public getWarehouseItemPage(filter?: WarehouseItemFilter): Observable<WarehouseItemPage> {
        if (!filter) {
            throw new Error('filter must be defined');
        }
        if (filter.filterWarehouseId === undefined) {
            throw new Error('filter.filterWarehouseId must be defined');
        }

        const query: any[] = [];
        query.push("?");
        query.push(`filter.warehouseId=${filter.filterWarehouseId}`);

        if (filter.filterSku) {
            query.push(`filter.sku=${encodeURIComponent(filter.filterSku)}`);
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
        return this.getPage(...query);
    }

    public postWarehouseItem(warehouseItem: WarehouseItem): Observable<WarehouseItem> {
        return this.postEntity(warehouseItem);
    }
}
