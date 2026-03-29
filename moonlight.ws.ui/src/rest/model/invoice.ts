import { AbstractPage } from './abstract-page';
import { AbstractFilter } from './abstract-filter';
import { Party } from './party';
import { WarehouseItemMovement } from './warehouse-item-movement';
import { Price } from './price';

export interface Invoice {
    id?: number;
    created?: string; // TODO date+time
    createdByUserId?: number;
    changed?: string; // TODO date+time
    changedByUserId?: number;
    deleted?: string; // TODO date+time
    deletedByUserId?: number;
    draft?: boolean;
    finalized?: string; // TODO date+time
    finalizedByUserId?: number;
    booked?: string; // TODO date+time
    workflow?: InvoiceWorkflow;
    party?: Party;
    orderId?: number;
    shipmentId?: number;

    includedTotalNet?: number;
    includedTotalGross?: number;
    excludedTotalNet?: number;
    excludedTotalGross?: number;

    /**
     * When was the invoice marked as being paid in our system. Either {@code undefined}
     * or the timestamp when it was marked as paid.
     */
    markedPaid?: string; // TODO date+time
    markedPaidByUserId?: number;

    /**
     * Either {@code undefined} or the date when it was paid. If there are multiple
     * payments, this is the date of the last payment, i.e. when there is no open
     * amount left for this invoice, anymore.
     */
    paid?: string; // TODO date

    description?: string;
}

// eslint-disable-next-line
export interface InvoicePage extends AbstractPage<Invoice> {}

export interface InvoiceFilter extends AbstractFilter {
    filterPartyId?: number;

    filterCreatedFromIncl?: string;
    filterCreatedToExcl?: string;
    filterChangedFromIncl?: string;
    filterChangedToExcl?: string;

    filterBookedFromIncl?: string;
    filterBookedToExcl?: string;

    filterBooked?: boolean;
    filterDraft?: boolean;
}

export enum InvoiceWorkflow {
    CONSIGNEE = 'CONSIGNEE',
    SUPPLIER = 'SUPPLIER'
}

export interface InvoiceItem {
    id?: number;
    created?: string; // TODO date+time
    createdByUserId?: number;
    changed?: string; // TODO date+time
    changedByUserId?: number;
    deleted?: string; // TODO date+time
    deletedByUserId?: number;
    draft?: boolean;
    finalized?: string; // TODO date+time
    finalizedByUserId?: number;

    invoice?: Invoice;
    warehouseItemMovement?: WarehouseItemMovement;

    include?: InvoiceInclude;

    /**
     * The quantity being charged. It is positive, if we charge to the other party
     * and it is negative, if we refund an item.
     * <p>
     * In a normal sale to a customer, both the quantity and the
     * {@link #getPriceTotalGross() priceTotalGross} are positive.
     */
    quantity?: number;

    price?: Price;

    orderItemId?: number;
    shipmentItemId?: number;
}

export enum InvoiceInclude {
    INCLUDE = 'INCLUDE',
    EXCLUDE = 'EXCLUDE'
}

// eslint-disable-next-line
export interface InvoiceItemPage extends AbstractPage<InvoiceItem> {}

export interface InvoiceItemFilter extends AbstractFilter {
    filterInvoiceId?: number;
    filterWarehouseItemMovementId?: number;
}

export interface AutoCreateInvoiceItemsRequest {
    /**
     * The invoice to be processed. Only its {@link Invoice.id} is taken
     * into account. All other properties are ignored.
     */
    invoice: Invoice;
}