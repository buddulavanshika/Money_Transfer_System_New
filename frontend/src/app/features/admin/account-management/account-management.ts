import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { AdminService } from '../../../core/services/admin';
import { Navbar } from '../../../shared/components/navbar/navbar';
import { AccountResponse, AccountStatus } from '../../../core/models/account.model';
import { AccountCreateRequest, AccountUpdateRequest } from '../../../core/models/admin.model';

@Component({
    selector: 'app-account-management',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatCardModule,
        MatTableModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatDialogModule,
        Navbar
    ],
    templateUrl: './account-management.html',
    styleUrl: './account-management.scss'
})
export class AccountManagement implements OnInit {
    createForm: FormGroup;
    updateForm: FormGroup;
    selectedAccount: AccountResponse | null = null;
    loading = false;
    successMessage = '';
    errorMessage = '';

    accountStatuses = Object.values(AccountStatus);

    constructor(
        private fb: FormBuilder,
        private adminService: AdminService,
        private dialog: MatDialog
    ) {
        this.createForm = this.fb.group({
            holderName: ['', [Validators.required]],
            openingBalance: [0, [Validators.required, Validators.min(0)]]
        });

        this.updateForm = this.fb.group({
            holderName: [''],
            dailyLimit: [null, [Validators.min(0)]]
        });
    }

    ngOnInit(): void { }

    onCreateAccount(): void {
        if (this.createForm.invalid) {
            return;
        }

        this.loading = true;
        this.errorMessage = '';
        this.successMessage = '';

        const request: AccountCreateRequest = {
            holderName: this.createForm.value.holderName,
            openingBalance: parseFloat(this.createForm.value.openingBalance)
        };

        this.adminService.createAccount(request).subscribe({
            next: (account) => {
                this.loading = false;
                this.successMessage = `Account created successfully! Account ID: ${account.id}`;
                this.createForm.reset();
            },
            error: (error) => {
                this.loading = false;
                this.errorMessage = error.error?.message || 'Failed to create account';
            }
        });
    }

    onUpdateAccount(accountId: string): void {
        if (this.updateForm.invalid) {
            return;
        }

        this.loading = true;
        this.errorMessage = '';
        this.successMessage = '';

        const request: AccountUpdateRequest = {
            holderName: this.updateForm.value.holderName || undefined,
            dailyLimit: this.updateForm.value.dailyLimit || undefined
        };

        this.adminService.updateAccount(accountId, request).subscribe({
            next: (account) => {
                this.loading = false;
                this.successMessage = `Account ${account.id} updated successfully!`;
                this.updateForm.reset();
            },
            error: (error) => {
                this.loading = false;
                this.errorMessage = error.error?.message || 'Failed to update account';
            }
        });
    }


    onDeleteAccount(accountId: string): void {
        if (!confirm('Are you sure you want to delete this account? This action cannot be undone.')) {
            return;
        }

        this.loading = true;
        this.errorMessage = '';
        this.successMessage = '';

        this.adminService.deleteAccount(accountId).subscribe({
            next: () => {
                this.loading = false;
                this.successMessage = `Account ${accountId} deleted successfully`;
            },
            error: (error) => {
                this.loading = false;
                this.errorMessage = error.error?.message || 'Failed to delete account';
            }
        });
    }

    clearMessages(): void {
        this.errorMessage = '';
        this.successMessage = '';
    }
}
