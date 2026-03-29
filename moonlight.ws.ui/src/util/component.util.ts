import { BehaviorSubject, debounceTime } from 'rxjs';
import { getValidFiniteNumber } from './number.util';
import { untilDestroyed } from '@ngneat/until-destroy';
import { DateTime } from 'luxon';

export function createNumberPropertyUndefined(component: any, propertyName: string, defaultValue: number | undefined): BehaviorSubject<number | undefined> {
    const oldValue: number | undefined = getValidFiniteNumber(localStorage.getItem(propertyName), defaultValue)!;
    const result$ = new BehaviorSubject<number | undefined>(oldValue);
    result$.pipe(untilDestroyed(component), debounceTime(500)).subscribe(newValue => newValue === undefined ? localStorage.removeItem(propertyName) : localStorage.setItem(propertyName, '' + newValue));
    return result$;
}

export function createNumberPropertyDefined(component: any, propertyName: string, defaultValue: number): BehaviorSubject<number> {
    const oldValue: number = getValidFiniteNumber(localStorage.getItem(propertyName), defaultValue)!;
    const result$ = new BehaviorSubject<number>(oldValue);
    result$.pipe(untilDestroyed(component), debounceTime(500)).subscribe(newValue => newValue === undefined ? localStorage.removeItem(propertyName) : localStorage.setItem(propertyName, '' + newValue));
    return result$;
}

export function createStringPropertyUndefined(component: any, propertyName: string, defaultValue: string | undefined): BehaviorSubject<string | undefined> {
    let oldValue: string | null | undefined = localStorage.getItem(propertyName);
    if (oldValue === null || oldValue === undefined) {
        oldValue = defaultValue;
    }
    const result$ = new BehaviorSubject<string | undefined>(oldValue);
    result$.pipe(untilDestroyed(component), debounceTime(500)).subscribe(newValue => newValue === undefined || newValue === null ? localStorage.removeItem(propertyName) : localStorage.setItem(propertyName, newValue));
    return result$;
}

export function createStringPropertyDefined(component: any, propertyName: string, defaultValue: string): BehaviorSubject<string> {
    let oldValue: string | null | undefined = localStorage.getItem(propertyName);
    if (oldValue === null || oldValue === undefined) {
        oldValue = defaultValue;
    }
    const result$ = new BehaviorSubject<string>(oldValue);
    result$.pipe(untilDestroyed(component), debounceTime(500)).subscribe(newValue => newValue === undefined || newValue === null ? localStorage.removeItem(propertyName) : localStorage.setItem(propertyName,  newValue));
    return result$;
}

export function createDateTimePropertyUndefined(component: any, propertyName: string, defaultValue: DateTime | undefined): BehaviorSubject<DateTime|undefined>  {
    let oldValue: DateTime|undefined = undefined;
    const oldString = localStorage.getItem(propertyName);
    if (oldString) {
        oldValue = DateTime.fromISO(oldString);
    }
    if (!oldValue) {
        oldValue = defaultValue;
    }
    const result$ = new BehaviorSubject<DateTime|undefined>(oldValue);
    result$.pipe(untilDestroyed(component), debounceTime(500)).subscribe(newValue => !newValue ? localStorage.removeItem(propertyName) : localStorage.setItem(propertyName, newValue.toISO()!));
    return result$;
}

export function createBooleanPropertyDefined(component: any, propertyName: string, defaultValue: boolean): BehaviorSubject<boolean>  {
    let oldValue: boolean;
    const oldString = localStorage.getItem(propertyName);
    if (oldString === null) {
        oldValue = defaultValue;
    } else {
        if (oldString === 'true') {
            oldValue = true;
        } else if (oldString === 'false') {
            oldValue = false;
        } else {
            oldValue = defaultValue;
        }
    }
    const result$ = new BehaviorSubject<boolean>(oldValue);
    result$.pipe(untilDestroyed(component), debounceTime(500)).subscribe(newValue => newValue === undefined ? localStorage.removeItem(propertyName) : localStorage.setItem(propertyName, newValue ? 'true' : 'false'));
    return result$;
}