import { ChangeDetectionStrategy, Component, inject, Input, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTimepickerModule } from '@angular/material/timepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import {BehaviorSubject, combineLatest, concatMap, debounceTime, filter, Observable, of, take, tap} from 'rxjs';
import { DateTime } from 'luxon';
import { ServiceModule } from '../service/service.module';
import { RestModule } from '../rest/rest.module';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
import { WarehouseItemMovementRestService } from '../rest/warehouse-item-movement-rest.service';
import { WarehouseItemMovement, WarehouseItemMovementPage, WarehouseItemMovementType } from '../rest/model/warehouse-item-movement';
import { createBooleanPropertyDefined, createDateTimePropertyUndefined, createNumberPropertyDefined, createStringPropertyUndefined } from '../util/component.util';
import { getL10n } from '../util/i18n.util';
import { Warehouse } from '../rest/model/warehouse';
import { getAppUrl } from '../util/url.util';
import {TimestampPipe} from "../pipe/timestamp.pipe";
import { MatCheckbox } from '@angular/material/checkbox';
import {MatOption} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {Supplier} from "../rest/model/supplier";
import {SupplierRestService} from "../rest/supplier-rest.service";
import {getValidFiniteNumber} from "../util/number.util";

const PAGE_INDEX = "mlws.WarehouseItemMovementListComponent.pageIndex";
const PAGE_SIZE = "mlws.WarehouseItemMovementListComponent.pageSize";
const FILTER_SKU = "mlws.WarehouseItemMovementListComponent.filterSku";
const FILTER_PRODUCT_NAME = "mlws.WarehouseItemMovementListComponent.filterProductName";
const FILTER_TYPE = "mlws.WarehouseItemMovementListComponent.filterType";
const FILTER_SUPPLIER_ID = "mlws.WarehouseItemMovementListComponent.filterSupplierId";
const FILTER_CREATED_FROM = "mlws.WarehouseItemMovementListComponent.filterCreatedFrom";
const FILTER_CREATED_TO = "mlws.WarehouseItemMovementListComponent.filterCreatedTo";
const FILTER_VISIBLE = "mlws.WarehouseItemMovementListComponent.filterVisible";
const COLUMN_SKU_VISIBLE = "mlws.WarehouseItemMovementListComponent.columnSkuVisible";
const COLUMN_PRODUCTS_VISIBLE = "mlws.WarehouseItemMovementListComponent.columnProductsVisible";
const COLUMN_FROM_OR_TO_VISIBLE = "mlws.WarehouseItemMovementListComponent.columnFromOrToVisible";
const COLUMN_PRICE_SINGLE_NET_VISIBLE = "mlws.WarehouseItemMovementListComponent.columnPriceSingleNetVisible";
const COLUMN_PRICE_SINGLE_GROSS_VISIBLE = "mlws.WarehouseItemMovementListComponent.columnPriceSingleGrossVisible";
const COLUMN_PRICE_TOTAL_NET_VISIBLE = "mlws.WarehouseItemMovementListComponent.columnPriceTotalNetVisible";
const COLUMN_PRICE_TOTAL_GROSS_VISIBLE = "mlws.WarehouseItemMovementListComponent.columnPriceTotalGrossVisible";

@Component({
    selector: 'mlws-warehouse-item-movement-list',
    imports: [
        CommonModule, MatButtonModule, MatPaginatorModule, MatTableModule, FormsModule, MatFormFieldModule, MatInputModule,
        MatProgressSpinnerModule, MatTimepickerModule, MatDatepickerModule, MatIconModule, MatCheckbox, ReactiveFormsModule,
        ServiceModule, RestModule, TimestampPipe, MatOption, MatSelect],
    templateUrl: './warehouse-item-movement-list.component.html',
    styleUrls: ['./warehouse-item-movement-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class WarehouseItemMovementListComponent {

    @Input()
    public warehouseItemId: number | undefined;

    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
    protected readonly warehouseItemMovementRestService = inject(WarehouseItemMovementRestService);
    protected readonly supplierRestService = inject(SupplierRestService);
    protected warehouses: Warehouse[] = [];
    protected readonly movementTypes: (WarehouseItemMovementType|undefined)[];
    protected readonly suppliers$ = new BehaviorSubject<(Supplier|undefined)[]>([]);

    protected readonly appUrl: string;

    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    protected readonly warehouseItemMovementPage$ = new BehaviorSubject<WarehouseItemMovementPage | undefined>(undefined);
    protected readonly columnKeys$ = new BehaviorSubject<string[]>([]);
    protected readonly pageIndex$: BehaviorSubject<number>;
    protected readonly pageSize$: BehaviorSubject<number>;

    protected readonly filterVisible$: BehaviorSubject<boolean>;
    protected readonly filterSku$: BehaviorSubject<string | undefined>;
    protected readonly filterProductName$: BehaviorSubject<string | undefined>;
    protected readonly filterType$: BehaviorSubject<WarehouseItemMovementType | undefined>;
    protected readonly filterSupplier$: BehaviorSubject<Supplier | undefined>;
    protected readonly filterCreatedFrom$: BehaviorSubject<DateTime | undefined>;
    protected readonly filterCreatedTo$: BehaviorSubject<DateTime | undefined>;

    protected readonly columnSkuVisible$: BehaviorSubject<boolean>;
    protected readonly columnProductsVisible$: BehaviorSubject<boolean>;
    protected readonly columnFromOrToVisible$: BehaviorSubject<boolean>;
    protected readonly columnPriceSingleNetVisible$: BehaviorSubject<boolean>;
    protected readonly columnPriceSingleGrossVisible$: BehaviorSubject<boolean>;
    protected readonly columnPriceTotalNetVisible$: BehaviorSubject<boolean>;
    protected readonly columnPriceTotalGrossVisible$: BehaviorSubject<boolean>;

    protected get filterButtonClass(): string {
        return this.filterVisible$.getValue() ? 'filter-button-checked' : 'filter-button-unchecked';
    }
    protected filterButtonClicked(): void {
        this.filterVisible$.next(!this.filterVisible$.getValue());
    }

    public constructor() {
        this.appUrl = getAppUrl();
        this.pageIndex$ = createNumberPropertyDefined(this, PAGE_INDEX, 0);
        this.pageSize$ = createNumberPropertyDefined(this, PAGE_SIZE, 10);
        this.filterVisible$ = createBooleanPropertyDefined(this, FILTER_VISIBLE, true);
        this.filterSku$ = createStringPropertyUndefined(this, FILTER_SKU, undefined);
        this.filterType$ = this.initFilterType();
        this.filterSupplier$ = this.initFilterSupplier();
        this.filterProductName$ = createStringPropertyUndefined(this, FILTER_PRODUCT_NAME, undefined);
        this.filterCreatedFrom$ = createDateTimePropertyUndefined(this, FILTER_CREATED_FROM, undefined);
        this.filterCreatedTo$ = createDateTimePropertyUndefined(this, FILTER_CREATED_TO, undefined);
        this.columnSkuVisible$ = createBooleanPropertyDefined(this, COLUMN_SKU_VISIBLE, true);
        this.columnProductsVisible$ = createBooleanPropertyDefined(this, COLUMN_PRODUCTS_VISIBLE, true);
        this.columnFromOrToVisible$ = createBooleanPropertyDefined(this, COLUMN_FROM_OR_TO_VISIBLE, true);
        this.columnPriceSingleNetVisible$ = createBooleanPropertyDefined(this, COLUMN_PRICE_SINGLE_NET_VISIBLE, false);
        this.columnPriceSingleGrossVisible$ = createBooleanPropertyDefined(this, COLUMN_PRICE_SINGLE_GROSS_VISIBLE, false);
        this.columnPriceTotalNetVisible$ = createBooleanPropertyDefined(this, COLUMN_PRICE_TOTAL_NET_VISIBLE, true);
        this.columnPriceTotalGrossVisible$ = createBooleanPropertyDefined(this, COLUMN_PRICE_TOTAL_GROSS_VISIBLE, true);

        this.warehouseSelectorService.getWarehouses$().pipe(untilDestroyed(this)).subscribe(warehouses => this.warehouses = warehouses);

        this.movementTypes = [undefined, ... Object.keys(WarehouseItemMovementType) //
            .map(k => WarehouseItemMovementType[k as keyof typeof WarehouseItemMovementType]) //
            .sort()];

        this.initColumnVisible();
        this.initLoadData();

        this.loadSuppliers().pipe(untilDestroyed(this)).subscribe(suppliers => {
            this.suppliers$.next(suppliers);
        });
    }

    protected loadSuppliers(): Observable<(Supplier|undefined)[]> {
        return this._loadSuppliers([undefined], 1);
    }

    protected _loadSuppliers(suppliersCollected: (Supplier|undefined)[], pageNumber: number): Observable<(Supplier|undefined)[]> {
        return this.supplierRestService.getSupplierPage({pageNumber, pageSize: 500, fetch: 'party'}).pipe(
            untilDestroyed(this), take(1),
            concatMap(supplierPage => {
                const nextPageNumber = pageNumber + 1;
                if (nextPageNumber > (supplierPage.lastPageNumber??0)) {
                    return of([...suppliersCollected, ...supplierPage.items]);
                } else {
                    return this._loadSuppliers([...suppliersCollected, ...supplierPage.items], nextPageNumber);
                }
            })
        );
    }

    protected initFilterType(): BehaviorSubject<WarehouseItemMovementType | undefined> {
        const s = localStorage.getItem(FILTER_TYPE)
        const filterType = s
            ? WarehouseItemMovementType[s as keyof typeof WarehouseItemMovementType]
            : undefined;
        const movementType$ = new BehaviorSubject<WarehouseItemMovementType | undefined>(filterType);
        movementType$.pipe(untilDestroyed(this))
            .subscribe(mt=> localStorage.setItem(FILTER_TYPE, mt ? "" + mt : ""));
        return movementType$;
    }

    protected initFilterSupplier(): BehaviorSubject<Supplier|undefined> {
        const s = localStorage.getItem(FILTER_SUPPLIER_ID);
        const supplierId = getValidFiniteNumber(s);

        const filterSupplier$ = new BehaviorSubject<Supplier | undefined>(undefined);
        filterSupplier$.pipe(untilDestroyed(this))
            .subscribe(supplier => localStorage.setItem(FILTER_SUPPLIER_ID, supplier ? "" + supplier.id : "" ));

        this.suppliers$.pipe(
            untilDestroyed(this),
            filter(suppliers => suppliers.length > 0),
            take(1)
        ).subscribe(suppliers => {
            const sup = suppliers.find(supplier => supplier?.id === supplierId);
            if (sup) {
                filterSupplier$.next(sup);
            }
        });
        return filterSupplier$;
    }

    protected isSupplierSelectionVisible() {
        return this.filterType$.getValue() === WarehouseItemMovementType.SUPPLY;
    }

    protected initColumnVisible(): void {
        combineLatest([
            this.columnSkuVisible$,
            this.columnProductsVisible$,
            this.columnFromOrToVisible$,
            this.columnPriceSingleNetVisible$,
            this.columnPriceSingleGrossVisible$,
            this.columnPriceTotalNetVisible$,
            this.columnPriceTotalGrossVisible$
        ]).pipe(untilDestroyed(this))
            .subscribe(([
                columnSkuVisible,
                columnProductsVisible,
                columnFromOrToVisible,
                columnPriceSingleNetVisible,
                columnPriceSingleGrossVisible,
                columnPriceTotalNetVisible,
                columnPriceTotalGrossVisible
            ]) => {
                const columnKeys: string[] = ['created'];
                if (columnSkuVisible) {
                    columnKeys.push('sku');
                }
                if (columnProductsVisible) {
                    columnKeys.push('products');
                }
                columnKeys.push('quantity', 'type');
                if (columnFromOrToVisible) {
                    columnKeys.push('fromOrTo');
                }
                if (columnPriceSingleNetVisible) {
                    columnKeys.push('priceSingleNet');
                }
                if (columnPriceSingleGrossVisible) {
                    columnKeys.push('priceSingleGross');
                }
                if (columnPriceTotalNetVisible) {
                    columnKeys.push('priceTotalNet');
                }
                if (columnPriceTotalGrossVisible) {
                    columnKeys.push('priceTotalGross');
                }
                this.columnKeys$.next(columnKeys);
            });
    }

    protected initLoadData(): void {
        combineLatest([
            this.warehouseSelectorService.getSelectedWarehouse$(),
            this.suppliers$,
            this.filterSku$,
            this.filterProductName$,
            this.filterType$,
            this.filterSupplier$,
            this.filterCreatedFrom$,
            this.filterCreatedTo$,
            this.pageIndex$,
            this.pageSize$
        ]).pipe(
            untilDestroyed(this),
            tap(() => this.loading$.next(true)),
            filter(() => WarehouseItemMovementType.SUPPLY !== this.filterType$.getValue() || this.suppliers$.getValue().length > 0),
            debounceTime(500),
            concatMap(
                ([
                     warehouse, suppliers, filterSku, filterProductName, filterType, filterSupplier,
                     filterCreatedFrom, filterCreatedTo, pageIndex, pageSize
                 ]) =>
                    this.warehouseItemMovementRestService.getWarehouseItemMovementPage({
                    filterWarehouseItemIds: this.warehouseItemId != undefined ? [this.warehouseItemId] : undefined,
                    filterWarehouseId: warehouse.id!,
                    filterSku: filterSku ? `/${filterSku.replace("*", ".*")}.*/i` : undefined,
                    filterProductName: filterProductName ? `/.*${filterProductName.replace("*", ".*")}.*/i` : undefined,
                    filterType,
                    filterSupplierId: filterSupplier?.id,
                    filterCreatedFromIncl: filterCreatedFrom?.toISO() ?? undefined,
                    filterCreatedToExcl: filterCreatedTo?.toISO() ?? undefined,
                    pageNumber: pageIndex + 1,
                    pageSize: pageSize,
                    sort: "created:desc,sku:asc",
                    fetch: "products,supplier.party"
                })
            )
        ).subscribe(warehouseItemMovementPage => {
            this.loading$.next(false);
            this.warehouseItemMovementPage$.next(warehouseItemMovementPage);
            if (this.pageIndex$.getValue() + 1 > warehouseItemMovementPage.lastPageNumber!) {
                this.pageIndex$.next(warehouseItemMovementPage.lastPageNumber! - 1);
            }
        });
    }

    protected getProductsLabel(warehouseItemMovement: WarehouseItemMovement): string {
        if (!warehouseItemMovement.products || warehouseItemMovement.products.length === 0) {
            return '';
        }
        let result: string = getL10n(warehouseItemMovement.products[0].productName) ?? "";
        if (warehouseItemMovement.products.length > 1) {
            result += ` (+ ${warehouseItemMovement.products.length - 1} more)`;
        }
        return result;
    }

    protected getFromOrToLabel(warehouseItemMovement: WarehouseItemMovement): string {
        if (WarehouseItemMovementType.SUPPLY === warehouseItemMovement.type) {
            if (warehouseItemMovement.supplier?.party) {
                return warehouseItemMovement.supplier.party.name??'';
            }
        }
        if (WarehouseItemMovementType.TRANSFER === warehouseItemMovement.type) {
            if (warehouseItemMovement.otherWarehouseId) {
                const warehouse = this.warehouses.find(wh => wh.id === warehouseItemMovement.otherWarehouseId);
                return this.warehouseSelectorService.getWarehouseLabel(warehouse);
            }
        }
        return '';
    }

    protected onPageEvent(pageEvent: PageEvent): void {
        this.pageIndex$.next(pageEvent.pageIndex);
        this.pageSize$.next(pageEvent.pageSize);
    }

    protected warehouseItemMovementTrackBy(index: number, warehouseItemMovement: WarehouseItemMovement): number {
        return warehouseItemMovement.id!;
    }

    protected onClick(row: WarehouseItemMovement): void {
        // nothing to do yet
    }

    protected readonly DateTime = DateTime;
}
