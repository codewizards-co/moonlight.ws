import { AbstractPage } from './abstract-page';
import { AbstractFilter } from './abstract-filter';
import { Supplier } from './supplier';
import { Price } from './price';

export interface WarehouseItemMovement {
    id?: number;
    created?: string; // TODO date+time
    createdByUserId?: number;
    changed?: string; // TODO date+time
    changedByUserId?: number;
    warehouseItemId?: number;
    warehouseItemErc?: string;
    sku?: string;
    products?: WarehouseItemProduct[];
    warehouseId?: number;
    warehouseErc?: string;
    quantity: number;
    booked?: string; // TODO date+time
    draft?: boolean;
    finalized?: string; // TODO date+time
    groupId?: number;
    type: WarehouseItemMovementType;

    /**
     * References {@link Warehouse#getId() Warehouse.id} of the other warehouse, if
     * {@link #getType() type} is {@link WarehouseItemMovementType#TRANSFER
     * TRANSFER}. For other movement-types, this is {@code null}.
     * <p>
     * If {@link #getQuantity() quantity} is positive, the goods are transferred
     * from the other warehouse to this warehouse (referenced by
     * {@link #getWarehouseId()}).
     * <p>
     * If {@link #getQuantity() quantity} is negative, the goods are transferred to
     * the other warehouse from this warehouse (referenced by
     * {@link #getWarehouseId()}).
     * <p>
     * In such a transfer, there is also a {@link #getGroup() group} existing,
     * grouping the other warehouse's movement with this instance. If this
     * instance's quantity is positive, the other movement's quantity is negative
     * (and vice versa). The sum of both quantities is 0.
     *
     * @see WarehouseItemMovementType#TRANSFER
     */
    otherWarehouseId?: number;

    /**
     * References {@link WarehouseItem#getId() WarehouseItem.id} of the other
     * warehouse, if {@link #getType() type} is
     * {@link WarehouseItemMovementType#TRANSFER TRANSFER}. For other
     * movement-types, this is {@code null}.
     * <p>
     * The same product having the same SKU has a different warehouse-item-ID in
     * each warehouse.
     */
    otherWarehouseItemId?: number;

    /**
     * References {@link SupplierDto#getId() SupplierDto.id} of the supplier, if
     * {@link #getType() type} is {@link WarehouseItemMovementType#SUPPLY SUPPLY}.
     * For other movement-types, this is {@code null}.
     */
    supplierId?: number;

    supplier?: Supplier;

    price?: Price;
}

// eslint-disable-next-line
export interface WarehouseItemMovementPage extends AbstractPage<WarehouseItemMovement> {}

export interface WarehouseItemMovementFilter extends AbstractFilter {
    filterWarehouseItemId?: number;
    filterWarehouseItemErc?: string;
    filterSku?: string;
    filterWarehouseId?: number;
    filterWarehouseErc?: string;

    filterCreatedFromIncl?: string;
    filterCreatedToExcl?: string;

    filterChangedFromIncl?: string;
    filterChangedToExcl?: string;

    filterBookedFromIncl?: string;
    filterBookedToExcl?: string;

    filterBooked?: boolean;
    filterDraft?: boolean;
}

export enum WarehouseItemMovementType {
    /**
     * The inventory was taken initially or a wrong quantity was corrected when
     * taking inventory again, later. It basically means that we don't know where a
     * product has come from (positive quantity) or where it has gone (negative
     * quantity).
     * <p>
     * Even while taking inventory, another (specific) movement-type should be
     * chosen, if it is known. This is a fallback-type to be used only, if no
     * specific type is known.
     */
    INVENTORY = "INVENTORY",

    /**
     * A product or raw material was <b>purchased from a supplier</b> and added to
     * stock.
     * <p>
     * The <b>quantity is normally positive</b>, but it may be negative in order to
     * correct a previous wrong booking.
     */
    SUPPLY = "SUPPLY",

    /**
     * An intermediate product or raw material was taken from stock to be consumed
     * in the process of producing another product.
     * <p>
     * The raw material may be transformed into sth. else or it may just be used and
     * thrown away. For example, cacao-beans are transformed into chocolate, but
     * cleaning agents are simply washed away.
     * <p>
     * The <b>quantity is normally negative</b>, but it may be positive in order to
     * correct a previous wrong booking.
     */
    CONSUMPTION = "CONSUMPTION",

    /**
     * An intermediate or final product was produced and added to stock.
     * <p>
     * The <b>quantity is normally positive</b>, but it may be negative in order to
     * correct a previous wrong booking.
     */
    PRODUCTION = "PRODUCTION",

    /**
     * Goods were transferred from/to another warehouse.
     * <p>
     * When performing a transfer, 2 movements are created and persisted, both with
     * this type. The sum of their quantities is 0 as one side has a negative
     * quantity to remove the goods from stock while the other side has an
     * equivalent positive quantity to add the goods to stock.
     *
     * @see WarehouseItemMovementEntity#getOtherWarehouseId()
     */
    TRANSFER = "TRANSFER",

    /**
     * Goods were sold.
     * <p>
     * Since we normally handle all of our own sales directly in Liferay (and there
     * exists no warehouse-item-movement in Moonlight), this type is usually used
     * for shops selling on a commission basis, only.
     */
    SALE = "SALE",

    /**
     * Goods were damaged so that they cannot be sold or processed, anymore. The
     * cause of this may be just time or some moulder or any events like a flood.
     */
    DAMAGED = "DAMAGED"
}

export interface WarehouseItemProduct {
    productId: number;
    productName?: {[locale: string]: string};
}
