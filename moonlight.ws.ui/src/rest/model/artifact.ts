import {AbstractPage} from './abstract-page';

export interface Artifact {
    groupId: string;
    artifactId: string;
    version: string;
}

// eslint-disable-next-line
export interface ArtifactPage extends AbstractPage<Artifact> {}
