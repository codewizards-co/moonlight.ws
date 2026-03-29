import { ChangeDetectionStrategy, Component, ViewEncapsulation } from '@angular/core';
import { InvoiceListComponent } from '../component/invoice-list.component';

@Component({
    selector: 'mlws-invoice-list-page', imports: [InvoiceListComponent],
    templateUrl: './invoice-list.page.html',
    styleUrls: ['./invoice-list.page.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InvoiceListPage {

}