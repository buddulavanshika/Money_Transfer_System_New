import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth';
import { UserService } from '../../core/services/user';
import { Navbar } from '../../shared/components/navbar/navbar';
import { AccountResponse } from '../../core/models/account.model';
import { UserProfileResponse } from '../../core/models/user.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    Navbar
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  userProfile: UserProfileResponse | null = null;
  accounts: AccountResponse[] = [];
  loading = true;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadUserData();
  }

  loadUserData(): void {
    this.loading = true;

    // Load user profile and accounts
    this.userService.getProfile().subscribe({
      next: (profile) => {
        this.userProfile = profile;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load user profile';
        console.error('Error loading profile:', error);
      }
    });

    this.userService.getMyAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load accounts';
        this.loading = false;
        console.error('Error loading accounts:', error);
      }
    });
  }

  navigateToTransfer(): void {
    this.router.navigate(['/transfer']);
  }

  navigateToHistory(accountId: string): void {
    this.router.navigate(['/history'], { queryParams: { accountId } });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  refreshData(): void {
    this.loadUserData();
  }

  getTotalBalance(): number {
    return this.accounts.reduce((sum, account) => sum + account.balance, 0);
  }
}