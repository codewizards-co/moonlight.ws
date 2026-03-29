import { ErrorHandler, Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";
import {v4 as uuid} from 'uuid';

@Injectable()
export class MlwsErrorHandler implements ErrorHandler {

	public readonly instanceId = uuid();

	public readonly errorMessage$ = new BehaviorSubject<string|undefined>(undefined);
	public readonly error$ = new BehaviorSubject<any>(undefined);
	public readonly errorJson$ = new BehaviorSubject<string|undefined>(undefined);

	public constructor() {
		console.info(`[${this.instanceId}]: created.`);
	}

	public handleError(error: any): void {
		const errorMessage = this.getErrorMessage(error);
		console.info(`[${this.instanceId}]: ${errorMessage}`, error);
		this.errorMessage$.next(errorMessage);
		this.errorJson$.next(JSON.stringify(error, null, 2));
		this.error$.next(error);
	}

	public clear(): void {
		this.errorMessage$.next(undefined);
		this.errorJson$.next(undefined);
		this.error$.next(undefined);
	}

	protected getErrorMessage(error: any): string {
		if (error === undefined || error === null) {
			console.error("ERROR: error is undefined/null! This should never happen!");
			return "Unknown error!";
		}
		if (error.message) {
			return error.message;
		}
		if (error.statusText) {
			return error.statusText;
		}
		// fallback
		return "Unknown error!";
	}
}