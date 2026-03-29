import { ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output, ViewEncapsulation } from '@angular/core';
import { BehaviorSubject, concatMap, forkJoin, map } from 'rxjs';
import { Warehouse } from '../rest/model/warehouse';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
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
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { Consignee } from '../rest/model/consignee';
import { ConsigneeRestService } from '../rest/consignee-rest.service';

@Component({
    selector: 'mlws-add-consignee-overlay',
    imports: [
        CommonModule, FormsModule, MatFormFieldModule, MatInputModule,
        ReactiveFormsModule, ServiceModule, RestModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
        MatSelectModule
    ],
    templateUrl: './add-consignee-overlay.component.html',
    styleUrls: ['./add-consignee-overlay.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class AddConsigneeOverlayComponent {

    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
    protected readonly consigneeRestService = inject(ConsigneeRestService);

    @Input()
    public partyId: number | null | undefined;
    protected readonly warehouses$ = new BehaviorSubject<Warehouse[]>([]);
    protected readonly warehouse$ = new BehaviorSubject<Warehouse | undefined>(undefined);

    /**
     * If the user aborts (e.g. clicking "Cancel"), this emits undefined. Otherwise, it emits a new Consignee after it was persisted.
     */
    @Output()
    public consigneeAdded$ = new EventEmitter<Consignee|undefined>();

    public constructor() {
        this.warehouseSelectorService.getWarehouses$().pipe( //
            untilDestroyed(this), //
            concatMap((warehouses: Warehouse[]) =>
                forkJoin(warehouses.map(wh => this.consigneeRestService.getConsigneePage({filterWarehouseId: wh.id, pageSize: 0}).pipe(map(consigneePage => consigneePage.totalSize === 0 ? wh : undefined))))
            ),
            map(warehouses => warehouses.filter(wh => !!wh))
        ) //
            .subscribe((warehouses: Warehouse[]) => this.warehouses$.next(warehouses));
    }

    protected onCancelClick(): void {
        this.consigneeAdded$.emit(undefined);
    }

    protected onOkClick(): void {
        if (this.partyId === null || this.partyId === undefined) {
            console.error('partyId is null or undefined!');
            return;
        }
        const warehouseId = this.warehouse$.getValue()?.id;
        if (warehouseId === undefined) {
            console.error('warehouseId is undefined!');
            return;
        }
        this.consigneeAdded$.next({
            party: { id: this.partyId },
            warehouseId
        });
    }
}