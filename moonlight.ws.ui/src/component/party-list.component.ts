import { ChangeDetectionStrategy, Component, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BehaviorSubject, combineLatest, concatMap, debounceTime, tap } from 'rxjs';
import { ServiceModule } from '../service/service.module';
import { RestModule } from '../rest/rest.module';
import { Party, PartyPage } from '../rest/model/party';
import { createNumberPropertyDefined } from '../util/component.util';
import { PartyRestService } from '../rest/party-rest.service';

const PAGE_INDEX = "mlws.PartyListComponent.pageIndex";
const PAGE_SIZE = "mlws.PartyListComponent.pageSize";

@Component({
    selector: 'mlws-party-list',
    imports: [
        CommonModule, MatButtonModule, MatPaginatorModule, MatTableModule, FormsModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, ServiceModule, RestModule,
        MatProgressSpinnerModule, MatIconModule
    ],
    templateUrl: './party-list.component.html',
    styleUrls: ['./party-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class PartyListComponent {
    protected readonly router = inject(Router);
    protected readonly partyRestService = inject(PartyRestService);

    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    protected readonly columnKeys = ["name", "code"];
    protected readonly partyPage$ = new BehaviorSubject<PartyPage | undefined>(undefined);
    protected readonly pageIndex$ : BehaviorSubject<number>;
    protected readonly pageSize$: BehaviorSubject<number>;
    protected readonly filterCode$ = new BehaviorSubject<string | undefined>(undefined);
    protected readonly filterName$ = new BehaviorSubject<string | undefined>(undefined);

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
            this.filterCode$, //
            this.filterName$, //
            this.pageIndex$, //
            this.pageSize$ //
        ]).pipe( //
            untilDestroyed(this), //
            tap(() => this.loading$.next(true)), //
            debounceTime(500), //
            concatMap(([filterCode, filterName, pageIndex, pageSize]) => //
                this.partyRestService.getPartyPage({ //
                    filterCode: filterCode ? `/${filterCode.replace("*", ".*")}.*/i` : undefined, //
                    filterName: filterName ? `/${filterName.replace("*", ".*")}.*/i` : undefined, //
                    pageNumber: pageIndex + 1, //
                    pageSize,
                    sort: "name"
                }))).subscribe(partyPage => {
            this.loading$.next(false);
            this.partyPage$.next(partyPage);
            if (this.pageIndex$.getValue() + 1 > partyPage.lastPageNumber!) {
                this.pageIndex$.next(partyPage.lastPageNumber! - 1);
            }
        });
    }

    protected partyTrackBy(index: number, party: Party): number {
        return party.id!;
    }

    protected onClick(row: Party): void {
        this.router.navigate(["party"], { queryParams: { id: row.id } });
    }

    protected onCreatePartyClick(): void {
        this.router.navigate(["party"], { queryParams: { id: -1 } });
    }
}