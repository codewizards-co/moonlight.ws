import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AbstractRestService } from './abstract-rest.service';
import { Supplier, SupplierFilter, SupplierPage } from './model/supplier';
import { Party } from './model/party';
import { ReadOptionSet } from './model/read-option-set';

@Injectable()
export class SupplierRestService extends AbstractRestService<Supplier, SupplierPage> {

    public constructor() {
        super('supplier');
    }

    public getSupplier(id: number, readOptionSet?: ReadOptionSet): Observable<Supplier> {
        const path: any[] = [id];
        if (readOptionSet?.fetch) {
            path.push('?');
            path.push(`fetch=${encodeURIComponent(readOptionSet.fetch)}`);
        }
        return this.getEntity(...path);
    }

    public getSupplierPage(filter?: SupplierFilter): Observable<SupplierPage> {
        const query: any[] = [];
        if (filter) {
            query.push('?');
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

    public postSupplier(supplier: Supplier): Observable<Supplier> {
        return this.postEntity(supplier);
    }

    public deleteSupplier(id: number): Observable<any> {
        return this.deleteEntity(id);
    }
}