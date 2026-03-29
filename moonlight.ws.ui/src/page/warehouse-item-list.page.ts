import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { WarehouseItemListComponent } from '../component/warehouse-item-list.component';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';

@Component({
	selector: 'mlws-warehouse-item-list-page', imports: [WarehouseItemListComponent],
	templateUrl: './warehouse-item-list.page.html',
	styleUrls: ['./warehouse-item-list.page.scss'],
	encapsulation: ViewEncapsulation.None,
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class WarehouseItemListPage implements OnInit, OnDestroy {

	protected readonly warehouseSelectorService = inject(WarehouseSelectorService);

	public ngOnInit(): void {
		console.info('WarehouseItemListPage.ngOnInit');
		this.warehouseSelectorService.visible$.next(true);
	}

	public ngOnDestroy(): void {
		console.info('WarehouseItemListPage.ngOnDestroy');
		this.warehouseSelectorService.visible$.next(false);
	}
}