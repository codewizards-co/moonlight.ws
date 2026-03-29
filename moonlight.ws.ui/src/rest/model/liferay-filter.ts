import {AbstractFilter} from './abstract-filter';

export interface LiferayFilter extends AbstractFilter {
    search?: string;
    filterString?: string;
}