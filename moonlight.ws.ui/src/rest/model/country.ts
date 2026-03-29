import { AbstractPage } from './abstract-page';
import { LiferayFilter } from './liferay-filter';
import { Region } from './region';

export interface Country {
    id?: number;
    idd?: number;
    a2?: string;
    a3?: string;
    active?: boolean;
    billingAllowed?: boolean;
    groupFilterEnabled?: boolean;
    name?: string;
    number?: number;
    position?: number;
    regions?: Region[];
    shippingAllowed?: boolean;
    subjectToVAT?: boolean;
    title_i18n?: { [key: string]: string };
    zipRequired?: boolean;
}

// eslint-disable-next-line
export interface CountryPage extends AbstractPage<Country> {}

export interface CountryFilter extends LiferayFilter {
    filterActive?: boolean;
}