import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http'
import {RestConfig} from './rest.config';
import {concat, concatMap, map, Observable} from 'rxjs';
import {Person} from './model/person';
import {PersonPage} from './model/person-page';
import {PersonFilter} from './model/person-filter';
import {KeycloakProvider} from './keycloak.provider';
import {AbstractRestService} from './abstract-rest.service';

@Injectable()
export class PersonService extends AbstractRestService<Person, PersonPage> {

    constructor() {
        super("person");
    }

    public getPerson(id: number): Observable<Person> {
        return this.getEntity(id);
    }

    public getPersons(personFilter?: PersonFilter): Observable<PersonPage> {
        const query: any[] = [];
        if (personFilter) {
            query.push("?");
            if (personFilter.pageNumber) {
                query.push(`pageNumber=${personFilter.pageNumber}`);
            }
            if (personFilter.pageSize !== undefined) {
                query.push(`pageSize=${personFilter.pageSize}`);
            }
            if (personFilter.email) {
                query.push(`email=${personFilter.email}`);
            }
            if (personFilter.firstName) {
                query.push(`email=${personFilter.firstName}`);
            }
            if (personFilter.lastName) {
                query.push(`email=${personFilter.lastName}`);
            }
        }
        return this.getPage(...query);
    }
}