import { AbstractPage } from './abstract-page';
import { AbstractFilter } from './abstract-filter';
import { Party } from './party';

export interface Supplier {
    id?: number;
    created?: string; // TODO date+time
    createdByUserId?: number;
    changed?: string; // TODO date+time
    changedByUserId?: number;

    /**
     * The related party. Must not be {@code null}. If the query-parameter
     * {@link RestConst#QUERY_FETCH fetch} contained {@code party} during a
     * GET-request, this is the fully loaded DTO, otherwise it is hollow and
     * contains nothing but the {@link Party#getId() id}.
     */
    party: Party;
}

// eslint-disable-next-line
export interface SupplierPage extends AbstractPage<Supplier> {}

export interface SupplierFilter extends AbstractFilter {
    // filter.party.id
    filterPartyId?: number;
}
