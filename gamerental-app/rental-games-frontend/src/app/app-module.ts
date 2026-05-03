import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { DatePipe } from '@angular/common';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { jwtInterceptor } from './core/auth/jwt.interceptor';

import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { BadgeComponent } from './shared/components/badge/badge.component';
import { ConfirmModalComponent } from './shared/components/confirm-modal/confirm-modal.component';

import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';

import { GameListComponent } from './features/games/game-list/game-list.component';
import { GameDetailComponent } from './features/games/game-detail/game-detail.component';
import { GameFormComponent } from './features/games/game-form/game-form.component';

import { ClientListComponent } from './features/clients/client-list/client-list.component';
import { ClientDetailComponent } from './features/clients/client-detail/client-detail.component';
import { ClientFormComponent } from './features/clients/client-form/client-form.component';

import { RentalListComponent } from './features/rentals/rental-list/rental-list.component';
import { IssueGameComponent } from './features/rentals/issue-game/issue-game.component';
import { ReturnGameComponent } from './features/rentals/return-game/return-game.component';

@NgModule({
  declarations: [
    App,
    NavbarComponent,
    BadgeComponent,
    ConfirmModalComponent,
    LoginComponent,
    RegisterComponent,
    GameListComponent,
    GameDetailComponent,
    GameFormComponent,
    ClientListComponent,
    ClientDetailComponent,
    ClientFormComponent,
    RentalListComponent,
    IssueGameComponent,
    ReturnGameComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    DatePipe
  ],
  bootstrap: [App]
})
export class AppModule { }
