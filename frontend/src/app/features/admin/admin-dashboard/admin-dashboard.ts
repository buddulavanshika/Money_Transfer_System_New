import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService } from '../../../core/services/admin';
import { UserService } from '../../../core/services/user';
import { Navbar } from '../../../shared/components/navbar/navbar';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        Navbar
    ],
    templateUrl: './admin-dashboard.html',
    styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard implements OnInit {
    loading = true;
    globalLimit: number = 0;

    constructor(
        private adminService: AdminService,
        private userService: UserService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadDashboardData();
    }

    loadDashboardData(): void {
        this.adminService.getGlobalLimit().subscribe({
            next: (limit) => {
                this.globalLimit = limit;
                this.loading = false;
            },
            error: (error) => {
                console.error('Error loading dashboard data:', error);
                this.loading = false;
            }
        });
    }

    navigateToAccounts(): void {
        this.router.navigate(['/admin/accounts']);
    }

    navigateToTransactions(): void {
        this.router.navigate(['/admin/transactions']);
    }

    navigateToUsers(): void {
        this.router.navigate(['/admin/users']);
    }

    navigateToSettings(): void {
        this.router.navigate(['/admin/settings']);
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    }
}
