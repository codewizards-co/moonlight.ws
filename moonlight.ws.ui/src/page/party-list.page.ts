import { ChangeDetectionStrategy, Component, ViewEncapsulation } from '@angular/core';
import { PartyListComponent } from '../component/party-list.component';

@Component({
    selector: 'mlws-party-list-page', imports: [PartyListComponent],
    templateUrl: './party-list.page.html',
    styleUrls: ['./party-list.page.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PartyListPage {

}