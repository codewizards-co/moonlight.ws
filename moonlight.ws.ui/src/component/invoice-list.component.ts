import { ChangeDetectionStrategy, Component, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ServiceModule } from '../service/service.module';
import { RestModule } from '../rest/rest.module';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { Router } from '@angular/router';
import { BehaviorSubject, combineLatest, concatMap, debounceTime, tap } from 'rxjs';
import { InvoiceRestService } from '../rest/invoice-rest.service';
import { Invoice, InvoicePage } from '../rest/model/invoice';
import { createNumberPropertyDefined } from '../util/component.util';
import { TimestampPipe } from '../pipe/timestamp.pipe';

const PAGE_INDEX = "mlws.InvoiceListComponent.pageIndex";
const PAGE_SIZE = "mlws.InvoiceListComponent.pageSize";

@Component({
    selector: 'mlws-invoice-list',
    imports: [CommonModule, MatButtonModule, MatPaginatorModule, MatTableModule, FormsModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, ServiceModule, RestModule, MatProgressSpinnerModule, MatIconModule, TimestampPipe],
    templateUrl: './invoice-list.component.html',
    styleUrls: ['./invoice-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class InvoiceListComponent {
    protected readonly router = inject(Router);
    protected readonly invoiceRestService = inject(InvoiceRestService);

    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    protected readonly columnKeys = ["created", "partyName", "workflow", "includedTotalNet", "includedTotalGross", "excludedTotalNet", "excludedTotalGross", "status", "orderId"];
    protected readonly invoicePage$ = new BehaviorSubject<InvoicePage | undefined>(undefined);
    protected readonly pageIndex$ : BehaviorSubject<number>;
    protected readonly pageSize$: BehaviorSubject<number>;
    protected readonly filterPartyId$ = new BehaviorSubject<number | undefined>(undefined);

    public constructor() {
        this.pageIndex$ = createNumberPropertyDefined(this, PAGE_INDEX, 0);
        this.pageSize$ = createNumberPropertyDefined(this, PAGE_SIZE, 10);
        this.initLoadData();
    }

    protected onPageEvent(pageEvent: PageEvent): void {
        this.pageIndex$.next(pageEvent.pageIndex);
        this.pageSize$.next(pageEvent.pageSize);
    }

    protected initLoadData(): void {
        combineLatest([ //
            this.filterPartyId$, //
            this.pageIndex$, //
            this.pageSize$ //
        ]).pipe( //
            untilDestroyed(this), //
            tap(() => this.loading$.next(true)), //
            debounceTime(500), //
            concatMap(([filterPartyId, pageIndex, pageSize]) => //
                this.invoiceRestService.getInvoicePage({ //
                    filterPartyId: filterPartyId, //
                    pageNumber: pageIndex + 1, //
                    pageSize,
                    fetch: "party",
                    sort: "created:desc,id:desc"
                }))).subscribe(invoicePage => {
            this.loading$.next(false);
            this.invoicePage$.next(invoicePage);
            if (this.pageIndex$.getValue() + 1 > invoicePage.lastPageNumber!) {
                this.pageIndex$.next(invoicePage.lastPageNumber! - 1);
            }
        });
    }

    protected getStatus(invoice: Invoice): string {
        if (invoice.paid) {
            return 'paid';
        }
        if (invoice.booked) {
            return 'booked';
        }
        if (invoice.finalized) {
            return 'finalized';
        }
        if (invoice.draft) {
            return 'draft';
        }
        return 'unknown';
    }

    protected invoiceTrackBy(index: number, invoice: Invoice): number {
        return invoice.id!;
    }

    protected onClick(row: Invoice): void {
        this.router.navigate(["invoice"], { queryParams: { id: row.id } });
    }

    protected onCreateInvoiceClick(): void {
        this.router.navigate(["invoice"], { queryParams: { id: -1 } });
    }
}