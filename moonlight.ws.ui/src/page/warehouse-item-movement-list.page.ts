import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { WarehouseItemMovementListComponent } from '../component/warehouse-item-movement-list.component';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';

@Component({
    selector: 'mlws-warehouse-item-movement-list-page', imports: [WarehouseItemMovementListComponent],
    templateUrl: './warehouse-item-movement-list.page.html',
    styleUrls: ['./warehouse-item-movement-list.page.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WarehouseItemMovementListPage implements OnInit, OnDestroy {

    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);

    public ngOnInit(): void {
        console.info('WarehouseItemMovementListPage.ngOnInit');
        this.warehouseSelectorService.visible$.next(true);
    }

    public ngOnDestroy(): void {
        console.info('WarehouseItemMovementListPage.ngOnDestroy');
        this.warehouseSelectorService.visible$.next(false);
    }
}