import { AbstractPage } from './abstract-page';

export interface ShortUrl {
  id?: number;
  created?: string; // TODO date+time
  createdByUserId?: number;
  changed?: string; // TODO date+time
  changedByUserId?: number;

  code?: string;
  longUrl: string;
  shortUrl?: string;
}

// eslint-disable-next-line
export interface ShortUrlPage extends AbstractPage<ShortUrl> {}
