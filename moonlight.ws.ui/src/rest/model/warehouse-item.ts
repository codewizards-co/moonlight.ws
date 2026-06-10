import {AbstractPage} from './abstract-page';
import {AbstractFilter} from './abstract-filter';
import {WarehouseItemProduct} from "./warehouse-item-movement";

export interface WarehouseItem {
    id?: number;
    externalReferenceCode?: string;
    // modifiedDate?: string; // should probably be a date, but I have no example-data (it's null)
    quantity?: number;
    reservedQuantity?: number;
    sku?: string;
    unitOfMeasureKey?: string;
    warehouseId?: number;
    warehouseExternalReferenceCode?: string;
    /**
     * The list of products related to the SKU of this warehouse-item.
     * Resolved only when {@code fetch} contains {@code products}.
     */
    products?: WarehouseItemProduct[];
}

// eslint-disable-next-line
export interface WarehouseItemPage extends AbstractPage<WarehouseItem> {}

export interface WarehouseItemFilter extends AbstractFilter {
    filterWarehouseId: number;
    filterSku?: string;
    filterProductName?: string;

    //filter.includeInternal
    filterIncludeInternal?: boolean;
}
