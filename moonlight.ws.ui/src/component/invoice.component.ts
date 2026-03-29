import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTableModule } from '@angular/material/table';
import { UntilDestroy } from '@ngneat/until-destroy';
import { BehaviorSubject, concatMap, forkJoin, from, map, Observable, of, take, tap } from 'rxjs';
import { v4 as uuid } from 'uuid';
import { isValidFiniteNumber } from '../util/number.util';
import { ServiceModule } from '../service/service.module';
import { RestModule } from '../rest/rest.module';
import { Invoice, InvoiceInclude, InvoiceItem, InvoiceWorkflow } from '../rest/model/invoice';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
import { InvoiceItemRestService, InvoiceRestService } from '../rest/invoice-rest.service';
import { Warehouse } from '../rest/model/warehouse';
import { PartyRestService } from '../rest/party-rest.service';
import { Party } from '../rest/model/party';
import { isStringEqual, trimString } from '../util/string.util';
import { WarehouseItemMovement } from '../rest/model/warehouse-item-movement';
import { EditPriceComponent } from './edit-price.component';
import { Price } from '../rest/model/price';
import { TimestampPipe } from '../pipe/timestamp.pipe';
import { MatDatepicker, MatDatepickerInput, MatDatepickerToggle } from '@angular/material/datepicker';
import { dateWithoutTime } from '../util/date.util';

const INVOICE_ITEM_FETCH = 'warehouseItemMovement';

@Component({
    selector: 'mlws-invoice', //
    imports: [ //
        CommonModule, FormsModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, ServiceModule, RestModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, //
        MatSelectModule, MatCheckboxModule, MatTableModule, EditPriceComponent, TimestampPipe, MatDatepicker, MatDatepickerInput, MatDatepickerToggle //
    ],
    templateUrl: './invoice.component.html',
    styleUrls: ['./invoice.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class InvoiceComponent implements OnInit {
    protected readonly changeDetectorRef = inject(ChangeDetectorRef);
    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
    protected readonly invoiceRestService = inject(InvoiceRestService);
    protected readonly invoiceItemRestService = inject(InvoiceItemRestService);
    protected readonly partyRestService = inject(PartyRestService);
    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    protected loadingCount = 0;
    protected warehouses: Warehouse[] = [];
    protected readonly invoice$ = new BehaviorSubject<Invoice | undefined>(undefined);
    protected readonly invoiceItemRows$ = new BehaviorSubject<InvoiceItemRow[]>([]);
    protected readonly parties$ = new BehaviorSubject<Party[]>([]);
    protected invoiceOriginal: Invoice | undefined;
    protected readonly invoiceWorkflows: InvoiceWorkflow[];

    protected invoiceItemTableColumnKeys: string[] = [];

    @Input()
    public invoiceId: number | null | undefined;

    public constructor() {
        this.updateInvoiceItemTableColumnKeys();
        this.invoiceWorkflows = Object.keys(InvoiceWorkflow) //
            .map(k => InvoiceWorkflow[k as keyof typeof InvoiceWorkflow]) //
            .sort();
    }

    public ngOnInit(): void {
        this.load();
    }

    protected get party(): Party | undefined {
        const party = this.invoice$.getValue()?.party;
        return party === undefined ? undefined : this.parties$.getValue().find(p => p.id === party.id);
    }

    protected set party(party: Party | undefined) {
        const invoice = this.invoice$.getValue();
        if (!invoice) {
            throw new Error('invoice not yet loaded!')
        }
        invoice.party = party;
    }

    protected updateInvoiceItemTableColumnKeys() {
        const columnKeys = ["include", "movementCreated", "warehouseLabel", "sku", "quantity"];
        if (this.invoice$.getValue()?.draft) {
            columnKeys.push("price");
        } else {
            columnKeys.push("priceSingleNet", "priceSingleGross");
        }
        columnKeys.push("priceTotalNet", "priceTotalGross");
        if (this.invoice$.getValue()?.draft) {
            columnKeys.push("removeAction");
        }
        this.invoiceItemTableColumnKeys = columnKeys;
        this.changeDetectorRef.markForCheck();
    }
    
    protected load(): void {
        this.loadingCountInc();
        this.warehouseSelectorService.getWarehouses$() //
            .pipe(take(1)) //
            .subscribe(warehouses => {
                this.warehouses = warehouses;
                this.loadingCountDec();
            });

        this.loadParties();

        if (isValidFiniteNumber(this.invoiceId) && this.invoiceId! >= 0) {
            this.loadingCountInc();
            this.invoiceRestService.getInvoice(this.invoiceId!) //
                .pipe(take(1)) //
                .subscribe(invoice => {
                    this.invoice$.next(JSON.parse(JSON.stringify(invoice)));
                    this.invoiceOriginal = invoice;
                    this.updateInvoiceItemTableColumnKeys();
                    this.loadingCountDec();
                });
            this.loadInvoiceItems();
        } else if (isValidFiniteNumber(this.invoiceId) && this.invoiceId! < 0) {
            const invoice: Invoice = { draft: true };
            this.invoice$.next(JSON.parse(JSON.stringify(invoice)));
            this.invoiceOriginal = invoice;
            this.invoiceItemRows$.next([]);
            this.updateInvoiceItemTableColumnKeys();
        } else {
            this.invoiceOriginal = undefined;
            this.invoice$.next(undefined);
            this.invoiceItemRows$.next([]);
            this.updateInvoiceItemTableColumnKeys();
        }
    }

    protected loadInvoiceItems(pageNumber = 1, invoiceItemRows: InvoiceItemRow[] = []): void {
        this.loadingCountInc();
        this.invoiceItemRestService.getInvoiceItemPage({ filterInvoiceId: this.invoiceId??0, fetch: INVOICE_ITEM_FETCH }) //
            .pipe(take(1)) //
            .subscribe(invoiceItemPage => {
                invoiceItemRows.push(...this.toInvoiceItemRows(invoiceItemPage.items));
                if (pageNumber < invoiceItemPage.lastPageNumber!) {
                    this.loadInvoiceItems(pageNumber + 1, invoiceItemRows);
                } else {
                    this.invoiceItemRows$.next(invoiceItemRows);
                }
                this.loadingCountDec();
            });
    }

    protected loadParties(pageNumber = 1, parties: Party[] = []): void {
        this.loadingCountInc();
        this.partyRestService.getPartyPage({ pageNumber, pageSize: 500 })
            .pipe(take(1))
            .subscribe(partyPage => {
                parties.push(...(partyPage.items??[]));
                if (pageNumber < partyPage.lastPageNumber!) {
                    this.loadParties(pageNumber + 1, parties);
                } else {
                    this.parties$.next(parties);
                }
                this.loadingCountDec();
            });
    }

    private loadingCountInc(): void {
        if (++this.loadingCount === 1) {
            this.loading$.next(true);
        }
    }

    private loadingCountDec(): void {
        if (--this.loadingCount === 0) {
            this.loading$.next(false);
        }
    }

    protected isAutoCreateInvoiceItemsDisabled(): boolean {
        // The invoice must be properly stored on the server-side (with up-to-date infos) for this
        // operation to work correctly on the server-side. Hence, it must have an id and it must
        // not be dirty. Also, we reload the entire component and thus, isInvoiceDirty() is not
        // sufficient as dirty invoice-items would be implicitly reverted.
        const invoice = this.invoice$.getValue();
        return invoice?.id === undefined
            || this.isDirty()
            || !invoice.draft;
    }

    protected isPartyReadonly(): boolean {
        const invoice = this.invoice$.getValue();
        if (!invoice) {
            return true;
        }
        return !invoice.draft || this.invoiceItemRows$.getValue().length !== 0;
    }

    protected isWorkflowReadonly(): boolean {
        return this.isPartyReadonly();
    }

    protected isSaveDisabled(): boolean {
        const invoice = this.invoice$.getValue();
        if (!this.party || !invoice?.workflow) {
            return true;
        }
        if (!invoice.draft) {
            if (this.isPaidDirty()) {
                return false; // "paid" is the only thing that can be changed after finalizing
            }
            return true;
        }
        return !this.isDirty();
    }

    protected isRevertDisabled(): boolean {
        return !this.isDirty();
    }

    protected isFinalizeDisabled(): boolean {
        const invoice = this.invoice$.getValue();
        return invoice?.id === undefined
            || this.isDirty()
            || !invoice.draft
            || !this.isInvoiceItemsFinalizable();
    }

    protected isInvoiceItemsFinalizable(): boolean {
        const invoiceItemRows = this.invoiceItemRows$.getValue().filter(row => row.invoiceItem.include === InvoiceInclude.INCLUDE);
        if (invoiceItemRows.length === 0) {
            return false;
        }
        return !invoiceItemRows.find(row => row.invoiceItem.price === undefined);
    }

    protected isDirty(): boolean {
        if (this.loading$.getValue()) {
            return false;
        }
        return this.isInvoiceDirty() || this.isInvoiceItemsDirty();
    }

    protected isInvoiceDirty(): boolean {
        const invoice = this.invoice$.getValue();
        if (!invoice || !this.invoiceOriginal) {
            return false;
        }
        return invoice.party?.id != this.invoiceOriginal.party?.id //
            || invoice.workflow != this.invoiceOriginal.workflow //
            || this.isPaidDirty() //
            || !isStringEqual(invoice.description, this.invoiceOriginal.description);
    }

    protected isInvoiceItemsDirty(): boolean {
        const invoiceItemRows = this.invoiceItemRows$.getValue();
        for (const invoiceItemRow of invoiceItemRows) {
            if (this.isInvoiceItemDirty(invoiceItemRow)) {
                return true;
            }
        }
        return false;
    }

    protected isInvoiceItemDirty(invoiceItemRow: InvoiceItemRow): boolean {
        const invoiceItemOriginal = invoiceItemRow.invoiceItemOriginal;
        if (invoiceItemRow.action !== InvoiceItemRowAction.NONE || !invoiceItemOriginal) {
            return true;
        }
        const invoiceItem = invoiceItemRow.invoiceItem;
        return invoiceItem.include !== invoiceItemOriginal.include || invoiceItem.quantity !== invoiceItemOriginal.quantity || !this.isPriceEqual(invoiceItem.price, invoiceItemOriginal.price);
    }

    private isPriceEqual(price1?: Price, price2?: Price): boolean {
        if (price1 === undefined && price2 === undefined) {
            return true;
        }
        if (price1 === undefined || price2 === undefined) {
            return false;
        }
        if (price1.quantity !== price2.quantity
            || price1.taxPercent !== price2.taxPercent) {
            return false;
        }
        if (price1.priceTotalGross !== undefined
            && price2.priceTotalGross !== undefined) {
            return price1.priceTotalGross === price2.priceTotalGross;
        }
        if (price1.priceTotalNet !== undefined
            && price2.priceTotalNet !== undefined) {
            return price1.priceTotalNet === price2.priceTotalNet;
        }
        if (price1.priceSingleGross !== undefined
            && price2.priceSingleGross !== undefined) {
            return price1.priceSingleGross === price2.priceSingleGross;
        }
        if (price1.priceSingleNet !== undefined
            && price2.priceSingleNet !== undefined) {
            return price1.priceSingleNet === price2.priceSingleNet;
        }
        return price1.priceSingleGross === price2.priceSingleGross
            && price1.priceSingleNet === price2.priceSingleNet
            && price1.priceTotalGross === price2.priceTotalGross
            && price1.priceTotalNet === price2.priceTotalNet;
    }

    protected getPriceTotalNetSum(): number {
        return this.invoiceItemRows$.getValue().map(row => row.priceCalculated?.priceTotalNet ?? 0).reduce((previousValue, currentValue) => previousValue + currentValue, 0);
    }
    protected getPriceTotalGrossSum(): number {
        return this.invoiceItemRows$.getValue().map(row => row.priceCalculated?.priceTotalGross ?? 0).reduce((previousValue, currentValue) => previousValue + currentValue, 0);
    }
    protected getQuantitySum(): number {
        return this.invoiceItemRows$.getValue().map(row => row.invoiceItem.quantity ?? 0).reduce((previousValue, currentValue) => previousValue + currentValue, 0);
    }

    private toInvoiceItemRows(invoiceItems?: InvoiceItem[]): InvoiceItemRow[] {
        const rows: InvoiceItemRow[] = [];
        if (invoiceItems) {
            for (const invoiceItem of invoiceItems) {
                rows.push(this.toInvoiceItemRow(invoiceItem))
            }
        }
        return rows;
    }

    private toInvoiceItemRow(invoiceItem: InvoiceItem): InvoiceItemRow {
        const row: InvoiceItemRow = {
            invoiceItem: JSON.parse(JSON.stringify(invoiceItem)),
            action: InvoiceItemRowAction.NONE,
            invoiceItemRowTrackBy: '' + (invoiceItem.id !== undefined ? invoiceItem.id : uuid()),
            invoiceItemOriginal: invoiceItem,
            priceCalculated: invoiceItem.price
        }
        return row;
    }

    protected invoiceItemRowTrackBy(index: number, invoiceItemRow: InvoiceItemRow): string {
        return invoiceItemRow.invoiceItemRowTrackBy;
    }

    protected onInvoiceItemRowClick(invoiceItemRow: InvoiceItemRow): void {
        console.info("item clicked", invoiceItemRow);
    }

    protected onAutoCreateInvoiceItemsClick(): void {
        const invoiceId = this.invoice$.getValue()?.id;
        if (invoiceId === undefined) {
            throw new Error('invoice.id is undefined');
        }
        this.loadingCountInc();
        this.invoiceItemRestService.autoCreateInvoiceItems({ invoice: { id: invoiceId } }).pipe(take(1)).subscribe(() => {
            this.load();
            this.loadingCountDec();
        });
    }

    protected markPaid(): void {
        const invoice = this.invoice$.getValue();
        if (!invoice?.id) {
            throw new Error('invoice.id is undefined');
        }
        this.loadingCountInc();
        this.invoiceRestService.markInvoicePaid(invoice.id, dateWithoutTime(invoice.paid)).pipe(take(1)).subscribe((i) => {
            this.load();
            this.loadingCountDec();
        });
    }

    protected isPaidDirty(): boolean {
        const invoice = this.invoice$.getValue();
        return dateWithoutTime(invoice?.paid) != dateWithoutTime(this.invoiceOriginal?.paid);
    }

    protected onSaveClick(): void {
        const invoiceDirty = this.isInvoiceDirty();
        let invoice = this.invoice$.getValue();
        invoice = invoice ? JSON.parse(JSON.stringify(invoice)) : undefined;
        if (!invoice) {
            return;
        }
        if (!invoice.draft) {
            if (this.isPaidDirty()) {
                this.markPaid();
                return;
            }
            throw new Error('invoice is not a draft!');
        }
        const dirtyInvoiceItemRows = this.invoiceItemRows$.getValue().filter(row => this.isInvoiceItemDirty(row)).map((row: InvoiceItemRow) => JSON.parse(JSON.stringify(row))) as InvoiceItemRow[];

        invoice.description = trimString(invoice.description);

        // read-only properties can & should be cleared
        invoice.party = { id: invoice.party?.id };
        invoice.draft = undefined;
        invoice.booked = undefined;
        invoice.finalized = undefined;
        invoice.finalizedByUserId = undefined;
        invoice.changed = undefined;
        invoice.changedByUserId = undefined;
        invoice.created = undefined;
        invoice.createdByUserId = undefined;
        invoice.deleted = undefined;
        invoice.deletedByUserId = undefined;

        dirtyInvoiceItemRows.forEach((row: InvoiceItemRow) => {
            const ii = row.invoiceItem;
            ii.invoice = { id: invoice.id };
            ii.warehouseItemMovement = ii.warehouseItemMovement ? { id: ii.warehouseItemMovement?.id } as WarehouseItemMovement : undefined;
            ii.draft = undefined;
            ii.finalized = undefined;
            ii.finalizedByUserId = undefined;
            ii.changed = undefined;
            ii.changedByUserId = undefined;
            ii.created = undefined;
            ii.createdByUserId = undefined;
            ii.deleted = undefined;
            ii.deletedByUserId = undefined;
        });

        const saveParam = new SaveParam(invoice, invoiceDirty, dirtyInvoiceItemRows);
        this._save_askForUserConfirmationIfNeeded(saveParam).pipe( //
            tap(() => this.loadingCountInc()),
            concatMap(() => this._save_invoice(saveParam)),
            concatMap(() => this._save_invoiceItems(saveParam)) //
        ).subscribe(() => {
            if (!saveParam.cancelled) {
                this.load();
            }
            this.loadingCountDec();
        });
    }

    protected _save_askForUserConfirmationIfNeeded(param: SaveParam): Observable<boolean> {
        const newlyExcludedRows = param.dirtyInvoiceItemRows.filter(row => row.invoiceItem.include === InvoiceInclude.EXCLUDE && row.invoiceItemOriginal?.include !== InvoiceInclude.EXCLUDE);
        if (newlyExcludedRows.length === 0) {
            return of(true);
        }
        const element = document.createElement('div');
        element.innerHTML = `Do you want to permanently exclude ${newlyExcludedRows.length} more warehouse-item-movements from all invoices?<br><br>` +
            'This means the excluded movements are linked to this invoice as "excluded" (consumed), and thus neither charged now nor ' +
            'in any future invoice.<br><br>' +
            'If you want to exclude the movements from this invoice, but add them again to the next invoice (when auto-creating items), ' +
            'press "No" now and instead remove the items from this invoice.';

        return from(swal({
            title: 'Permanently exclude movements?',
            // text: xxx, // does not support HTML-tags
            content: { element },
            icon: 'assets/dialog/question.svg',
            buttons: ['No', 'Yes']
        })).pipe(
            map((dialogResult: any) => {
                if (dialogResult) {
                    return true;
                }
                param.cancelled = true;
                return false;
            })
        );
    }

    protected _save_invoice(param: SaveParam): Observable<Invoice> {
        if (param.cancelled) {
            return of(param.invoice);
        }
        if (!param.invoiceDirty) {
            return of(param.invoice);
        }
        if (param.invoice.id === undefined) {
            return this.invoiceRestService.postInvoice(param.invoice).pipe(tap(i => {
                param.invoice = i;
                this.invoiceId = i.id;
            }));
        }
        return this.invoiceRestService.putInvoice(param.invoice).pipe(tap(i => param.invoice = i));
    }

    protected _save_invoiceItems(param: SaveParam): Observable<any> {
        if (param.cancelled) {
            return of(undefined);
        }
        if (param.dirtyInvoiceItemRows.length === 0) {
            return of(undefined);
        }
        return forkJoin(
            param.dirtyInvoiceItemRows.map(invoiceItemRow => {
                switch (invoiceItemRow.action) {
                    case InvoiceItemRowAction.ADD:
                        return this.invoiceItemRestService.postInvoiceItem(invoiceItemRow.invoiceItem);
                    case InvoiceItemRowAction.REMOVE:
                        return this.invoiceItemRestService.deleteInvoiceItem(invoiceItemRow.invoiceItem.id!);
                    case InvoiceItemRowAction.NONE:
                        return this.invoiceItemRestService.putInvoiceItem(invoiceItemRow.invoiceItem);
                    default:
                        throw new Error("Unknown action: " + invoiceItemRow.action);
                }
            })
        );
    }

    protected onRevertClick(): void {
        if (!this.invoiceOriginal) {
            return;
        }
        const invoice = JSON.parse(JSON.stringify(this.invoiceOriginal));
        this.invoice$.next(invoice);
        const invoiceItemRows = this.invoiceItemRows$.getValue();
        for (const row of invoiceItemRows) {
            row.invoiceItem = JSON.parse(JSON.stringify(row.invoiceItemOriginal));
            row.action = InvoiceItemRowAction.NONE;
            row.priceCalculated = row.invoiceItem.price;
        }
        this.invoiceItemRows$.next(invoiceItemRows);
        this.changeDetectorRef.markForCheck();
    }

    protected onFinalizeClick(): void {
        const invoiceId = this.invoice$.getValue()?.id;
        if (invoiceId === undefined) {
            throw new Error("invoiceId is undefined");
        }
        from(swal({
            title: 'Finalize invoice?',
            text: 'Do you want to irrevocably finalize and charge this invoice?', // does not support HTML-tags
            icon: 'assets/dialog/question.svg',
            buttons: ['No', 'Yes']
        })).pipe(
            concatMap((dialogResult: any) => {
                if (dialogResult) {
                    this.loadingCountInc();
                    return this.finalizeInvoice(invoiceId).pipe(map(() => {
                        this.load();
                        this.loadingCountDec();
                        return true;
                    }));
                }
                return of(false);
            })
        ).subscribe();
    }

    protected finalizeInvoice(invoiceId: number): Observable<any> {
        return this.invoiceRestService.finalizeInvoice(invoiceId);
    }

    protected getWarehouse(invoiceItemRow: InvoiceItemRow): Warehouse | undefined {
        const warehouseId = invoiceItemRow.invoiceItem.warehouseItemMovement?.warehouseId;
        if (!warehouseId) {
            return undefined;
        }
        const warehouse = this.warehouses.find(wh => wh.id === warehouseId);
        if (!warehouse) {
            throw new Error(`No warehouse with id=${warehouseId} found!`);
        }
        return warehouse;
    }

    protected onIncludeClick(row: InvoiceItemRow): void {
        if (!this.invoice$.getValue()?.draft) {
            return;
        }
        switch (row.invoiceItem.include) {
            case InvoiceInclude.INCLUDE:
                row.invoiceItem.include = InvoiceInclude.EXCLUDE;
                return;
            case InvoiceInclude.EXCLUDE:
                row.invoiceItem.include = InvoiceInclude.INCLUDE;
                return;
            default:
                throw new Error('Unknown include: ' + row.invoiceItem.include);
        }
    }

    protected getRemoveButtonClass(row: InvoiceItemRow): string {
        return row.action === InvoiceItemRowAction.REMOVE ? 'remove-button-checked' : 'remove-button-unchecked';
    }

    protected onRemoveInvoiceItemRowClick(row: InvoiceItemRow): void {
        if (!this.invoice$.getValue()?.draft) {
            return;
        }
        switch (row.action) {
            case InvoiceItemRowAction.ADD: {
                const rows = [...this.invoiceItemRows$.getValue()];
                const index = rows.indexOf(row);
                if (index >= 0) {
                    rows.splice(index, 1);
                    this.invoiceItemRows$.next(rows);
                }
                return;
            }
            case InvoiceItemRowAction.REMOVE: {
                row.action = InvoiceItemRowAction.NONE;
                return;
            }
            case InvoiceItemRowAction.NONE: {
                row.action = InvoiceItemRowAction.REMOVE;
                return;
            }
            default: {
                throw new Error('Unknown action: ' + row.action);
            }
        }
    }

    protected getInvoiceItemTableRowClass(row: InvoiceItemRow): string {
        switch (row.action) {
            case InvoiceItemRowAction.ADD:
                return 'to-add';
            case InvoiceItemRowAction.REMOVE:
                return 'to-remove';
            default:
                return '';
        }
    }

    protected readonly InvoiceInclude = InvoiceInclude;
}

class SaveParam {
    public cancelled = false;

    public constructor(public invoice: Invoice, public invoiceDirty: boolean, public dirtyInvoiceItemRows: InvoiceItemRow[]) { /* empty */ }
}

enum InvoiceItemRowAction {
    NONE = 'NONE',
    ADD = 'ADD',
    REMOVE = 'REMOVE'
}

interface InvoiceItemRow {
    invoiceItem: InvoiceItem;
    action: InvoiceItemRowAction;
    invoiceItemRowTrackBy: string;
    invoiceItemOriginal?: InvoiceItem;
    priceCalculated?: Price;
}
