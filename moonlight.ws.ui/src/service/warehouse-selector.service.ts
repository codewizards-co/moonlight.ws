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
        this.migrate(); // TODO remove this clean-up again after 2026-04-01
        this.loading$.next(true);
        this.loadWarehouses(1);
    }

	private migrate(): void { // TODO remove this clean-up again after 2026-04-01
		const oldKey = "MLL_WAREHOUSE_ERC";
		const value = localStorage.getItem(oldKey);
		if (value) {
			localStorage.setItem(MLWS_WAREHOUSE_ERC, value);
			localStorage.removeItem(oldKey);
		}
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

    private loadWarehouses(pageNumber: number): void {
        this.warehouseRestService.getWarehousePage({
            filterActive: true,
            sort: "name:asc,countryISOCode:asc,city:asc",
            pageNumber,
            pageSize: 500
        }).subscribe(page => {
            const oldValue = this.warehouses$.getValue();
            if (oldValue) {
                this.warehouses$.next([...oldValue, ...page.items]);
            } else {
                this.warehouses$.next(page.items);
            }
            if (page.pageNumber < page.lastPageNumber!) {
                this.loadWarehouses(page.pageNumber + 1);
            } else {
                this.loading$.next(false);
                const lastSelectedWarehouseExternalReferenceCode = localStorage.getItem(MLWS_WAREHOUSE_ERC);
                if (lastSelectedWarehouseExternalReferenceCode) {
                    const warehouse = this.warehouses$.getValue()?.find((wh) => lastSelectedWarehouseExternalReferenceCode === wh.externalReferenceCode);
                    if (warehouse) {
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
}