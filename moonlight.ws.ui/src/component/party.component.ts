import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ServiceModule } from '../service/service.module';
import { RestModule } from '../rest/rest.module';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { UntilDestroy } from '@ngneat/until-destroy';
import { BehaviorSubject, concatMap, filter, forkJoin, Observable, of, take, tap } from 'rxjs';
import { v4 as uuid } from 'uuid';
import { Party } from '../rest/model/party';
import { isValidFiniteNumber } from '../util/number.util';
import { PartyRestService } from '../rest/party-rest.service';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { isStringEqual, trimString } from '../util/string.util';
import { SupplierRestService } from '../rest/supplier-rest.service';
import { ConsigneeRestService } from '../rest/consignee-rest.service';
import { Consignee } from '../rest/model/consignee';
import { Warehouse } from '../rest/model/warehouse';
import { CountryService } from '../service/country.service';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
import { AddConsigneeOverlayComponent } from './add-consignee-overlay.component';
import { getL10n } from '../util/i18n.util';
import { Country } from '../rest/model/country';
import { Region } from '../rest/model/region';

@Component({
    selector: 'mlws-party',
    imports: [ //
        CommonModule, FormsModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, ServiceModule, RestModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, //
        MatSelectModule, MatCheckboxModule, MatTableModule, AddConsigneeOverlayComponent //
    ],
    templateUrl: './party.component.html',
    styleUrls: ['./party.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class PartyComponent implements OnInit {
    protected readonly changeDetectorRef = inject(ChangeDetectorRef);
    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
    protected readonly partyRestService = inject(PartyRestService);
    protected readonly supplierRestService = inject(SupplierRestService);
    protected readonly consigneeRestService = inject(ConsigneeRestService);
    protected readonly countryService = inject(CountryService);
    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    protected loadingCount = 0;
    protected warehouses: Warehouse[] = [];
    protected readonly party$ = new BehaviorSubject<Party | undefined>(undefined);
    protected readonly consigneeRows$ = new BehaviorSubject<ConsigneeRow[]>([]);
    protected supplier = false;
    protected partyOriginal: Party | undefined;
    protected readonly createConsigneeOverlayVisible$ = new BehaviorSubject<boolean>(false);

    protected readonly consigneeTableColumnKeys = ["warehouseLabel", "removeAction"]; //, "warehouseId"];

    @Input()
    public partyId: number | null | undefined;

    public get country(): Country | undefined {
        const party = this.party$.getValue();
        if (!party) {
            return undefined;
        }
        return this.countryService.getCountryByIsoCode(party.countryIsoCode);
    }

    public set country(country: Country | undefined) {
        const party = this.party$.getValue();
        if (!party) {
            throw new Error('Party not yet loaded!');
        }
        if (!country) {
            party.countryIsoCode = undefined;
            return;
        }
        party.countryIsoCode = country.a2;
    }

    public get regions(): Region[] {
        const country = this.country;
        if (!country) {
            return [];
        }
        return [{}, ...(country.regions??[])];
    }

    public get region(): Region | undefined {
        const party = this.party$.getValue();
        if (!party) {
            return undefined;
        }
        const country = this.country;
        if (!country) {
            return undefined;
        }
        return (country.regions??[]).find(r => r.regionCode === party.regionCode);
    }

    public set region(region: Region | undefined) {
        const party = this.party$.getValue();
        if (!party) {
            throw new Error('Party not yet loaded!');
        }
        if (!region) {
            party.regionCode = undefined;
            return;
        }
        party.regionCode = region.regionCode;
    }

    public constructor() {
        // nothing yet
    }

    public ngOnInit(): void {
        this.load();
    }

    protected load(): void {
        this.loadingCountInc();
        this.countryService.loading$ //
            .pipe(filter(loading => !loading), take(1)) //
            .subscribe(() => this.loadingCountDec());

        if (isValidFiniteNumber(this.partyId) && this.partyId! >= 0) {
            this.loadingCountInc();
            this.warehouseSelectorService.getWarehouses$().pipe( //
                take(1), //
                tap(warehouses => this.warehouses = warehouses), //
                concatMap(() => this.partyRestService.getParty(this.partyId!, { fetch: 'consignees' })), //
                take(1) //
            ).subscribe(party => {
                this.loadingCountDec();
                this.afterLoad(party);
            });
        } else if (isValidFiniteNumber(this.partyId) && this.partyId! < 0) {
            const party: Party = { code: uuid().substring(24, 36), active: true };
            this.afterLoad(party);
        } else {
            this.partyOriginal = undefined;
            this.supplier = false;
            this.party$.next(undefined);
            this.consigneeRows$.next([]);
        }
    }

    protected afterLoad(party: Party): void {
        this.partyOriginal = JSON.parse(JSON.stringify(party));
        this.supplier = !!party.supplier;
        this.party$.next(party);
        this.consigneeRows$.next(this.toConsigneeRows(party.consignees));
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

    protected isSaveDisabled(): boolean {
        return !this.isDirty();
    }

    protected isRevertDisabled(): boolean {
        return !this.isDirty();
    }

    protected isDirty(): boolean {
        const party = this.party$.getValue();
        if (!party || !this.partyOriginal) {
            return false;
        }
        return party.active !== this.partyOriginal.active
            || !isStringEqual(party.code, this.partyOriginal.code)
            || !isStringEqual(party.name, this.partyOriginal.name)
            || !isStringEqual(party.email, this.partyOriginal.email)
            || !isStringEqual(party.phone, this.partyOriginal.phone)
            || !isStringEqual(party.street1, this.partyOriginal.street1)
            || !isStringEqual(party.street2, this.partyOriginal.street2)
            || !isStringEqual(party.street3, this.partyOriginal.street3)
            || !isStringEqual(party.zip, this.partyOriginal.zip)
            || !isStringEqual(party.city, this.partyOriginal.city)
            || !isStringEqual(party.regionCode, this.partyOriginal.regionCode)
            || !isStringEqual(party.countryIsoCode, this.partyOriginal.countryIsoCode)
            || !isStringEqual(party.taxNo, this.partyOriginal.taxNo)
            || !isStringEqual(party.website, this.partyOriginal.website)
            || !isStringEqual(party.description, this.partyOriginal.description)
            || this.supplier !== !!party.supplier
            || this.consigneeRows$.getValue().find(cr => cr.action !== ConsigneeRowAction.NONE) !== undefined
            ;
    }

    protected onSaveClick(): void {
        let party = this.party$.getValue();
        party = party ? JSON.parse(JSON.stringify(party)) : undefined;
        if (!party) {
            return;
        }
        party.code = trimString(party.code);
        party.name = trimString(party.name);
        party.email = trimString(party.email);
        party.phone = trimString(party.phone);
        party.street1 = trimString(party.street1);
        party.street2 = trimString(party.street2);
        party.street3 = trimString(party.street3);
        party.zip = trimString(party.zip);
        party.city = trimString(party.city);
        party.regionCode = trimString(party.regionCode);
        party.countryIsoCode = trimString(party.countryIsoCode);
        party.taxNo = trimString(party.taxNo);
        party.website = trimString(party.website);
        party.description = trimString(party.description);

        // read-only properties can & should be cleared
        party.supplier = undefined;
        party.consignees = undefined;

        const saveParam = new SaveParam(party, this.supplier, this.consigneeRows$.getValue());
        this._save_party(saveParam).pipe( //
            concatMap(() => this._save_supplier(saveParam)), //
            concatMap(() => this._save_consignees(saveParam)) //
        ).subscribe(() => this.load());
    }

    protected _save_party(param: SaveParam): Observable<Party> {
        if (param.party.id === undefined) {
            return this.partyRestService.postParty(param.party).pipe(tap(p => {
                param.party = p;
                this.partyId = p.id;
            }));
        }
        return this.partyRestService.putParty(param.party).pipe(tap(p => param.party = p));
    }

    protected _save_supplier(param: SaveParam): Observable<any> {
        const supplierId = param.party.supplier?.id;
        const oldSupplier = supplierId !== undefined;
        if (param.supplier === oldSupplier) { // no change => immediately return
            return of(undefined);
        }
        if (param.supplier) {
            return this.supplierRestService.postSupplier({ //
                party: { id: param.party.id }, //
            });
        } else if (supplierId !== undefined) {
            return this.supplierRestService.deleteSupplier(supplierId);
        }
        return of(undefined);
    }

    protected _save_consignees(param: SaveParam): Observable<any> {
        const consigneeRows = param.consigneeRows.filter(cr => cr.action !== ConsigneeRowAction.NONE);
        if (consigneeRows.length === 0) {
            return of(undefined);
        }
        return forkJoin(
            consigneeRows.map(consigneeRow => {
                switch (consigneeRow.action) {
                    case ConsigneeRowAction.ADD:
                        return this.consigneeRestService.postConsignee({
                            party: { id: this.partyId! },
                            warehouseId: consigneeRow.warehouseId
                        });
                    case ConsigneeRowAction.REMOVE:
                        return this.consigneeRestService.deleteConsignee(consigneeRow.id!);
                    default:
                        throw new Error("Unknown action: " + consigneeRow.action);
                }
            })
        );
    }

    protected onRevertClick(): void {
        if (!this.partyOriginal) {
            return;
        }
        const party = JSON.parse(JSON.stringify(this.partyOriginal));
        this.supplier = !!party.supplier;
        this.party$.next(party);
        this.consigneeRows$.next(this.toConsigneeRows(party.consignees));
        this.changeDetectorRef.markForCheck();
    }

    protected toConsigneeRows(consignees?: Consignee[]): ConsigneeRow[] {
        const rows: ConsigneeRow[] = [];
        if (consignees) {
            for (const consignee of consignees) {
                rows.push(this.toConsigneeRow(consignee))
            }
        }
        return rows;
    }

    private toConsigneeRow(consignee: Consignee): ConsigneeRow {
        const row = JSON.parse(JSON.stringify(consignee));
        row.action = ConsigneeRowAction.NONE;
        row.warehouse = this.warehouses.find(warehouse => warehouse.id === row.warehouseId);
        return row;
    }

    protected consigneeRowTrackBy(index: number, consigneeRow: ConsigneeRow): number {
        return consigneeRow.id!;
    }

    protected onConsigneeRowClick(row: ConsigneeRow): void {
        // empty
    }

    protected onAddConsigneeClick(): void {
        this.createConsigneeOverlayVisible$.next(true);
    }

    protected getRemoveButtonClass(row: ConsigneeRow): string {
        return row.action === ConsigneeRowAction.REMOVE ? 'remove-button-checked' : 'remove-button-unchecked';
    }

    protected onRemoveConsigneeClick(row: ConsigneeRow): void {
        switch (row.action) {
            case ConsigneeRowAction.ADD: {
                const rows = [...this.consigneeRows$.getValue()];
                const index = rows.indexOf(row);
                if (index >= 0) {
                    rows.splice(index, 1);
                    this.consigneeRows$.next(rows);
                }
                return;
            }
            case ConsigneeRowAction.REMOVE: {
                row.action = ConsigneeRowAction.NONE;
                break;
            }
            case ConsigneeRowAction.NONE: {
                row.action = ConsigneeRowAction.REMOVE;
                break;
            }
            default: {
                throw new Error('Unknown action: ' + row.action);
            }
        }
    }

    protected consigneeAdded(consignee: Consignee | undefined) {
        this.createConsigneeOverlayVisible$.next(false);
        if (consignee) {
            const consigneeRows = this.consigneeRows$.getValue();
            const consigneeRow = this.toConsigneeRow(consignee);
            consigneeRow.action = ConsigneeRowAction.ADD;
            consigneeRows.push(consigneeRow);
            this.consigneeRows$.next(consigneeRows);
        }
    }

    protected readonly ConsigneeRowAction = ConsigneeRowAction;

    protected getConsigneeTableRowClass(consigneeRow: ConsigneeRow): string {
        switch (consigneeRow.action) {
            case ConsigneeRowAction.ADD:
                return 'to-add';
            case ConsigneeRowAction.REMOVE:
                return 'to-remove';
            default:
                return '';
        }
    }

    protected readonly getL10n = getL10n;
}

class SaveParam {
    public constructor(public party: Party, public supplier: boolean, public consigneeRows: ConsigneeRow[]) { /* empty */ }
}

enum ConsigneeRowAction {
    NONE = 'NONE',
    ADD = 'ADD',
    REMOVE = 'REMOVE'
}

interface ConsigneeRow extends Consignee {
    action: ConsigneeRowAction;
    warehouse: Warehouse;
}
