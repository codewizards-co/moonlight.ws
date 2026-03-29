import { AbstractPage } from './abstract-page';
import { AbstractFilter } from './abstract-filter';
import { Party } from './party';

export interface Consignee {
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

    /**
     * References the related {@linkplain Warehouse warehouse} via its
     * {@link Warehouse#getId() ID}. Must not be {@code null}.
     */
    warehouseId: number;
}

// eslint-disable-next-line
export interface ConsigneePage extends AbstractPage<Consignee> {}

export interface ConsigneeFilter extends AbstractFilter {
    // filter.party.id
    filterPartyId?: number;

    // filter.warehouseId
    filterWarehouseId?: number;
}
