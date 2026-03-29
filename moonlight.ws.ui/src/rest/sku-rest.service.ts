import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AbstractRestService} from './abstract-rest.service';
import {Sku, SkuFilter, SkuPage} from './model/sku';

@Injectable()
export class SkuRestService extends AbstractRestService<Sku, SkuPage> {

    constructor() {
        super("sku");
    }

    public getSku(id: number): Observable<Sku> {
        return this.getEntity(id);
    }

    public getSkuPage(filter?: SkuFilter): Observable<SkuPage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");

            if (filter.filterSku) {
                query.push(`filter.sku=${filter.filterSku}`);
            }
            if (filter.pageNumber !== undefined) {
                query.push(`pageNumber=${filter.pageNumber}`);
            }
            if (filter.pageSize !== undefined) {
                query.push(`pageSize=${filter.pageSize}`);
            }
        }
        return this.getPage(...query);
    }

}