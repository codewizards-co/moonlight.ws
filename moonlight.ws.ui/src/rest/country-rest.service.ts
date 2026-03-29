import { AbstractRestService } from './abstract-rest.service';
import { Country, CountryFilter, CountryPage } from './model/country';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class CountryRestService extends AbstractRestService<Country, CountryPage> {

    constructor() {
        super("country");
    }

    public getCountry(id: number): Observable<Country> {
        return this.getEntity(id);
    }

    public getCountryByAlpha2(a2: string): Observable<Country> {
        return this.getEntity("by-a2", a2);
    }

    public getCountryByAlpha3(a3: string): Observable<Country> {
        return this.getEntity("by-a3", a3);
    }

    public getCountryPage(filter?: CountryFilter): Observable<CountryPage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");

            if (filter.filterActive !== undefined) {
                query.push(`filter.active=${filter.filterActive}`);
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