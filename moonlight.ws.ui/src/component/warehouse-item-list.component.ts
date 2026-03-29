import {ChangeDetectionStrategy, Component, inject, OnInit, ViewEncapsulation} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {Router} from '@angular/router';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {BehaviorSubject, combineLatest, concatMap, debounceTime, filter, map, Observable, of, tap} from 'rxjs';
import {RestModule} from '../rest/rest.module';
import {WarehouseSelectorService} from '../service/warehouse-selector.service';
import {ServiceModule} from '../service/service.module';
import {WarehouseItemRestService} from '../rest/warehouse-item-rest.service';
import {WarehouseItem, WarehouseItemPage} from '../rest/model/warehouse-item';
import {SkuRestService} from '../rest/sku-rest.service';
import {getL10n} from '../util/i18n.util';
import {RestConfig} from '../rest/rest.config';
import {concatUrlSegments} from '../util/url.util';
import {KeycloakProvider} from '../rest/keycloak.provider';
import {createNumberPropertyDefined, createStringPropertyUndefined} from '../util/component.util';

const PAGE_INDEX = "mlws.WarehouseItemListComponent.pageIndex";
const PAGE_SIZE = "mlws.WarehouseItemListComponent.pageSize";
const FILTER_SKU = "mlws.WarehouseItemListComponent.filterSku";

@Component({
	selector: 'mlws-warehouse-item-list',
	imports: [
		CommonModule, MatButtonModule, MatPaginatorModule, MatTableModule, FormsModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, ServiceModule, RestModule,
		MatProgressSpinnerModule, MatIconModule
	],
	templateUrl: './warehouse-item-list.component.html',
	styleUrls: ['./warehouse-item-list.component.scss'],
	encapsulation: ViewEncapsulation.None,
	changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class WarehouseItemListComponent implements OnInit {

	protected readonly keycloakProvider = inject(KeycloakProvider);
	protected readonly restConfig = inject(RestConfig);
	protected readonly router = inject(Router);

	protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
	protected readonly warehouseItemRestService = inject(WarehouseItemRestService);
	protected readonly skuRestService = inject(SkuRestService);

	protected readonly loading$ = new BehaviorSubject<boolean>(false);
	protected readonly columnKeys = ["sku", "products", "quantity"]; //, "reservedQuantity", "unitOfMeasureKey"];
	protected readonly warehouseItemPage$ = new BehaviorSubject<WarehouseItemPage | undefined>(undefined);
	protected readonly pageIndex$ : BehaviorSubject<number>;
	protected readonly pageSize$: BehaviorSubject<number>;
	protected readonly filterSku$: BehaviorSubject<string | undefined>;
	protected readonly printLabelUrl$: Observable<string>;

	protected get filterSku(): string|undefined {
		return this.filterSku$.getValue();
	}
	protected set filterSku(val: string|undefined) {
		this.filterSku$.next(val);
	}

	public constructor() {
		this.pageIndex$ = createNumberPropertyDefined(this, PAGE_INDEX, 0);
		this.pageSize$ = createNumberPropertyDefined(this, PAGE_SIZE, 10);
		this.filterSku$ = createStringPropertyUndefined(this, FILTER_SKU, undefined);

		this.printLabelUrl$ = combineLatest({
			bearerTokenSha256: this.keycloakProvider.bearerTokenSha256$,
			warehouseItemPage: this.warehouseItemPage$
		}).pipe(
			untilDestroyed(this),
			filter(v => !!v.bearerTokenSha256 && !!v.warehouseItemPage),
			map(v => {
				if (!v.warehouseItemPage?.items?.length) {
					return "";
				}
				const idString = v.warehouseItemPage!.items.map(wi => wi.id).join(",");
				const redirectUrlEncoded = encodeURIComponent("warehouse-item-label?warehouseItemIds=" + idString);
				return concatUrlSegments(this.restConfig.restUrl, "auth-and-redirect", v.bearerTokenSha256, redirectUrlEncoded)
			})
		);
	}

	protected onPageEvent(pageEvent: PageEvent): void {
		this.pageIndex$.next(pageEvent.pageIndex);
		this.pageSize$.next(pageEvent.pageSize);
	}

	protected warehouseItemTrackBy(index: number, warehouseItem: WarehouseItem): number {
		return warehouseItem.id!;
	}

	public ngOnInit(): void {
		combineLatest([
			this.warehouseSelectorService.getSelectedWarehouse$(),
			this.filterSku$,
			this.pageIndex$,
			this.pageSize$
		]).pipe(
			untilDestroyed(this),
			tap(() => this.loading$.next(true)),
			debounceTime(500),
			concatMap(([warehouse, filterSku, pageIndex, pageSize]) =>
				this.warehouseItemRestService.getWarehouseItemPage({
					filterWarehouseId: warehouse.id!,
					filterSku: filterSku ? `/${filterSku.replace("*", ".*")}.*/i` : undefined,
					sort: 'sku:asc',
					pageNumber: pageIndex + 1,
					pageSize: pageSize
				})
			)
		).subscribe(warehouseItemPage => {
			this.loading$.next(false);
			this.warehouseItemPage$.next(warehouseItemPage);
			if (this.pageIndex$.getValue() + 1 > warehouseItemPage.lastPageNumber!) {
				this.pageIndex$.next(warehouseItemPage.lastPageNumber! - 1);
			}
		});
	}

	protected getProductsLabel$(warehouseItem: WarehouseItem): Observable<string> {
		if (!warehouseItem.sku) {
			return of("");
		}
		let productsLabel$: BehaviorSubject<string> | undefined = (warehouseItem as any)._productsLabel;
		if (!productsLabel$) {
			productsLabel$ = new BehaviorSubject<string>("(loading)");
			(warehouseItem as any)._productsLabel = productsLabel$;

			this.skuRestService.getSkuPage({ filterSku: warehouseItem.sku }).pipe(untilDestroyed(this))
				.subscribe(page => {
					if (!page.items || page.items.length === 0) {
						productsLabel$?.next("");
						return;
					}
					let result: string = getL10n(page.items[0].productName) ?? "";
					if (page.totalSize > 1) {
						result += ` (+ ${page.totalSize - 1} more)`;
					}
					productsLabel$?.next(result);
				});
		}
		return productsLabel$;
	}

	protected onClick(row: WarehouseItem): void {
		this.router.navigate(["warehouse-item"], { queryParams: { id: row.id } });
	}
}