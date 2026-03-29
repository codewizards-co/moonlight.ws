import { ApplicationConfig, DEFAULT_CURRENCY_CODE, ErrorHandler, inject, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withHashLocation } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { RestConfig } from '../rest/rest.config';
import { KeycloakProvider } from '../rest/keycloak.provider';
import { appRoutes } from './app.routes';
import { customHttpInterceptor } from '../rest/custom-http-interceptor';
import { MlwsErrorHandler } from '../service/mlws-error-handler';
import { MAT_LUXON_DATE_FORMATS, provideLuxonDateAdapter } from '@angular/material-luxon-adapter';

export const appConfig: ApplicationConfig = {
    providers: [ //
        provideZoneChangeDetection({ eventCoalescing: true }), //
        provideRouter(appRoutes, withHashLocation()), //
        provideHttpClient(withInterceptors([customHttpInterceptor])), //
        provideAppInitializer(() => { //
            const restConfig = inject(RestConfig);
            return restConfig.init();
        }), //
        provideAppInitializer(() => { //
            const keycloakProvider = inject(KeycloakProvider);
            return keycloakProvider.init();
        }), //
        // { provide: DEFAULT_CURRENCY_CODE, useValue: '฿' },
        { provide: MAT_DATE_LOCALE, useValue: 'en-DK' }, //
        // provideNativeDateAdapter(MAT_NATIVE_DATE_FORMATS),
        provideLuxonDateAdapter(),
        MlwsErrorHandler, //
        { provide: ErrorHandler, useExisting: MlwsErrorHandler } //
    ]
};
