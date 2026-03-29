import {AbstractPage} from './abstract-page';
import {AbstractFilter} from './abstract-filter';
import {LiferayFilter} from './liferay-filter';

export interface CustomValue {
    data: string;
    data_i18n: { [locale: string]: string };
    geo: {
        latitude: number;
        longitude: number;
    }
}

export interface CustomField {
    name: string;
    customValue?: CustomValue,
    dataType: string;
}

export interface SkuSubscriptionConfiguration {
    deliverySubscriptionEnable?: boolean;
    deliverySubscriptionLength?: number;
    deliverySubscriptionNumberOfLength?: number;
    /**
     * example: {deliveryMonthDay=1, deliveryMonthlyMode=0}
     */
    deliverySubscriptionTypeSettings?: { [key: string]: string };
    enable?: boolean;
    length?: number;
    numberOfLength?: number;
    overrideSubscriptionInfo?: boolean;
    /**
     * example: {monthDay=1, monthlyMode=0}
     */
    subscriptionTypeSettings?: { [key: string]: string };
    /**
     * example: monthly
     * <p>
     *     Enum:
     *     [ daily, monthly, weekly, yearly ]
     */
    deliverySubscriptionType?: string;
    /**
     * example: monthly
     * <p>
     *     Enum:
     *     [ daily, monthly, weekly, yearly ]
     */
    subscriptionType?: string;
}

export interface SkuUnitOfMeasure {
    id?: number;
    skuId?: number;
    sku?: string;
    actions?: { [key: string]: string };
    active?: boolean;
    basePrice?: number;
    incrementalOrderQuantity?: number;
    key?: string;
    name?: { [locale: string]: string };
    precision?: number;
    pricingQuantity?: number;
    primary?: boolean;
    priority?: number;
    promoPrice?: number;
    rate?: number;
}

export interface SkuVirtualSettings {
    activationStatus?: number;
    activationStatusInfo?: {
        code?: number;
        label?: string;
        label_i18n?: string;
    };
// attachment	string
//
// Base64 encoded file
// duration	integer($int64)
//
// Number of days to download the attachment
// id	integer($int64)
// example: 30130
// maxUsages	integer($int32)
//
// Number of downloads available for attachment
//     override	boolean
//
// Override product virtual settings
// sampleAttachment	string
//
// Base64 encoded sample file
// sampleSrc	string
// readOnly: true
//
// URL to download the sample file
// sampleURL	string
//
// URL of the sample file
// skuVirtualSettingsFileEntries	[SkuVirtualSettingsFileEntry{...}]
// src	string
// readOnly: true
//
// URL to download the file
// termsOfUseContent	{
//     description:
//
//         Terms of Use content
//     < * >:	[...]
// }
// example: {en_US=Croatia, hr_HR=Hrvatska, hu_HU=Horvatorszag}
// termsOfUseJournalArticleId	integer($int64)
//
// Terms of Use related Article Id
// termsOfUseRequired	boolean
//
// Terms of Use required
// url	string
//
// URL of the file
// useSample	boolean
//
// Enable sample file
}

export interface Sku {
    id?: number;
    externalReferenceCode?: string;
    sku: string;
    cost?: number;
    customFields?: CustomField[];
    depth?: number;
    discontinued?: boolean;
    discontinuedDate?: string; // TODO date+time?
    displayDate?: string; // TODO date+time?
    expirationDate?: string; // TODO date+time?
    gtin?: string;
    height?: number;
    inventoryLevel?: number;
    manufacturerPartNumber?: string;
    neverExpire?: boolean;
    price?: number;
    productId?: number;
    productName?: {[locale: string]: string};
    promoPrice?: number;
    published?: boolean;
    purchasable?: boolean;
    replacementSkuExternalReferenceCode?: string;
    replacementSkuId?: number;
    skuOptions?: [{
        key: string;
        optionId?: number;
        optionValueId?: number;
        value?: string;
    }];
    skuSubscriptionConfiguration?: SkuSubscriptionConfiguration;
    skuUnitOfMeasures?: SkuUnitOfMeasure[];
    skuVirtualSettings?: SkuVirtualSettings;
    unitOfMeasureKey?: string;
    unitOfMeasureName?: { [locale: string]: string };
    unitOfMeasureSkuId?: string;
    unspsc?: string;
    weight?: number;
    width?: number;
}

// eslint-disable-next-line
export interface SkuPage extends AbstractPage<Sku> {}

export interface SkuFilter extends LiferayFilter {
    filterSku?: string;
}
