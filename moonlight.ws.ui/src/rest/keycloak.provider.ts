import Keycloak, {KeycloakConfig, KeycloakInitOptions} from 'keycloak-js';
import {inject, Injectable} from '@angular/core';
import {RestConfig} from './rest.config';
import {BehaviorSubject, catchError, concatMap, from, map, Observable, tap} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class KeycloakProvider {

    private readonly restConfig = inject(RestConfig);

    private keycloak: Keycloak|undefined;
    public readonly bearerToken$ = new BehaviorSubject<string>("");
    public readonly bearerTokenSha256$ = new BehaviorSubject<string>("");

    /**
     * Time before expiry in seconds. If the access token expires in less than X seconds, refresh it first.
     * <p>
     *     If the token already expired, it is also refreshed, of course, if this is still possible.
     * @see #refreshIfNeeded()
     */
    private readonly ACCESS_TOKEN_EXPIRES_IN_THRESHOLD = 10;

    public constructor() {
        this.bearerToken$.pipe(
            concatMap(bearerToken => {
                const bearerTokenBinary = new TextEncoder().encode(bearerToken);
                return crypto.subtle.digest("SHA-256", bearerTokenBinary);
            }),
            map(sha256Binary => {
                const hashArray = Array.from(new Uint8Array(sha256Binary));
                return hashArray.map(b => ('00' + b.toString(16)).slice(-2)).join('');
            })
        ).subscribe(bearerTokenSha256 => this.bearerTokenSha256$.next(bearerTokenSha256));
    }

    public async init(): Promise<any> {
        await this.restConfig.init();

        const config: KeycloakConfig = {
            url: this.restConfig.openidUrl,
            realm: this.restConfig.openidRealm,
            clientId: this.restConfig.openidClientId
        }
        this.keycloak = new Keycloak(config);

        const initOptions: KeycloakInitOptions = {
            onLoad: 'login-required',
            checkLoginIframe: false
        };
        return this.keycloak.init(initOptions).then((b: boolean) => {
            this.bearerToken$.next(this.keycloak?.token ?? "");
            return b;
        });
    }

    public getBearerToken(): string {
        if (!this.keycloak) {
            throw new Error('init() was not called, before!');
        }
        return this.keycloak.token!;
    }

    /**
     * Refreshes the token if it is about to expire.
     * @return true, if the token was refreshed.
     * @see #ACCESS_TOKEN_EXPIRES_IN_THRESHOLD
     */
    public refreshIfNeeded(): Observable<boolean> {
        if (!this.keycloak) {
            throw new Error('init() was not called, before!');
        }
        return from(this.keycloak.updateToken(this.ACCESS_TOKEN_EXPIRES_IN_THRESHOLD))
            .pipe(
                tap((refreshed: boolean) => {
                    if (refreshed) {
                        this.bearerToken$.next(this.keycloak?.token ?? "");
                    }
                }),
                catchError((err, caught) => {
                    console.error("Refreshing token failed!", err);
                    return from(this.logout()).pipe(map(() => true));
                })
            );
    }

    /**
     * Warning: Redirects away! We are leaving the app when calling this.
     */
    public logout(): Promise<void> {
        if (!this.keycloak) {
            throw new Error('init() was not called, before!');
        }
        return this.keycloak.logout();
    }
}