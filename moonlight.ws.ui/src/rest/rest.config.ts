import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, firstValueFrom, of, tap } from 'rxjs';
import { concatUrlSegments, getAppUrl } from '../util/url.util';

@Injectable({
  providedIn: 'root',
})
export class RestConfig {
  private readonly httpClient = inject(HttpClient);
  private initResult?: Promise<any>;

  public async init(): Promise<any> {
    if (this.initResult) {
      return this.initResult;
    }
    const restConfigFileName = 'moonlight.ws.ui_rest.config.json';
    const url = concatUrlSegments(getAppUrl(false), '../' + restConfigFileName);

    this.initResult = firstValueFrom(
      this.httpClient.get(url, {}).pipe(
        tap((json: any) => {
          console.log(`Fetching ${restConfigFileName} successful.`, json);
          this.restUrl = json.restUrl;
          this.openidUrl = json.openidUrl;
          this.openidRealm = json.openidRealm;
          this.openidClientId = json.openidClientId;
          this.validate();
        }),
        catchError((err) => {
          console.warn(
            `Fetching ${restConfigFileName} failed. Falling back to development-environment.`,
            err,
          );
          try {
            this.initDevEnv();
            this.validate();
          } catch (x) {
            console.error('Falling back to development-environment FAILED!', x);
          }
          return of({});
        }),
      ),
    );
    return this.initResult;
  }

  protected initDevEnv(): void {
    this.restUrl = 'http://localhost:8080/moonlight.ws.rs';
    this.openidUrl = 'https://codewizards.co:4443';
    this.openidRealm = 'codewizards';
    this.openidClientId = 'dragonkingchocolate-webui';
  }

  protected validate(): void {
    if (!this.restUrl) {
      throw new Error('restUrl is undefined!');
    }
    if (!this.openidUrl) {
      throw new Error('openidUrl is undefined!');
    }
    if (!this.openidRealm) {
      throw new Error('openidRealm is undefined!');
    }
    if (!this.openidClientId) {
      throw new Error('openidClientId is undefined!');
    }
  }

  public restUrl = '';
  public openidUrl = '';
  public openidRealm = '';
  public openidClientId = '';
}