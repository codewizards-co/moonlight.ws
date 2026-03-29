import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AbstractRestService } from './abstract-rest.service';
import { Consignee, ConsigneeFilter, ConsigneePage } from './model/consignee';
import { ReadOptionSet } from './model/read-option-set';

@Injectable()
export class ConsigneeRestService extends AbstractRestService<Consignee, ConsigneePage> {

    public constructor() {
        super('consignee');
    }

    public getConsignee(id: number, readOptionSet?: ReadOptionSet): Observable<Consignee> {
        const path: any[] = [id];
        if (readOptionSet?.fetch) {
            path.push('?');
            path.push(`fetch=${encodeURIComponent(readOptionSet.fetch)}`);
        }
        return this.getEntity(...path);
    }

    public getConsigneePage(filter?: ConsigneeFilter): Observable<ConsigneePage> {
        const query: any[] = [];
        if (filter) {
            query.push('?');
            if (filter.filterWarehouseId) {
                query.push(`filter.warehouseId=${filter.filterWarehouseId}`);
            }
            if (filter.filterPartyId) {
                query.push(`filter.name=${filter.filterPartyId}`);
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

    public postConsignee(consignee: Consignee): Observable<Consignee> {
        return this.postEntity(consignee);
    }

    public deleteConsignee(id: number): Observable<any> {
        return this.deleteEntity(id);
    }
}