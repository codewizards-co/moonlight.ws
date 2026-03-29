import { ChangeDetectionStrategy, Component, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ActivatedRoute, Params } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { getValidFiniteNumber } from '../util/number.util';
import { ID_PARAM } from '../util/shared-const';
import { PartyComponent } from '../component/party.component';
import { ServiceModule } from '../service/service.module';

@Component({
    selector: 'mlws-party-page', imports: [ServiceModule, CommonModule, MatProgressSpinnerModule, PartyComponent],
    templateUrl: './party.page.html',
    styleUrls: ['./party.page.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class PartyPage {
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