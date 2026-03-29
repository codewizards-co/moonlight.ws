import {inject} from "@angular/core";
import {HttpEvent, HttpHandlerFn, HttpRequest, HttpXsrfTokenExtractor} from "@angular/common/http";
import {Observable} from 'rxjs';

export function customHttpInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
    // This interceptor works, but I can't get the cookies stored in my browser :-(
    // const tokenExtractor = inject(HttpXsrfTokenExtractor);
    //
    // // send request with credential options in order to be able to read cross-origin cookies
    // req = req.clone({ withCredentials: true });
    //
    // // return XSRF-TOKEN in each request's header (anti-CSRF security)
    // const headerName = 'X-XSRF-TOKEN';
    // const token = tokenExtractor.getToken() as string;
    // if (token !== null && !req.headers.has(headerName)) {
    //     req = req.clone({ headers: req.headers.set(headerName, token) });
    // }
    return next(req);
}
