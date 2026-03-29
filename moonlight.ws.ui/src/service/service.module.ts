import { NgModule } from '@angular/core';
import { RestModule } from '../rest/rest.module';
import { CountryService } from './country.service';

@NgModule({
	declarations: [],
	imports: [RestModule],
	providers: [
		CountryService
	]
})
export class ServiceModule {
}