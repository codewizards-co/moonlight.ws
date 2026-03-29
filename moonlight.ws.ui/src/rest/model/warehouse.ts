import {AbstractPage} from './abstract-page';
import {LiferayFilter} from './liferay-filter';

export interface Warehouse {
    id?: number;
    active?: boolean;
    city?: string;
    countryISOCode?: string;
    description?: string;
    externalReferenceCode?: string;
    latitude?: number;
    longitude?: number;
    /**
     * key: locale (e.g. "en_US")
     * <p>
     *     value: name in the language specified by the key.
     */
    name?: {[locale: string]: string};
    regionISOCode?: string;
    street1?: string;
    street2?: string;
    street3?: string;
    type?: string;
    zip?: string;
    // warehouseItems: null
}

// eslint-disable-next-line
export interface WarehousePage extends AbstractPage<Warehouse> {}

export interface WarehouseFilter extends LiferayFilter {
    filterActive?: boolean;
}
