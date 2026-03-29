import { Injectable } from '@angular/core';
import { AbstractRestService } from './abstract-rest.service';
import { AutoCreateInvoiceItemsRequest, Invoice, InvoiceFilter, InvoiceItem, InvoiceItemFilter, InvoiceItemPage, InvoicePage } from './model/invoice';
import { ReadOptionSet } from './model/read-option-set';
import { Observable } from 'rxjs';

@Injectable()
export class InvoiceRestService extends AbstractRestService<Invoice, InvoicePage> {

    public constructor() {
        super("invoice");
    }

    public getInvoice(id: number, readOptionSet?: ReadOptionSet): Observable<Invoice> {
        const path: any[] = [id];
        if (readOptionSet?.fetch) {
            path.push('?');
            path.push(`fetch=${encodeURIComponent(readOptionSet.fetch)}`);
        }
        return this.getEntity(...path);
    }

    public getInvoicePage(filter?: InvoiceFilter): Observable<InvoicePage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");
            if (filter.filterPartyId) {
                query.push(`filter.party.id=${filter.filterPartyId}`);
            }

            if (filter.filterCreatedFromIncl !== undefined) {
                query.push(`filter.createdFromIncl=${encodeURIComponent(filter.filterCreatedFromIncl)}`);
            }
            if (filter.filterCreatedToExcl !== undefined) {
                query.push(`filter.createdToExcl=${encodeURIComponent(filter.filterCreatedToExcl)}`);
            }

            if (filter.filterChangedFromIncl !== undefined) {
                query.push(`filter.changedFromIncl=${encodeURIComponent(filter.filterChangedFromIncl)}`);
            }
            if (filter.filterChangedToExcl !== undefined) {
                query.push(`filter.changedToExcl=${encodeURIComponent(filter.filterChangedToExcl)}`);
            }

            if (filter.filterBookedFromIncl !== undefined) {
                query.push(`filter.bookedFromIncl=${filter.filterBookedFromIncl}`);
            }
            if (filter.filterBookedToExcl !== undefined) {
                query.push(`filter.bookedToExcl=${filter.filterBookedToExcl}`);
            }

            if (filter.filterBooked !== undefined) {
                query.push(`filter.booked=${filter.filterBooked}`);
            }

            if (filter.filterDraft !== undefined) {
                query.push(`filter.draft=${filter.filterDraft}`);
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
            if (filter.fetch) {
                query.push(`fetch=${encodeURIComponent(filter.fetch)}`);
            }
        }
        return this.getPage(...query);
    }

    public postInvoice(invoice: Invoice): Observable<Invoice> {
        return this.postEntity(invoice);
    }

    public putInvoice(invoice: Invoice): Observable<Invoice> {
        return this.putEntity(invoice, invoice.id);
    }

    public finalizeInvoice(invoiceId: number): Observable<Invoice> {
        return this.postEntity(null, invoiceId, 'finalize');
    }

    public markInvoicePaid(invoiceId: number, paid?: string): Observable<Invoice> {
        return this.postEntity(null, invoiceId, 'mark-paid', '?', `paid=${paid??''}`);
    }
}

@Injectable()
export class InvoiceItemRestService extends AbstractRestService<InvoiceItem, InvoiceItemPage> {

    public constructor() {
        super("invoice-item");
    }

    public getInvoiceItem(id: number, readOptionSet?: ReadOptionSet): Observable<InvoiceItem> {
        const path: any[] = [id];
        if (readOptionSet?.fetch) {
            path.push('?');
            path.push(`fetch=${encodeURIComponent(readOptionSet.fetch)}`);
        }
        return this.getEntity(...path);
    }

    public getInvoiceItemPage(filter?: InvoiceItemFilter): Observable<InvoiceItemPage> {
        const query: any[] = [];
        if (filter) {
            query.push("?");
            if (filter.fetch) {
                query.push(`fetch=${encodeURIComponent(filter.fetch)}`);
            }
            if (filter.filterInvoiceId) {
                query.push(`filter.invoice.id=${filter.filterInvoiceId}`);
            }
            if (filter.filterWarehouseItemMovementId) {
                query.push(`filter.warehouseItemMovement.id=${filter.filterWarehouseItemMovementId}`);
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

    public postInvoiceItem(invoiceItem: InvoiceItem): Observable<InvoiceItem> {
        return this.postEntity(invoiceItem);
    }

    public putInvoiceItem(invoiceItem: InvoiceItem): Observable<InvoiceItem> {
        return this.putEntity(invoiceItem, invoiceItem.id);
    }

    public deleteInvoiceItem(id: number): Observable<any> {
        return this.deleteEntity(id);
    }

    public autoCreateInvoiceItems(request: AutoCreateInvoiceItemsRequest): Observable<InvoiceItem[]> {
        return this.postEntitySpecial(request, "auto-create");
    }
}