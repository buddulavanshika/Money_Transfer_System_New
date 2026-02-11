import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin.guard';
import { Login } from './features/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { Transfer } from './features/transfer/transfer';
import { History } from './features/history/history';
import { AdminDashboard } from './features/admin/admin-dashboard';
import { AccountManagement } from './features/admin/account-management/account-management';
import { TransactionManagement } from './features/admin/transaction-management/transaction-management';
import { AdminSettings } from './features/admin/admin-settings/admin-settings';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'transfer', component: Transfer, canActivate: [authGuard] },
  { path: 'history', component: History, canActivate: [authGuard] },

  // Admin routes
  { path: 'admin', component: AdminDashboard, canActivate: [authGuard, adminGuard] },
  { path: 'admin/accounts', component: AccountManagement, canActivate: [authGuard, adminGuard] },
  { path: 'admin/transactions', component: TransactionManagement, canActivate: [authGuard, adminGuard] },
  { path: 'admin/users', component: UserManagementComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/approvals', component: AdminSettings, canActivate: [authGuard, adminGuard] }, // Reusing settings for approvals
  { path: 'admin/limits', component: AdminSettings, canActivate: [authGuard, adminGuard] }, // Reusing settings for limits
  { path: 'admin/settings', component: AdminSettings, canActivate: [authGuard, adminGuard] },

  { path: '**', redirectTo: '/dashboard' }
];
