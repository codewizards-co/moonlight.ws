import { ChangeDetectionStrategy, Component, EventEmitter, inject, Output, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { concatMap, of } from 'rxjs';
import {v4 as uuid} from 'uuid';
import { Supplier } from '../rest/model/supplier';
import { ServiceModule } from '../service/service.module';
import { RestModule } from '../rest/rest.module';
import { PartyRestService } from '../rest/party-rest.service';
import { SupplierRestService } from '../rest/supplier-rest.service';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { PartyPage } from '../rest/model/party';

@Component({
    selector: 'mlws-create-supplier-overlay',
    imports: [
        CommonModule, FormsModule, MatFormFieldModule, MatInputModule,
        ReactiveFormsModule, ServiceModule, RestModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
        MatSelectModule
    ],
    templateUrl: './create-supplier-overlay.component.html',
    styleUrls: ['./create-supplier-overlay.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
@UntilDestroy()
export class CreateSupplierOverlayComponent {

    protected readonly partyRestService = inject(PartyRestService);
    protected readonly supplierRestService = inject(SupplierRestService);

    protected supplier: Supplier = {
        party: {
            code: uuid().substring(24, 36)
        }
    };

    /**
     * If the user aborts (e.g. clicking "Cancel"), this emits undefined. Otherwise, it emits a new Supplier after it was persisted.
     */
    @Output()
    public supplierCreated$ = new EventEmitter<Supplier|undefined>();

    protected onCancelClick(): void {
        this.supplierCreated$.emit(undefined);
    }

    protected onSaveClick(): void {
        this.partyRestService.getPartyPage({filterCode: this.supplier.party.code }).pipe( //
            untilDestroyed(this), //
            concatMap((partyPage: PartyPage) => partyPage.items.length > 0 ? of(partyPage.items[0]) : this.partyRestService.postParty(this.supplier.party)), //
            concatMap(party => { //
                const supplier = JSON.parse(JSON.stringify(this.supplier)); //
                supplier.party = { id: party.id }; //
                return this.supplierRestService.postSupplier(supplier); //
            }) //
        ).subscribe((supplier: Supplier) => this.supplierCreated$.emit(supplier));
    }
}