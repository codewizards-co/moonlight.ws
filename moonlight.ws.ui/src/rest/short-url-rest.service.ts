import { Injectable } from '@angular/core';
import { AbstractRestService } from './abstract-rest.service';
import { ShortUrl, ShortUrlPage } from './model/short-url';
import { Observable } from 'rxjs';

@Injectable()
export class ShortUrlRestService extends AbstractRestService<ShortUrl, ShortUrlPage> {

  public constructor() {
    super('short-url');
  }

  public getShortUrl(id: number): Observable<ShortUrl> {
    return this.getEntity(id);
  }

  public getShortUrlByUrl(url: string): Observable<ShortUrl> {
    return this.getEntity("by-url", encodeURIComponent(url));
  }

  public postShortUrl(shortUrl: ShortUrl): Observable<ShortUrl> {
    return this.postEntity(shortUrl);
  }
}
