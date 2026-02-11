import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService } from '../../../core/services/admin';
import { Navbar } from '../../../shared/components/navbar/navbar';

@Component({
    selector: 'app-admin-settings',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        Navbar
    ],
    templateUrl: './admin-settings.html',
    styleUrl: './admin-settings.scss'
})
export class AdminSettings implements OnInit {
    limitForm: FormGroup;
    currentLimit: number = 0;
    loading = false;
    successMessage = '';
    errorMessage = '';

    constructor(
        private fb: FormBuilder,
        private adminService: AdminService
    ) {
        this.limitForm = this.fb.group({
            globalLimit: [0, [Validators.required, Validators.min(0)]]
        });
    }

    ngOnInit(): void {
        this.loadCurrentLimit();
    }

    loadCurrentLimit(): void {
        this.loading = true;
        this.adminService.getGlobalLimit().subscribe({
            next: (limit) => {
                this.currentLimit = limit;
                this.limitForm.patchValue({ globalLimit: limit });
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = 'Failed to load current limit';
                this.loading = false;
                console.error('Error loading limit:', error);
            }
        });
    }

    onUpdateLimit(): void {
        if (this.limitForm.invalid) {
            return;
        }

        this.loading = true;
        this.errorMessage = '';
        this.successMessage = '';

        const newLimit = parseFloat(this.limitForm.value.globalLimit);

        this.adminService.setGlobalLimit(newLimit).subscribe({
            next: () => {
                this.loading = false;
                this.currentLimit = newLimit;
                this.successMessage = `Global transfer limit updated to ${this.formatCurrency(newLimit)}`;
            },
            error: (error) => {
                this.loading = false;
                this.errorMessage = error.error?.message || 'Failed to update global limit';
            }
        });
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    }

    clearMessages(): void {
        this.errorMessage = '';
        this.successMessage = '';
    }
}
