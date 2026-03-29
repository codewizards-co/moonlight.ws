import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AbstractRestService} from './abstract-rest.service';
import {Artifact, ArtifactPage} from './model/artifact';

@Injectable()
export class ArtifactRestService extends AbstractRestService<Artifact, ArtifactPage> {

    constructor() {
        super("artifact");
    }

    public getArtifact(groupId: string, artifactId: string): Observable<Artifact> {
        return this.getEntity(groupId, artifactId);
    }
}
