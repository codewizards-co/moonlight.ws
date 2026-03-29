
export function getL10n(i18n?: {[locale: string]: string}, preferredLocale?: string): string|undefined {
    if (!i18n) {
        return undefined;
    }
    if (!preferredLocale) {
        preferredLocale = "en_US";
    }
    preferredLocale = preferredLocale.replace("-", "_"); // Angular uses "en-US", but Liferay/Java uses "en_US".
    let result = i18n[preferredLocale];
    if (result !== undefined && result !== null) {
        return result;
    }
    for (const key in i18n) {
        result = i18n[key];
        if (result !== undefined && result !== null) {
            return result;
        }
    }
    return undefined;
}