import { Component, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { WarehouseSelectorComponent } from './warehouse-selector.component';
import { WarehouseSelectorService } from '../service/warehouse-selector.service';
import { Router } from '@angular/router';
import { getAppUrl } from '../util/url.util';


@Component({
    selector: 'mlws-main-menu', //
    imports: [CommonModule, MatButtonModule, MatMenuModule, MatIconModule, WarehouseSelectorComponent],
    templateUrl: './main-menu.component.html',
    styleUrls: ['./main-menu.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class MainMenuComponent {
    protected readonly warehouseSelectorService = inject(WarehouseSelectorService);
    protected readonly router = inject(Router);
    protected readonly appUrl: string;

    public constructor() {
        this.appUrl = getAppUrl();
    }

    protected onClick(id: string): void {
        console.info(`onClick: id=${id}`);
        if (id === undefined || id === null) {
            throw new Error('id is undefined/null!');
        }
        const url = this.appUrl + id;
        this.router.navigate([id]);
    }
}