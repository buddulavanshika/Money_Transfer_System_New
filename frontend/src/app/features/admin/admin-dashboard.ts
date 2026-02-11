import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Navbar } from '../../shared/components/navbar/navbar';
import { AdminService } from '../../core/services/admin';

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
    globalLimit: number | null = null;
    loading = true;
    errorMessage = '';

    constructor(
        private adminService: AdminService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadGlobalLimit();
    }

    loadGlobalLimit(): void {
        this.loading = true;
        this.adminService.getGlobalLimit().subscribe({
            next: (limit) => {
                this.globalLimit = limit;
                this.loading = false;
            },
            error: (error) => {
                console.error('Error loading global limit:', error);
                this.errorMessage = 'Failed to load global limit';
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

    navigateToApprovals(): void {
        this.router.navigate(['/admin/approvals']);
    }

    navigateToLimits(): void {
        this.router.navigate(['/admin/limits']);
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    }
}
