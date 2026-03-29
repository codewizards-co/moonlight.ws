import { DateTime } from 'luxon';

export function dateWithoutTime(date?: DateTime|string|null): string|undefined {
    if (!date) {
        return undefined;
    }
    if (typeof date === 'string') {
        date = DateTime.fromISO(date);
    }
    return date.toISODate()!;
}

