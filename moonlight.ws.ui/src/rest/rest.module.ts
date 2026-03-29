import { NgModule } from '@angular/core';
import { ArtifactRestService } from './artifact-rest.service';
import { ConsigneeRestService } from './consignee-rest.service';
import { CountryRestService } from './country-rest.service';
import { InvoiceItemRestService, InvoiceRestService } from './invoice-rest.service';
import { PartyRestService } from './party-rest.service';
import { ShortUrlRestService } from './short-url-rest.service';
import { SkuRestService } from './sku-rest.service';
import { SupplierRestService } from './supplier-rest.service';
import { WarehouseItemMovementGroupRestService } from './warehouse-item-movement-group-rest.service';
import { WarehouseItemMovementRestService } from './warehouse-item-movement-rest.service';
import { WarehouseItemRestService } from './warehouse-item-rest.service';
import { WarehouseRestService } from './warehouse-rest.service';

@NgModule({
	declarations: [],
	imports: [],
	providers: [
		ArtifactRestService,
		ConsigneeRestService,
		CountryRestService,
		InvoiceRestService,
		InvoiceItemRestService,
		PartyRestService,
		ShortUrlRestService,
		SupplierRestService,
		SkuRestService,
		WarehouseItemMovementGroupRestService,
		WarehouseItemMovementRestService,
		WarehouseItemRestService,
		WarehouseRestService
	]
})
export class RestModule {
}