import {inject, Injectable} from '@angular/core';
import {BehaviorSubject, concatMap, filter, first, firstValueFrom, Observable, of, take} from 'rxjs';
import {Warehouse} from '../rest/model/warehouse';
import {WarehouseRestService} from '../rest/warehouse-rest.service';
import {getL10n} from "../util/i18n.util";

const MLWS_WAREHOUSE_ERC = "mlws.WarehouseSelectorService.warehouse.erc";

/**
 * Global service for selecting the current warehouse.
 */
@Injectable({
    providedIn: 'root'
})
export class WarehouseSelectorService {

    public readonly visible$ = new BehaviorSubject<boolean>(false);
    public readonly loading$: BehaviorSubject<boolean> = new BehaviorSubject(true);
    private readonly warehouseRestService = inject(WarehouseRestService);
    private readonly warehouse$ = new BehaviorSubject<Warehouse | undefined>(undefined);
    private readonly warehouses$ = new BehaviorSubject<Warehouse[] | undefined>(undefined);

    public constructor() {
        this.loadWarehouses();
    }

    public getWarehouses$(): Observable<Warehouse[]> {
        return this.warehouses$.pipe(
            filter(warehouses => !!warehouses)
        );
    }

    public getSelectedWarehouse$(): Observable<Warehouse> {
        return this.warehouse$.pipe(
            filter(warehouse => !!warehouse)
        );
    }

    public getSelectedWarehouse(): Warehouse | undefined {
        return this.warehouse$.getValue();
    }

    public selectWarehouse(warehouse: Warehouse) {
        if (warehouse?.externalReferenceCode) {
            localStorage.setItem(MLWS_WAREHOUSE_ERC, warehouse.externalReferenceCode);
        }
        this.warehouse$.next(warehouse);
    }

    public selectWarehouseByErc(warehouseErc: string): Promise<Warehouse> {
        return firstValueFrom(this.loading$.pipe(
            first(loading => !loading),
            concatMap(loading => this.getWarehouses$().pipe(take(1))),
            concatMap(warehouses => {
                const selectedWarehouse = this.getSelectedWarehouse();
                if (selectedWarehouse?.externalReferenceCode === warehouseErc) {
                    return of(selectedWarehouse);
                }
                const warehouse = warehouses.find(warehouse => warehouseErc === warehouse.externalReferenceCode);
                if (!warehouse) {
                    throw new Error("No warehouse found with this externalReferenceCode: " + warehouseErc);
                }
                this.selectWarehouse(warehouse);
                return of(warehouse);
            })
        ));
    }

    private loadWarehouses(suppressLoadingIndicator = false, pageNumber = 1, allWarehouses: Warehouse[] = []): void {
        if (pageNumber === 1 && !suppressLoadingIndicator) {
            this.loading$.next(true);
        }
        this.warehouseRestService.getWarehousePage({
            filterActive: true,
            sort: "name:asc,countryISOCode:asc,city:asc",
            pageNumber,
            pageSize: 500
        }).subscribe(page => {
            allWarehouses.push(...page.items);
            if (page.pageNumber < page.lastPageNumber!) {
                this.loadWarehouses(suppressLoadingIndicator, page.pageNumber + 1, allWarehouses);
            } else {
                this.warehouses$.next(allWarehouses);
                if (!suppressLoadingIndicator) {
                    this.loading$.next(false);
                }
                const lastSelectedWarehouseExternalReferenceCode = localStorage.getItem(MLWS_WAREHOUSE_ERC);
                if (lastSelectedWarehouseExternalReferenceCode) {
                    const warehouse = this.warehouses$.getValue()?.find((wh) => lastSelectedWarehouseExternalReferenceCode === wh.externalReferenceCode);
                    if (warehouse && warehouse.id !== this.getSelectedWarehouse()?.id) {
                        this.selectWarehouse(warehouse);
                    }
                }
                if (!this.getSelectedWarehouse()) {
                    this.warehouses$.pipe(take(1)).subscribe(warehouses => this.selectWarehouse(warehouses![0]));
                }
            }
        });
    }

    public getWarehouseLabel(warehouse?: Warehouse): string {
        if (!warehouse) {
            return "";
        }
        return getL10n(warehouse.name) + " (" + warehouse.countryISOCode + ", " + warehouse.city + ")";
    }

    public reload(suppressLoadingIndicator = true): void {
        this.loadWarehouses(suppressLoadingIndicator);
    }
}