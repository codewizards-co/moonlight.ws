import { Component, inject, ViewEncapsulation } from '@angular/core';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { CommonModule } from '@angular/common';
import { RestModule } from '../rest/rest.module';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
import { Warehouse } from '../rest/model/warehouse';
import { getL10n } from '../util/i18n.util';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ServiceModule } from 'src/service/service.module';

@Component({
    selector: 'mlws-warehouse-selector',
    imports: [CommonModule, MatSlideToggleModule, RestModule, ServiceModule, MatFormFieldModule, MatSelectModule, MatProgressSpinnerModule],
    templateUrl: './warehouse-selector.component.html',
    styleUrls: ['./warehouse-selector.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class WarehouseSelectorComponent {

    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);

    protected get selectedWarehouse(): Warehouse|undefined {
        return this.warehouseSelectorService.getSelectedWarehouse();
    }

    protected set selectedWarehouse(warehouse: Warehouse|undefined) {
        if (warehouse && this.selectedWarehouse !== warehouse) {
            console.log('selectWarehouse:', warehouse);
            this.warehouseSelectorService.selectWarehouse(warehouse);
        }
    }

    public constructor() {
        this.warehouseSelectorService.getSelectedWarehouse$().subscribe({
            next: (w) => {
                this.selectedWarehouse = w;
            },
            error: (err) => {
                console.log(err);
                alert("Loading the warehouses failed. Please try again in 5 minutes and contact your administrator, if the problem persists.");
            }
        });
    }
}