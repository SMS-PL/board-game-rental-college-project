import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
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

const routes: Routes = [
  { path: '', redirectTo: 'games', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'games', component: GameListComponent, canActivate: [authGuard] },
  { path: 'games/new', component: GameFormComponent, canActivate: [authGuard] },
  { path: 'games/:id', component: GameDetailComponent, canActivate: [authGuard] },
  { path: 'games/:id/edit', component: GameFormComponent, canActivate: [authGuard] },
  { path: 'clients', component: ClientListComponent, canActivate: [authGuard] },
  { path: 'clients/new', component: ClientFormComponent, canActivate: [authGuard] },
  { path: 'clients/:id', component: ClientDetailComponent, canActivate: [authGuard] },
  { path: 'clients/:id/edit', component: ClientFormComponent, canActivate: [authGuard] },
  { path: 'rentals', component: RentalListComponent, canActivate: [authGuard] },
  { path: 'rentals/issue', component: IssueGameComponent, canActivate: [authGuard] },
  { path: 'rentals/return', component: IssueGameComponent, canActivate: [authGuard] },
  { path: 'rentals/:id/return', component: ReturnGameComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'games' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
