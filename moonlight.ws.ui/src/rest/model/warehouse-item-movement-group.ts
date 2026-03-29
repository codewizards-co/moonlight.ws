import {AbstractPage} from './abstract-page';
import {AbstractFilter} from './abstract-filter';

export interface WarehouseItemMovementGroup {
    id?: number;
    created?: string; // TODO date+time
    createdByUserId?: number;
    changed?: string; // TODO date+time
    changedByUserId?: number;
    draft?: boolean;
    finalized?: string; // TODO date+time
}

// eslint-disable-next-line
export interface WarehouseItemMovementGroupPage extends AbstractPage<WarehouseItemMovementGroup> {}

export interface WarehouseItemMovementGroupFilter extends AbstractFilter {
    filterDraft?: boolean;
}