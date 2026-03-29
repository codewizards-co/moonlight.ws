import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BehaviorSubject, combineLatest, concatMap, filter, tap } from 'rxjs';
import { WarehouseItemComponent } from '../component/warehouse-item.component';
import { getValidFiniteNumber, isValidFiniteNumber } from '../util/number.util';
import { ID_PARAM } from '../util/shared-const';
import { WarehouseItemRestService } from '../rest/warehouse-item-rest.service';
import { WarehouseItem } from '../rest/model/warehouse-item';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
import { getL10n } from '../util/i18n.util';
import { ServiceModule } from '../service/service.module';

const WAREHOUSE_ERC_PARAM = 'warehouseErc';
const SKU_PARAM = 'sku';

@Component({
    selector: 'mlws-warehouse-item-page', imports: [ServiceModule, WarehouseItemComponent, CommonModule, MatProgressSpinnerModule, MatButton],
    templateUrl: './warehouse-item.page.html',
    styleUrls: ['./warehouse-item.page.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class WarehouseItemPage implements OnInit, OnDestroy {
    private readonly activatedRoute = inject(ActivatedRoute);
    private readonly router = inject(Router);
    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
    private readonly warehouseItemRestService = inject(WarehouseItemRestService);
    protected readonly id$ = new BehaviorSubject<number | undefined>(undefined);
    protected readonly warehouseErc$ = new BehaviorSubject<string | undefined>(undefined);
    protected readonly sku$ = new BehaviorSubject<string | undefined>(undefined);
    private loadingCount = 0;
    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    private warehouseItem: WarehouseItem | undefined;
    protected readonly creating$ = new BehaviorSubject<boolean>(false);

    public ngOnInit(): void {
        console.debug('ngOnInit: entered');
        this.warehouseSelectorService.visible$.next(true);
        this.activatedRoute.queryParams
            .pipe(untilDestroyed(this))
            .subscribe((params: Params) => {
                const id: number | undefined = getValidFiniteNumber(params[ID_PARAM]);
                const warehouseErc: string | undefined = params[WAREHOUSE_ERC_PARAM];
                const sku: string | undefined = params[SKU_PARAM];
                console.debug(`activatedRoute changed: id=${id} warehouseErc=${warehouseErc} sku=${sku} this.warehouseItem.id=${this.warehouseItem?.id}`);
                if (this.warehouseItem && ((id !== undefined && id !== this.warehouseItem.id) || (!!warehouseErc && warehouseErc !== this.warehouseItem.warehouseExternalReferenceCode) || (!!sku && sku !== this.warehouseItem.sku))) {
                    this.warehouseItem = undefined;
                    console.debug('ngOnInit/activatedRoute: this.warehouseItem cleared!');
                }
                this.id$.next(this.warehouseItem?.id ?? id);
                this.warehouseErc$.next(this.warehouseItem?.warehouseExternalReferenceCode ?? warehouseErc);
                this.sku$.next(this.warehouseItem?.sku ?? sku);
            });

        this.id$
            .pipe(untilDestroyed(this), filter((id) => isValidFiniteNumber(id) && id !== this.warehouseItem?.id), //
                tap(() => this.loadingCountInc()), //
                concatMap((id) => {
                    console.debug(`id$ changed: id=${id}: loading warehouseItem...`);
                    return this.warehouseItemRestService.getWarehouseItem(id!);
                }))
            .subscribe((warehouseItem) => {
                this.loadingCountDec();
                this.warehouseItem = warehouseItem;
                this.warehouseErc$.next(warehouseItem.warehouseExternalReferenceCode);
                this.sku$.next(warehouseItem.sku);
            });

        combineLatest([
            this.warehouseErc$.pipe(
                untilDestroyed(this), filter((erc) => !!erc), //
                concatMap((erc) => {
                    console.debug(`warehouseErc$ changed: erc=${erc}: selecting warehouse...`);
                    return this.warehouseSelectorService.selectWarehouseByErc(erc!);
                })
            ), //
            this.sku$
        ])
            .pipe( //
                untilDestroyed(this), //
                filter( //
                    ([warehouse, sku]) => !!warehouse && !!sku //
                    && (warehouse.externalReferenceCode !== this.warehouseItem?.warehouseExternalReferenceCode || sku !== this.warehouseItem?.sku) //
                ), //
                tap(() => this.loadingCountInc()), //
                concatMap(([warehouse, sku]) => this.warehouseItemRestService.getWarehouseItemPage({
                    filterWarehouseId: warehouse.id!, filterSku: sku
                }))
            )
            .subscribe((warehouseItemPage) => {
                this.loadingCountDec();
                if (warehouseItemPage.items.length > 0) {
                    this.warehouseItem = warehouseItemPage.items[0];
                    this.id$.next(warehouseItemPage.items[0].id);
                    this.router.navigate(['warehouse-item'], {
                        queryParams: { id: warehouseItemPage.items[0].id }
                    });
                } else {
                    this.warehouseItem = undefined;
                    this.id$.next(undefined);
                    this.router.navigate(['warehouse-item'], {
                        queryParams: {
                            warehouseErc: this.warehouseErc$.getValue(), sku: this.sku$.getValue()
                        }
                    });
                }
            });

        this.warehouseSelectorService.getSelectedWarehouse$()
            .pipe(untilDestroyed(this), filter(() => this.loadingCount === 0))
            .subscribe((warehouse) => {
                console.debug(`selectedWarehouse$ changed: warehouse.externalReferenceCode=${warehouse.externalReferenceCode}`);
                this.warehouseErc$.next(warehouse.externalReferenceCode)
            });
    }

    public ngOnDestroy(): void {
        console.debug('ngOnDestroy: entered');
        this.warehouseSelectorService.visible$.next(false);
    }

    protected onCreateWarehouseItem(): void {
        const warehouseId = this.warehouseSelectorService.getSelectedWarehouse()?.id;
        const sku = this.sku$.getValue();
        if (!isValidFiniteNumber(warehouseId)) {
            throw new Error('warehouseId illegal!');
        }
        if (!sku) {
            throw new Error('sku illegal!');
        }
        this.creating$.next(true);
        this.warehouseItemRestService.postWarehouseItem({
            warehouseId, sku
        }).pipe(untilDestroyed(this)).subscribe((warehouseItem) => {
            this.warehouseItem = warehouseItem;
            this.id$.next(warehouseItem.id);
            this.router.navigate(['warehouse-item'], {
                queryParams: { id: warehouseItem.id }
            });
        });
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

    protected readonly getL10n = getL10n;
}