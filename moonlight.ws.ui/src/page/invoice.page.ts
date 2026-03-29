import { ChangeDetectionStrategy, Component, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Params } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BehaviorSubject } from 'rxjs';
import { InvoiceComponent } from '../component/invoice.component';
import { ServiceModule } from '../service/service.module';
import { getValidFiniteNumber } from '../util/number.util';
import { ID_PARAM } from '../util/shared-const';

@Component({
    selector: 'mlws-invoice-page', imports: [ServiceModule, CommonModule, MatProgressSpinnerModule, InvoiceComponent],
    templateUrl: './invoice.page.html',
    styleUrls: ['./invoice.page.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class InvoicePage {
    private readonly activatedRoute = inject(ActivatedRoute);
    protected readonly id$ = new BehaviorSubject<number | undefined>(undefined);
    protected readonly loading$ = new BehaviorSubject<boolean>(false);

    public constructor() {
        this.activatedRoute.queryParams
            .pipe(untilDestroyed(this))
            .subscribe((params: Params) => {
                const id: number | undefined = getValidFiniteNumber(params[ID_PARAM]);
                console.debug(`activatedRoute changed: id=${id}`);
                this.id$.next(id);
            });
    }
}