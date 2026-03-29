import {AbstractPage} from './abstract-page';
import {AbstractFilter} from './abstract-filter';

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
}

// eslint-disable-next-line
export interface WarehouseItemPage extends AbstractPage<WarehouseItem> {}

export interface WarehouseItemFilter extends AbstractFilter {
    filterWarehouseId: number;
    filterSku?: string;
}
