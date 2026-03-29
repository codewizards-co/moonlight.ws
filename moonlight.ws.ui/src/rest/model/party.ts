import { AbstractPage } from './abstract-page';
import { AbstractFilter } from './abstract-filter';
import { Consignee } from './consignee';
import { Supplier } from './supplier';

export interface Party {
    id?: number;
    created?: string; // TODO date+time
    createdByUserId?: number;
    changed?: string; // TODO date+time
    changedByUserId?: number;

    active?: boolean;
    code?: string;
    name?: string;

    email?: string;
    phone?: string;

    street1?: string;
    street2?: string;
    street3?: string;
    zip?: string;
    city?: string;
    regionCode?: string;
    countryIsoCode?: string;

    taxNo?: string;

    website?: string;

    description?: string;

    /**
     * The consignee-relations of this party, if the client passed
     * {@code consignees} in its {@link RestConst#QUERY_FETCH fetch}. Otherwise,
     * this is {@code null}. If {@code fetch} contains {@code consignees}, but there
     * is none existing for this party, this is an empty list.
     */
    consignees?: Consignee[];

    /**
     * The supplier-relation of this party, if there is one assigned to this party.
     * If {@link RestConst#QUERY_FETCH fetch} contained {@code supplier}, it is
     * fully loaded. Otherwise it is a hollow object containing solely the
     * {@link SupplierDto#getId() id}.
     * <p>
     * If there is no supplier-instance associated with this party, this property is
     * {@code null}, meaning that this party is not a supplier.
     */
    supplier?: Supplier;
}

// eslint-disable-next-line
export interface PartyPage extends AbstractPage<Party> {}

export interface PartyFilter extends AbstractFilter {
    filterCode?: string;
    filterName?: string;
    filterCountryIsoCode?: string;
    filterCreatedFromIncl?: string;
    filterCreatedToExcl?: string;
    filterChangedFromIncl?: string;
    filterChangedToExcl?: string;
}