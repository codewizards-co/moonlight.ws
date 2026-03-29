import { Component, Input, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonAppearance, MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { RestModule } from '../rest/rest.module';
import { appendSlash, getAppUrl } from '../util/url.util';


@Component({
    selector: 'mlws-main-menu-item', imports: [CommonModule, RestModule, MatButtonModule, MatMenuModule, MatIconModule],
    templateUrl: './main-menu-item.component.html',
    styleUrls: ['./main-menu-item.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class MainMenuItemComponent {

    @Input()
    public id?: string;

    @Input()
    public name?: string;

    @Input()
    public subIds?: string[];

    protected readonly appUrl: string;

    protected get matButtonAppearance(): MatButtonAppearance {
        const href = appendSlash(location.href);
        let id = href.substring(href.indexOf("#") + 1);
        while (id.startsWith("/")) {
            id = id.substring(1);
        }
        while (id.endsWith("/")) {
            id = id.substring(0, id.length - 1);
        }
        const qmIndex = id.indexOf("?");
        if (qmIndex >= 0) {
            id = id.substring(0, qmIndex);
        }
        if (id === this.id) {
            return "filled";
        } else {
            if (this.subIds && this.subIds.includes(id)) {
                return "filled";
            }
            return "elevated";
        }
    }

    public constructor() {
        this.appUrl = getAppUrl();
    }

    protected onClick($event: any): void {
        console.info('onClick', $event);
    }
}