import { Route } from '@angular/router';
import { PartyListPage } from '../page/party-list.page';
import { ShortUrlPage } from '../page/short-url.page';
import { WarehouseItemMovementListPage } from '../page/warehouse-item-movement-list.page';
import { WarehouseItemListPage } from '../page/warehouse-item-list.page';
import { WarehouseItemPage } from '../page/warehouse-item.page';
import { WelcomePage } from '../page/welcome.page';
import { PartyPage } from '../page/party.page';
import { InvoiceListPage } from '../page/invoice-list.page';
import { InvoicePage } from '../page/invoice.page';

export const appRoutes: Route[] = [
    {path: '', component: WelcomePage},
    {path: 'invoice-list', pathMatch: 'full', component: InvoiceListPage},
    {path: 'invoice', pathMatch: 'full', component: InvoicePage},
    {path: 'party-list', pathMatch: 'full', component: PartyListPage},
    {path: 'party', component: PartyPage},
    {path: 'short-url', pathMatch: 'full', component: ShortUrlPage},
    {path: 'warehouse-item-list', pathMatch: 'full', component: WarehouseItemListPage},
    {path: 'warehouse-item', component: WarehouseItemPage},
    {path: 'warehouse-item-movement-list', pathMatch: 'full', component: WarehouseItemMovementListPage}
];
