import {Pipe, PipeTransform} from "@angular/core";
import {DateTime} from "luxon";

@Pipe({ name: 'timestamp' })
export class TimestampPipe implements PipeTransform {
    public transform(value: any, ...args: any[]): any {
        return DateTime.fromISO(value).toISO({ precision: 'second', includeOffset: false })!.replace('T', ' ');
    }
}