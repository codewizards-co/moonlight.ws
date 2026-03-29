import {Component} from '@angular/core';
import {RouterModule} from '@angular/router';
import {MainMenuComponent} from '../component/main-menu.component';
import { ErrorComponent } from '../component/error.component';

@Component({
    imports: [RouterModule, MainMenuComponent, ErrorComponent],
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss',
})
export class AppComponent {
}
