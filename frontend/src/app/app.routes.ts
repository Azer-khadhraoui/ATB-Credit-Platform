import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { LayoutComponent } from './core/layout/layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { UsersComponent } from './users/users.component';
import { UserFormComponent } from './users/user-form/user-form.component';
import { ClientsComponent } from './clients/clients.component';
import { ClientFormComponent } from './clients/client-form/client-form.component';
import { CreditFileListComponent } from './credit-files/credit-file-list/credit-file-list.component';
import { CreditFileFormComponent } from './credit-files/credit-file-form/credit-file-form.component';
import { AuditComponent } from './audit/audit.component';
import { SettingsComponent } from './shared/settings/settings.component';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  {
    path: 'app',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users', component: UsersComponent },
      { path: 'users/:id/edit', component: UserFormComponent },
      { path: 'clients', component: ClientsComponent },
      { path: 'clients/new', component: ClientFormComponent, data: { visualImage: '/images/crudclientimage.png' } },
      { path: 'credit-files', component: CreditFileListComponent },
      { path: 'credit-files/new', component: CreditFileFormComponent, data: { visualImage: '/images/imagecrud1.png' } },
      { path: 'audit', component: AuditComponent },
      { path: 'settings', component: SettingsComponent }
    ]
  }
];
