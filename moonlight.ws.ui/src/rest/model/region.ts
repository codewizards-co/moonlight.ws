
export interface Region {
    id?: number;
    countryId?: number;
    active?: boolean;
    name?: string;
    position?: number;
    regionCode?: string;
    title_i18n?: { [key: string]: string };
}