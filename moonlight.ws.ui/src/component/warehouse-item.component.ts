import { ChangeDetectionStrategy, Component, inject, Input, LOCALE_ID, numberAttribute, OnChanges, OnInit, signal, SimpleChanges, ViewEncapsulation } from '@angular/core';
import { CommonModule, formatNumber } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BehaviorSubject, combineLatest, concatMap, from, interval, map, Observable, of, tap } from 'rxjs';
import swal from 'sweetalert';
import { WarehouseItem, WarehouseItemPage } from '../rest/model/warehouse-item';
import { WarehouseItemRestService } from '../rest/warehouse-item-rest.service';
import { RestModule } from '../rest/rest.module';
import { isValidFiniteNumber } from '../util/number.util';
import { SkuRestService } from '../rest/sku-rest.service';
import { getL10n } from '../util/i18n.util';
import { Sku } from '../rest/model/sku';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
import { Warehouse } from '../rest/model/warehouse';
import { WarehouseItemMovementRestService } from '../rest/warehouse-item-movement-rest.service';
import { WarehouseItemMovement, WarehouseItemMovementType } from '../rest/model/warehouse-item-movement';
import { ServiceModule } from '../service/service.module';
import { WarehouseItemMovementGroupRestService } from '../rest/warehouse-item-movement-group-rest.service';
import { WarehouseItemMovementGroup } from '../rest/model/warehouse-item-movement-group';
import { Supplier } from '../rest/model/supplier';
import { SupplierRestService } from '../rest/supplier-rest.service';
import { CreateSupplierOverlayComponent } from './create-supplier-overlay.component';
import { EditPriceComponent } from './edit-price.component';
import { negatePriceIfNeeded, Price } from '../rest/model/price';

const MOVEMENT_TYPE = "mlws.WarehouseItemComponent.movementType";

const WAREHOUSE_ERC = "WAREHOUSE_ERC";
const OTHER_WAREHOUSE_ERC = "mlws.WarehouseItemComponent.otherWarehouse[WAREHOUSE_ERC].erc";

@Component({
    selector: 'mlws-warehouse-item',
    imports: [
        CommonModule, FormsModule, MatFormFieldModule, MatInputModule,
        ReactiveFormsModule, ServiceModule, RestModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
        MatSelectModule, CreateSupplierOverlayComponent, EditPriceComponent
    ],
    templateUrl: './warehouse-item.component.html',
    styleUrls: ['./warehouse-item.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class WarehouseItemComponent implements OnChanges, OnInit {

    protected static readonly NEW_SUPPLIER: Supplier = {
        id: -1,
        party: {
            code: 'NEW',
            name: '>> new <<'
        }
    };

    protected readonly localeId = inject(LOCALE_ID);
    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
    protected readonly warehouseItemRestService = inject(WarehouseItemRestService);
    protected readonly skuRestService = inject(SkuRestService);
    protected readonly warehouseItemMovementGroupRestService = inject(WarehouseItemMovementGroupRestService);
    protected readonly warehouseItemMovementRestService = inject(WarehouseItemMovementRestService);
    protected readonly supplierRestService = inject(SupplierRestService);
    protected readonly quantityFormControl = new FormControl<number|null>(null);

    @Input({transform: numberAttribute})
    public warehouseItemId: number | undefined;

    protected readonly warehouseItem$ = new BehaviorSubject<WarehouseItem | undefined>(undefined);
    protected readonly skus$ = new BehaviorSubject<Sku[]>([]);
    protected readonly quantity$ = new BehaviorSubject<number>(0);
    protected readonly warehouse$ = new BehaviorSubject<Warehouse | undefined>(undefined);
    protected readonly warehouseItemMovements$ = new BehaviorSubject<WarehouseItemMovement[]>([]);
    protected readonly unbookedQuantity$ = new BehaviorSubject<number>(0);
    protected readonly loading$ = new BehaviorSubject<boolean>(false);
    protected readonly movementTypes: WarehouseItemMovementType[];
    protected readonly movementType$: BehaviorSubject<WarehouseItemMovementType>;
    protected readonly otherWarehouses$ = new BehaviorSubject<Warehouse[]>([]);
    protected readonly otherWarehouse$ = new BehaviorSubject<Warehouse | undefined>(undefined);
    protected readonly suppliers$ = new BehaviorSubject<Supplier[]>([]);
    protected readonly supplier$ = new BehaviorSubject<Supplier | undefined>(undefined);
    protected price = signal<Price|undefined>(undefined);

    protected readonly addToStockDisabled$: Observable<boolean>;
    protected readonly removeFromStockDisabled$: Observable<boolean>;
    protected readonly isCreateSupplierOverlayVisible$: Observable<boolean>;

    protected supplierCreated(supplierCreated: Supplier | undefined) {
        this.supplier$.next(supplierCreated);
        if (supplierCreated) {
            this.supplierRestService.getSupplier(supplierCreated.id!, { fetch: "party" }).subscribe(supplierReloaded => {
                const suppliers = this.suppliers$.getValue();
                this.suppliers$.next([...suppliers, supplierReloaded]);
                this.supplier$.next(supplierReloaded);
            })
        }
    }

    protected get movementType(): WarehouseItemMovementType {
        return this.movementType$.getValue();
    }
    protected set movementType(movementType: WarehouseItemMovementType) {
        this.movementType$.next(movementType);
    }
    protected get otherWarehouse(): Warehouse | undefined {
        return this.otherWarehouse$.getValue();
    }
    protected set otherWarehouse(otherWarehouse: Warehouse | undefined) {
        this.otherWarehouse$.next(otherWarehouse);
    }
    protected get supplier(): Supplier | undefined {
        return this.supplier$.getValue();
    }
    protected set supplier(supplier: Supplier | undefined) {
        this.supplier$.next(supplier);
    }
    private loadingCount = 0;

    constructor() {
        this.movementTypes = Object.keys(WarehouseItemMovementType) //
            .map(k => WarehouseItemMovementType[k as keyof typeof WarehouseItemMovementType]) //
            .sort();
        this.movementType$ = this.initMovementType();
        this.addToStockDisabled$ = combineLatest([ //
            this.warehouseItem$, this.quantity$, this.movementType$, this.otherWarehouse$, this.supplier$ //
        ]).pipe( //
            untilDestroyed(this), //
            map(([warehouseItem, quantity, movementType, otherWarehouse, supplier]) =>
                !warehouseItem //
                || quantity <= 0 //
                || (movementType === WarehouseItemMovementType.TRANSFER && !otherWarehouse) //
                || (movementType === WarehouseItemMovementType.SUPPLY && ((supplier?.id??0) <= 0))
            ) //
        );
        this.removeFromStockDisabled$ = this.addToStockDisabled$;

        this.isCreateSupplierOverlayVisible$ = combineLatest([ //
            this.movementType$, this.supplier$ //
        ]).pipe( //
            untilDestroyed(this), //
            map(([movementType, supplier]) => this.isSupplierSelectionVisible() && ((supplier?.id??0) < 0))
        );
    }

    public ngOnInit(): void {
        interval(10000)
            .pipe(untilDestroyed(this))
            .subscribe(() => this.load(true));

        this.quantityFormControl.valueChanges
            .pipe(untilDestroyed(this))
            .subscribe((value) => {
                if (value !== null && value < 0) {
                    value = value * -1;
                    this.quantityFormControl.setValue(value);
                }
                this.quantity$.next(value === null ? 0 : value);
            });

        this.warehouseItem$.pipe(untilDestroyed(this)).subscribe(warehouseItem => {
            if (!warehouseItem) {
                this.skus$.next([]);
                return;
            }
            this.skuRestService.getSkuPage({filterSku: warehouseItem.sku, pageSize: 500}).pipe(untilDestroyed(this))
                .subscribe(page => {
                    if (!page.items || page.items.length === 0) {
                        this.skus$.next([]);
                        return;
                    }
                    this.skus$.next(page.items);
                });
            this.warehouseSelectorService.getWarehouses$().pipe(untilDestroyed(this)).subscribe(warehouses => {
                this.warehouse$.next(warehouses.find(warehouse => warehouseItem.warehouseId === warehouse.id));
            });
        });

        this.warehouseItemMovements$.pipe(untilDestroyed(this)).subscribe(movements => {
            const unbookedQty = movements.reduce((previousQty, warehouseItemMovement) => previousQty + warehouseItemMovement.quantity, 0);
            this.unbookedQuantity$.next(unbookedQty);
        });

        combineLatest([this.warehouseSelectorService.getWarehouses$(), this.warehouse$])
            .pipe(untilDestroyed(this))
            .subscribe(([warehouses, warehouse]) => {
                if (!warehouse) {
                    this.otherWarehouses$.next([]);
                    return;
                }
                const otherWarehouses = warehouses.filter(w => w.id !== warehouse.id);
                this.otherWarehouses$.next(otherWarehouses);

                const localStoreKey = OTHER_WAREHOUSE_ERC.replace(WAREHOUSE_ERC, warehouse.externalReferenceCode!);
                const otherWarehouseErc = localStorage.getItem(localStoreKey);
                if (otherWarehouseErc) {
                    const otherWarehouse = otherWarehouses.find(w => w.externalReferenceCode === otherWarehouseErc);
                    this.otherWarehouse$.next(otherWarehouse);
                }
            });

        this.otherWarehouse$.pipe(untilDestroyed(this)).subscribe(otherWarehouse => {
            const warehouse = this.warehouse$.getValue();
            if (warehouse) {
                const localStoreKey = OTHER_WAREHOUSE_ERC.replace(WAREHOUSE_ERC, warehouse.externalReferenceCode!);
                const otherWarehouseErc = otherWarehouse?.externalReferenceCode;
                if  (otherWarehouseErc) {
                    localStorage.setItem(localStoreKey, otherWarehouseErc);
                } else {
                    localStorage.removeItem(localStoreKey);
                }
            }
        });

        this.loadSuppliers().pipe(untilDestroyed(this)).subscribe(suppliers => {
           this.suppliers$.next(suppliers);
        });
    }

    protected loadSuppliers(): Observable<Supplier[]> {
        return this._loadSuppliers([], 1);
    }

    protected _loadSuppliers(suppliersCollected: Supplier[], pageNumber: number): Observable<Supplier[]> {
        return this.supplierRestService.getSupplierPage({pageNumber, pageSize: 2, fetch: 'party'}).pipe( // TODO pageSize is too small, now -- this is just for testing!
            untilDestroyed(this),
            concatMap(supplierPage => {
                const nextPageNumber = pageNumber + 1;
                if (nextPageNumber > (supplierPage.lastPageNumber??0)) {
                    return of([...suppliersCollected, ...supplierPage.items, WarehouseItemComponent.NEW_SUPPLIER]);
                } else {
                    return this._loadSuppliers([...suppliersCollected, ...supplierPage.items], nextPageNumber);
                }
            })
        );
    }

    protected initMovementType(): BehaviorSubject<WarehouseItemMovementType> {
        const s = localStorage.getItem(MOVEMENT_TYPE)
        const movementType = s
            ? WarehouseItemMovementType[s as keyof typeof WarehouseItemMovementType]
            : WarehouseItemMovementType.INVENTORY;
        const movementType$ = new BehaviorSubject<WarehouseItemMovementType>(movementType);
        movementType$.pipe(untilDestroyed(this))
            .subscribe(pageSize => localStorage.setItem(MOVEMENT_TYPE, "" + pageSize));
        return movementType$;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes['warehouseItemId']) {
            this.load();
        }
    }

    protected load(silent = false): void {
        if (isValidFiniteNumber(this.warehouseItemId)) {
            if (!silent) this.loadingCountInc();
            this.warehouseItemRestService.getWarehouseItem(this.warehouseItemId!).subscribe(warehouseItem => {
                if (!silent) this.loadingCountDec();
                this.warehouseItem$.next(warehouseItem);
            });

            if (!silent) this.loadingCountInc();
            this.warehouseItemMovementRestService.getWarehouseItemMovementPage({
                filterWarehouseItemId: this.warehouseItemId!,
                filterBooked: false,
                pageSize: 500
            }).subscribe(page => {
                if (!silent) this.loadingCountDec();
                this.warehouseItemMovements$.next(page.items ?? []);
            });
        } else {
            this.warehouseItem$.next(undefined);
        }
    }

    protected onAddToStockClick(): void {
        const saveParam = new SaveParam(
            this.localeId,
            this.warehouseItem$.getValue()!, // button disabled, if undefined => defined for sure
            this.movementType,
            this.quantity$.getValue(),
            this.getOtherWarehouseId(),
            this.getSupplierId(),
            this.getPrice()
        );
        this.showQuestionDialogAndAddToOrRemoveFromStock(saveParam).subscribe();
    }

    protected onRemoveFromStockClick(): void {
        const saveParam = new SaveParam(
            this.localeId,
            this.warehouseItem$.getValue()!, // button disabled, if undefined => defined for sure
            this.movementType,
            -1 * this.quantity$.getValue(),
            this.getOtherWarehouseId(),
            this.getSupplierId(),
            this.getPrice()
        );
        this.showQuestionDialogAndAddToOrRemoveFromStock(saveParam).subscribe();
    }

    protected showQuestionDialogAndAddToOrRemoveFromStock(saveParam: SaveParam): Observable<boolean> {
        const element = document.createElement('div');
        element.innerHTML = this.getAddToOrRemoveFromStockQuestionMessage(saveParam);

        return from(swal({
            title: 'Move warehouse-items?',
            // text: this.getAddToOrRemoveFromStockQuestionMessage(saveParam), // does not support HTML-tags
            content: { element },
            icon: 'assets/dialog/question.svg',
            buttons: ['No', 'Yes']
        })).pipe(
            concatMap((dialogResult: any) => {
                if (dialogResult) {
                    this.loadingCountInc();
                    this.quantityFormControl.setValue(null);
                    this.price.set(undefined);
                    return this.save(saveParam).pipe(map(() => {
                        this.load();
                        this.loadingCountDec();
                        return true;
                    }));
                }
                return of(false);
            })
        );
    }

    protected getAddToOrRemoveFromStockQuestionMessage(param: SaveParam): string {
        const warehouse = this.warehouse$.getValue();
        if (warehouse?.id !== param.warehouseItem.warehouseId) {
            throw new Error('warehouse.id does not match param.warehouseItem.warehouseId!');
        }
        const otherWarehouse = param.otherWarehouseId === undefined ? undefined : this.otherWarehouse$.getValue();
        if  (otherWarehouse?.id !== param.otherWarehouseId) {
            throw new Error('otherWarehouse.id does not match param.otherWarehouseId!');
        }
        if (!otherWarehouse) {
            return param.quantity > 0
                ? `<b>Add ${param.quantityAbsWithUom}</b> of <b>${param.warehouseItem?.sku}</b> to stock in <b>${this.warehouseSelectorService.getWarehouseLabel(warehouse)}</b>?`
                : `<b>Remove ${param.quantityAbsWithUom}</b> of <b>${param.warehouseItem?.sku}</b> from stock in <b>${this.warehouseSelectorService.getWarehouseLabel(warehouse)}</b>?`;
        } else {
            return param.quantity > 0
                ? `<b>Add ${param.quantityAbsWithUom}</b> of <b>${param.warehouseItem?.sku}</b> to stock in <b>${this.warehouseSelectorService.getWarehouseLabel(warehouse)}</b> <br/>and<br/> <b>remove</b> these goods from stock in <b>${this.warehouseSelectorService.getWarehouseLabel(otherWarehouse)}</b>?`
                : `<b>Remove ${param.quantityAbsWithUom}</b> of <b>${param.warehouseItem?.sku}</b> from stock in <b>${this.warehouseSelectorService.getWarehouseLabel(warehouse)}</b> <br/>and<br/> <b>add</b> these goods to stock in <b>${this.warehouseSelectorService.getWarehouseLabel(otherWarehouse)}</b>?`;
        }
    }

    protected isOtherWarehouseSelectionVisible() {
        return this.movementType === WarehouseItemMovementType.TRANSFER;
    }

    protected getOtherWarehouseId(): number|undefined {
        if (this.isOtherWarehouseSelectionVisible()) {
            const id = this.otherWarehouse$.getValue()?.id;
            if (id === undefined) {
                throw new Error("movementType is TRANSFER, but otherWarehouseId is undefined!");
            }
            return id;
        }
        return undefined;
    }

    protected getSupplierId(): number|undefined {
        if (this.isSupplierSelectionVisible()) {
            const id = this.supplier$.getValue()?.id;
            if (id === undefined) {
                throw new Error("movementType is SUPPLY, but supplierId is undefined!");
            }
            return id;
        }
        return undefined;
    }

    protected isSupplierSelectionVisible() {
        return this.movementType === WarehouseItemMovementType.SUPPLY;
    }

    protected isPriceInputVisible() {
        return this.isSupplierSelectionVisible();
    }

    protected save(param: SaveParam): Observable<SaveParam> {
        return this._save_getWarehouseItemPageForOtherWarehouseId(param).pipe(
            concatMap((page: WarehouseItemPage | undefined) => this._save_createOtherWarehouseItemIfNeeded(param, page)),
            tap((otherItem: WarehouseItem | undefined) => param.otherWarehouseItem = otherItem),
            concatMap(() => this._save_createGroupIfNeeded(param)),
            tap((group: WarehouseItemMovementGroup | undefined) => param.group = group),
            concatMap(() => this._save_createMovement(param)),
            tap((movement: WarehouseItemMovement) => param.movement = movement),
            concatMap(() => this._save_createOtherMovementIfNeeded(param)),
            tap((otherMovement: WarehouseItemMovement | undefined) => param.otherMovement = otherMovement),
            concatMap(() => this._save_finalizeGroupIfNeeded(param)),
            tap((group: WarehouseItemMovementGroup | undefined) => param.group = group),
            map(() => param)
        ) as Observable<SaveParam>;
    }

    protected _save_getWarehouseItemPageForOtherWarehouseId(param: SaveParam): Observable<WarehouseItemPage | undefined> {
        if (param.otherWarehouseId === undefined) {
            return of(undefined);
        }
        return this.warehouseItemRestService.getWarehouseItemPage({
            filterWarehouseId: param.otherWarehouseId, filterSku: param.sku
        });
    }

    protected _save_createOtherWarehouseItemIfNeeded(param: SaveParam, page: WarehouseItemPage | undefined): Observable<WarehouseItem | undefined> {
        if (!page) {
            if (param.otherWarehouseId !== undefined) {
                throw new Error("otherWarehouseId is defined, but page is undefined!");
            }
            return of(undefined) as Observable<WarehouseItem | undefined>;
        }
        return page.items.length > 0 ? of(page.items[0]) : this.warehouseItemRestService.postWarehouseItem({
            warehouseId: param.otherWarehouseId, sku: param.sku
        });
    }

    protected _save_createGroupIfNeeded(param: SaveParam): Observable<WarehouseItemMovementGroup | undefined> {
        if (!param.otherWarehouseItem) {
            if (param.otherWarehouseId !== undefined) {
                throw new Error("otherWarehouseId is defined, but otherWarehouseItem is undefined!");
            }
            return of(undefined);
        }
        return this.warehouseItemMovementGroupRestService.postWarehouseItemMovementGroup({});
    }

    protected _save_createMovement(param: SaveParam): Observable<WarehouseItemMovement> {
        return this.warehouseItemMovementRestService.postWarehouseItemMovement({
            warehouseItemId: param.warehouseItem.id,
            quantity: param.quantity,
            type: param.type,
            groupId: param.group?.id,
            otherWarehouseId: param.otherWarehouseId,
            otherWarehouseItemId: param.otherWarehouseItem?.id,
            supplierId: param.supplierId,
            price: negatePriceIfNeeded(param.quantity, param.price)
        });
    }

    protected _save_createOtherMovementIfNeeded(param: SaveParam): Observable<WarehouseItemMovement | undefined> {
        if (!param.otherWarehouseItem || !param.group) {
            if (param.otherWarehouseId !== undefined) {
                throw new Error("otherWarehouseId is defined, but group is undefined!");
            }
            return of(undefined);
        }
        return this.warehouseItemMovementRestService.postWarehouseItemMovement({
            warehouseItemId: param.otherWarehouseItem.id,
            quantity: -1 * param.quantity,
            type: param.type,
            groupId: param.group?.id,
            otherWarehouseId: param.warehouseItem.warehouseId,
            otherWarehouseItemId: param.warehouseItem.id,
            supplierId: param.supplierId,
            price: negatePriceIfNeeded(param.quantity, param.price)
        });
    }

    protected getPrice(): Price | undefined {
        return this.isPriceInputVisible() ? this.price() : undefined;
    }

    protected _save_finalizeGroupIfNeeded(param: SaveParam): Observable<WarehouseItemMovementGroup | undefined> {
        if (!param.group) {
            return of(undefined);
        }
        return this.warehouseItemMovementGroupRestService.finalizeWarehouseItemMovementGroup(param.group.id!);
    }

    protected readonly getL10n = getL10n;

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
}

class SaveParam {
    public quantityAbsWithUom: string;
    public sku: string;
    public otherWarehouseItem?: WarehouseItem;
    public group?: WarehouseItemMovementGroup;
    public movement?: WarehouseItemMovement;
    public otherMovement?: WarehouseItemMovement;

    public constructor(
        protected readonly localeId: string,
        public readonly warehouseItem: WarehouseItem,
        public readonly type: WarehouseItemMovementType,
        public readonly quantity: number,
        public readonly otherWarehouseId: number | undefined,
        public readonly supplierId: number | undefined,
        public readonly price: Price | undefined
    ) {
        this.quantityAbsWithUom = warehouseItem?.unitOfMeasureKey ? "" + formatNumber(Math.abs(quantity), this.localeId, '1.0-3') + " " + warehouseItem.unitOfMeasureKey : "" + Math.abs(quantity);
        this.sku = warehouseItem.sku!;
    }
}