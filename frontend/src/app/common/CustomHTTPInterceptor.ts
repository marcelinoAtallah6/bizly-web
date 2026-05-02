import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, finalize, Observable, retry, throwError } from "rxjs";
import { GlobalConstants } from "./GlobalConstants";

@Injectable()
export class CustomHTTPInterceptor implements HttpInterceptor {

    // Request Interceptor Class

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const reqRefreshToken = localStorage.getItem('jwtAccessToken');
        const reqWithAuth = req.clone({ headers: GlobalConstants.headers });

        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                return throwError(error);
            }),
            finalize(() => {
                const request = `${req.method} "${req.urlWithParams}"`;
            })
        );
    } 
}