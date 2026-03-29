import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AbstractRestService} from './abstract-rest.service';
import {Warehouse, WarehouseFilter, WarehousePage} from './model/warehouse';

@Injectable({
	providedIn: 'root'
})
export class WarehouseRestService extends AbstractRestService<Warehouse, WarehousePage> {

    public constructor() {
        super("warehouse");
    }

    public getWarehouse(id: number): Observable<Warehouse> {
        return this.getEntity(id);
    }

    public getWarehousePage(filter?: WarehouseFilter): Observable<WarehousePage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");
            if (filter.pageNumber !== undefined) {
                query.push(`pageNumber=${filter.pageNumber}`);
            }
            if (filter.pageSize !== undefined) {
                query.push(`pageSize=${filter.pageSize}`);
            }
            if (filter.filterActive !== undefined) {
                query.push(`filter.active=${filter.filterActive}`);
            }
            if (filter.sort) {
                query.push(`sort=${filter.sort}`);
            }
        }
        return this.getPage(...query);
    }
}
