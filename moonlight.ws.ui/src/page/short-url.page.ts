import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { RestModule } from '../rest/rest.module';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import {
  BehaviorSubject, catchError, combineLatest, concatMap, debounceTime, filter, map, Observable, of, tap
} from 'rxjs';
import { ShortUrlRestService } from '../rest/short-url-rest.service';
import { ShortUrl } from '../rest/model/short-url';
import { RestConfig } from '../rest/rest.config';
import { concatUrlSegments } from '../util/url.util';
import { KeycloakProvider } from '../rest/keycloak.provider';
import { getValidFiniteNumber, isValidFiniteNumber } from '../util/number.util';

const QR_CODE_SIZE_DEFAULT = 100;
const QR_CODE_SIZE_MIN = 50;
const QR_CODE_SIZE_MAX = 5000;

const QR_CODE_SIZE = "mlws.ShortUrlPage.qrCodeSize";

@Component({
  selector: 'mlws-short-url-page',
  imports: [
    CommonModule,
    MatButtonModule,
    MatPaginatorModule,
    MatTableModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    RestModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './short-url.page.html',
  styleUrls: ['./short-url.page.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class ShortUrlPage implements OnInit, OnDestroy {
  protected readonly restConfig = inject(RestConfig);
  protected readonly keycloakProvider = inject(KeycloakProvider);
  protected readonly urlFormControl = new FormControl('');
  protected readonly qrCodeSizeFormControl = new FormControl<number|null>(QR_CODE_SIZE_DEFAULT);
  protected readonly url$ = new BehaviorSubject<string>('');
  protected readonly qrCodeSize$ = new BehaviorSubject<number>(QR_CODE_SIZE_DEFAULT);
  protected readonly urlInvalidMessage$ = new BehaviorSubject<string|undefined>(undefined);
  protected readonly shortUrl$ = new BehaviorSubject<ShortUrl | undefined>(
    undefined,
  );
  protected readonly shortUrlRestService = inject(ShortUrlRestService);
  protected readonly loading$ = new BehaviorSubject<boolean>(false);
  protected readonly createShortUrlDisabled$ = new BehaviorSubject<boolean>(true);
  protected readonly qrCodeImageUrl$: Observable<string>;

  public constructor() {
    this.qrCodeImageUrl$ = combineLatest({
      bearerTokenSha256: this.keycloakProvider.bearerTokenSha256$,
      shortUrl: this.shortUrl$,
      qrCodeSize: this.qrCodeSize$.pipe(debounceTime(500))
    }).pipe(
        untilDestroyed(this),
        filter(v => !!v.bearerTokenSha256),
        map(v => {
          if (!v.shortUrl) {
            return "";
          }
          const qrCodeContent = encodeURIComponent(v.shortUrl.shortUrl!);
          const redirectUrlEncoded = encodeURIComponent("qr-code/" + qrCodeContent + "?errorCorrectionLevel=H&width=" + v.qrCodeSize + "&height=" + v.qrCodeSize);
          return concatUrlSegments(this.restConfig.restUrl, "auth-and-redirect", v.bearerTokenSha256, redirectUrlEncoded)
        })
    );
  }

  public ngOnInit(): void {
    console.info('ShortUrlPage.ngOnInit');

    const s = localStorage.getItem(QR_CODE_SIZE);
    const qrCodeSize = getValidFiniteNumber(s);
    if (qrCodeSize && qrCodeSize >= QR_CODE_SIZE_MIN && qrCodeSize <= QR_CODE_SIZE_MAX) {
      this.qrCodeSize$.next(qrCodeSize);
      this.qrCodeSizeFormControl.setValue(qrCodeSize);
    }

    this.qrCodeSize$.pipe(untilDestroyed(this)).subscribe(qrCodeSize => {
      if (qrCodeSize) {
        localStorage.setItem(QR_CODE_SIZE, '' + qrCodeSize);
      }
    });

    this.urlFormControl.valueChanges.pipe(untilDestroyed(this)).subscribe((value) => {
      this.url$.next(value === null ? '' : value);
    });

    this.qrCodeSizeFormControl.valueChanges
        .pipe(untilDestroyed(this))
        .subscribe((value) => {
          let qrCodeSize = -1;
          if (isValidFiniteNumber(value)) {
            qrCodeSize = getValidFiniteNumber(value)!;
          }
          if (qrCodeSize < 0 || qrCodeSize < QR_CODE_SIZE_MIN || qrCodeSize > QR_CODE_SIZE_MAX) {
            return;
          }
          if (this.qrCodeSize$.getValue() !== qrCodeSize) {
            this.qrCodeSize$.next(qrCodeSize);
          }
        });

    this.url$
      .pipe(
        untilDestroyed(this),
        tap(() => {
          this.shortUrl$.next(undefined);
          this.urlInvalidMessage$.next(undefined);
          this.loading$.next(true);
          this.createShortUrlDisabled$.next(true);
        }),
        debounceTime(500),
        concatMap((url) => {
          this.urlInvalidMessage$.next(this.validateUrl(url));
          return !url ? of(undefined) : this.shortUrlRestService.getShortUrlByUrl(url).pipe(catchError((error, caught) => {
            if (error.name === 'HttpErrorResponse' && error.status === 404) {
              // this is fine => the URL is unknown
              console.info('This URL is unknown: ' + url);
              return of(undefined);
            }
            throw error;
          }))
        })
      )
      .subscribe({
        next: (shortUrl) => {
          this.shortUrl$.next(shortUrl);
          this.loading$.next(false);
          if (this.url$.getValue() && !shortUrl && !this.urlInvalidMessage$.getValue()) {
            this.createShortUrlDisabled$.next(false);
          }
        },
        error: (error) => {
          this.loading$.next(false);
          console.error(error);
          window.alert("Unexpected error!");
        },
        complete: () => {
          console.info('this.url$-observable completed.');
        },
      });
  }

  public ngOnDestroy(): void {
    console.info('ShortUrlPage.ngOnDestroy');
  }

  protected validateUrl(url: string): string|undefined {
    if (!url) {
      return "URL is empty.";
    }
    if (url.length < 10) {
      return "URL is too short.";
    }
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      return "URL starts with an invalid protocol! Only 'http://' and 'https://' are allowed.";
    }
    if (url.includes(" ")) {
      return "URL contains illegal character. A space is not allowed in a URL!";
    }
    return undefined;
  }

  protected onCreateShortUrl(): void {
    const url = this.url$.getValue();
    if (!url) {
      throw new Error('URL is missing');
    }
    if (this.shortUrl$.getValue()) {
      throw new Error('There is already a short url for this URL.');
    }
    const shortUrl = {
      longUrl: this.url$.getValue()
    };
    this.createShortUrlDisabled$.next(true);
    this.shortUrlRestService.postShortUrl(shortUrl).subscribe({
      next: (shortUrl) => {
        this.shortUrl$.next(shortUrl);
      },
      error: (error) => {
        console.error(error);
        window.alert("Creating the short url failed!");
      }
    });
  }
}
