import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AbstractRestService } from './abstract-rest.service';
import { Party, PartyFilter, PartyPage } from './model/party';
import { ReadOptionSet } from './model/read-option-set';

@Injectable()
export class PartyRestService extends AbstractRestService<Party, PartyPage> {

    public constructor() {
        super("party");
    }

    public getParty(id: number, readOptionSet?: ReadOptionSet): Observable<Party> {
        const path: any[] = [id];
        this.appendReadOptionSet(path, readOptionSet);
        return this.getEntity(...path);
    }

    public getPartyPage(filter?: PartyFilter): Observable<PartyPage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");
            if (filter.filterCode) {
                query.push(`filter.code=${encodeURIComponent(filter.filterCode)}`);
            }
            if (filter.filterName) {
                query.push(`filter.name=${encodeURIComponent(filter.filterName)}`);
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
        }
        this.appendReadOptionSet(query, filter);
        return this.getPage(...query);
    }

    public postParty(party: Party, readOptionSet?: ReadOptionSet): Observable<Party> {
        const path: any[] = [];
        this.appendReadOptionSet(path, readOptionSet);
        return this.postEntity(party, path);
    }

    public putParty(party: Party, readOptionSet?: ReadOptionSet): Observable<Party> {
        const path: any[] = [party.id];
        this.appendReadOptionSet(path, readOptionSet);
        return this.putEntity(party, path);
    }
}