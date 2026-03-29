import { Component, inject, ViewEncapsulation } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MlwsErrorHandler } from "../service/mlws-error-handler";
import {MatButton, MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";

@Component({
    selector: 'mlws-error',
    imports: [CommonModule, MatButtonModule, MatIconModule],
    templateUrl: './error.component.html',
    styleUrls: ['./error.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class ErrorComponent {

	protected readonly errorHandler = inject(MlwsErrorHandler);

    protected onClearClick(): void {
        this.errorHandler.clear();
    }
}