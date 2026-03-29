import {Component, inject, OnInit, ViewEncapsulation} from '@angular/core';
import {AsyncPipe} from '@angular/common';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {BehaviorSubject} from 'rxjs';
import packageJson from '../../package.json';
import {ArtifactRestService} from '../rest/artifact-rest.service';
import {RestModule} from '../rest/rest.module';
import {concatUrlSegments} from '../util/url.util';
import { WarehouseItemMovementListPage } from './warehouse-item-movement-list.page';

@Component({
  selector: 'mlws-welcome-page',
  imports: [AsyncPipe, RestModule],
  templateUrl: './welcome.page.html',
  styleUrls: ['./welcome.page.scss'],
  encapsulation: ViewEncapsulation.None
})
@UntilDestroy()
export class WelcomePage implements OnInit {
  protected readonly artifactRestService = inject(ArtifactRestService);

  public readonly frontendVersion: string = packageJson.version;
  public readonly backendVersion$ = new BehaviorSubject<string>('(loading)');
  public readonly backendVersionError$ = new BehaviorSubject<string|undefined>(undefined);

  public async ngOnInit(): Promise<void> {
    console.log('WelcomePage.ngOnInit');

    this.backendVersionError$.next(undefined);
    this.artifactRestService
      .getArtifact('moonlight.ws', 'mlws.rs')
      .pipe(untilDestroyed(this))
      .subscribe({
        next: (artifact) => this.backendVersion$.next(artifact.version),
        error: (error) => {
          console.error(error);
          this.backendVersion$.next("ERROR");
          this.backendVersionError$.next(JSON.stringify(error, null, 2));
        }
      });
  }

  protected readonly encodeURIComponent = encodeURIComponent;
  protected readonly concatUrlSegments = concatUrlSegments;
}