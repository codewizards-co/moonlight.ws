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
import { BehaviorSubject, combineLatest, concatMap, debounceTime, tap } from 'rxjs';
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

const PAGE_INDEX = "mlws.WarehouseItemMovementListComponent.pageIndex";
const PAGE_SIZE = "mlws.WarehouseItemMovementListComponent.pageSize";
const FILTER_SKU = "mlws.WarehouseItemMovementListComponent.filterSku";
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
        ServiceModule, RestModule, TimestampPipe],
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
    protected warehouses: Warehouse[] = [];

    protected readonly appUrl: string;

    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    protected readonly warehouseItemMovementPage$ = new BehaviorSubject<WarehouseItemMovementPage | undefined>(undefined);
    protected readonly columnKeys$ = new BehaviorSubject<string[]>([]);
    protected readonly pageIndex$: BehaviorSubject<number>;
    protected readonly pageSize$: BehaviorSubject<number>;

    protected readonly filterVisible$: BehaviorSubject<boolean>;
    protected readonly filterSku$: BehaviorSubject<string | undefined>;
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

        this.initColumnVisible();
        this.initLoadData();
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
            this.filterSku$,
            this.filterCreatedFrom$,
            this.filterCreatedTo$,
            this.pageIndex$,
            this.pageSize$
        ]).pipe(
            untilDestroyed(this),
            tap(() => this.loading$.next(true)),
            debounceTime(500),
            concatMap(([warehouse, filterSku, filterCreatedFrom, filterCreatedTo, pageIndex, pageSize]) =>
                this.warehouseItemMovementRestService.getWarehouseItemMovementPage({
                    filterWarehouseItemId: this.warehouseItemId,
                    filterWarehouseId: warehouse.id!,
                    filterSku: filterSku ? `/${filterSku.replace("*", ".*")}.*/i` : undefined,
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
    }

    protected readonly DateTime = DateTime;
}
