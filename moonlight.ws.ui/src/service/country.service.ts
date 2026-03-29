import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, take } from 'rxjs';
import { CountryRestService } from '../rest/country-rest.service';
import { Country } from '../rest/model/country';

@Injectable()
export class CountryService {

    protected readonly countryRestService = inject(CountryRestService);
    public readonly loading$: BehaviorSubject<boolean> = new BehaviorSubject(false);
    protected loadingCount = 0;
    public readonly countries$ = new BehaviorSubject<Country[]>([]);

    public constructor() {
        this.loadCountries();
    }

    public getCountryByIsoCode(code?: string): Country | undefined {
        if (this.loading$.getValue()) {
            throw new Error('Still loading countries!');
        }
        if (!code) {
            return undefined;
        }
        return this.countries$.getValue().find(c => c.a2 === code || c.a3 === code);
    }

    private loadCountries(pageNumber = 1, countries: Country[] = []): void {
        this.loadingCountInc();
        if (pageNumber === 1) {
            countries.push({ id: undefined, name: undefined, title_i18n: { } });
        }
        this.countryRestService.getCountryPage({ filterActive: true, pageNumber, pageSize: 500 })
            .pipe(take(1))
            .subscribe(countryPage => {
                countries.push(...(countryPage.items??[]));
                if (pageNumber < countryPage.lastPageNumber!) {
                    this.loadCountries(pageNumber + 1, countries);
                } else {
                    this.countries$.next(countries);
                }
                this.loadingCountDec();
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
}