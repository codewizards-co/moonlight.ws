import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {concatMap, Observable} from 'rxjs';
import {concatUrlSegments} from '../util/url.util';
import {RestConfig} from './rest.config';
import {KeycloakProvider} from './keycloak.provider';
import {AbstractPage} from './model/abstract-page';
import { ReadOptionSet } from './model/read-option-set';

@Injectable()
export abstract class AbstractRestService<T, P extends AbstractPage<T>> {
    protected readonly restConfig = inject(RestConfig);
    protected readonly httpClient = inject(HttpClient);
    protected readonly keycloakProvider = inject(KeycloakProvider);

    // eslint-disable-next-line @angular-eslint/prefer-inject
    protected constructor(protected readonly resourcePath: string) {
    }

    protected getEntity(...path: any[]): Observable<T> {
        const url = concatUrlSegments(this.restConfig.restUrl, this.resourcePath, ...path);
        return this.keycloakProvider.refreshIfNeeded().pipe(
            concatMap(() => this.httpClient.get(url, this.getHttpOptions()) as Observable<T>)
        );
    }

    protected getPage(...path: any[]): Observable<P> {
        const url = concatUrlSegments(this.restConfig.restUrl, this.resourcePath, ...path);
        return this.keycloakProvider.refreshIfNeeded().pipe(
            concatMap(() => this.httpClient.get(url, this.getHttpOptions()) as Observable<P>)
        );
    }

    protected postEntity(entity: any, ...path: any[]): Observable<T> {
        const url = concatUrlSegments(this.restConfig.restUrl, this.resourcePath, ...path);
        return this.keycloakProvider.refreshIfNeeded().pipe(
            concatMap(() => this.httpClient.post(url, entity, this.getHttpOptions()) as Observable<T>)
        );
    }

    protected postEntitySpecial<R>(entity: any, ...path: any[]): Observable<R> {
        const url = concatUrlSegments(this.restConfig.restUrl, this.resourcePath, ...path);
        return this.keycloakProvider.refreshIfNeeded().pipe(
            concatMap(() => this.httpClient.post(url, entity, this.getHttpOptions()) as Observable<R>)
        );
    }

    protected putEntity(entity: any, ...path: any[]): Observable<T> {
        const url = concatUrlSegments(this.restConfig.restUrl, this.resourcePath, ...path);
        return this.keycloakProvider.refreshIfNeeded().pipe(
            concatMap(() => this.httpClient.put(url, entity, this.getHttpOptions()) as Observable<T>)
        );
    }

    protected deleteEntity(...path: any[]): Observable<any> {
        const url = concatUrlSegments(this.restConfig.restUrl, this.resourcePath, ...path);
        return this.keycloakProvider.refreshIfNeeded().pipe(
            concatMap(() => this.httpClient.delete(url, this.getHttpOptions()))
        );
    }

    protected appendReadOptionSet(path: any[], readOptionSet?: ReadOptionSet): void {
        if (readOptionSet?.fetch) {
            if (!path.find(p => p === '?' || (typeof p === 'string' && p.startsWith('?')))) {
                path.push('?');
            }
            path.push(`fetch=${encodeURIComponent(readOptionSet.fetch)}`);
        }
    }

    protected getHttpOptions() {
        return {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + this.keycloakProvider.getBearerToken()
            })
        };
    }
}