import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { AdminService } from '../../../core/services/admin';
import { Navbar } from '../../../shared/components/navbar/navbar';
import { TransactionResponse, TransactionFilter } from '../../../core/models/admin.model';
import { Page } from '../../../core/models/transaction.model';
import { TransactionStatus } from '../../../core/models/transfer.model';

@Component({
    selector: 'app-transaction-management',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatCardModule,
        MatTableModule,
        MatPaginatorModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatChipsModule,
        Navbar
    ],
    templateUrl: './transaction-management.html',
    styleUrl: './transaction-management.scss'
})
export class TransactionManagement implements OnInit {
    filterForm: FormGroup;
    transactions: TransactionResponse[] = [];
    displayedColumns: string[] = ['id', 'from', 'to', 'amount', 'currency', 'status', 'date', 'actions'];
    loading = false;
    successMessage = '';
    errorMessage = '';

    // Pagination
    totalElements = 0;
    pageSize = 20;
    pageIndex = 0;

    statusOptions = Object.values(TransactionStatus);

    constructor(
        private fb: FormBuilder,
        private adminService: AdminService
    ) {
        this.filterForm = this.fb.group({
            accountId: [''],
            status: [''],
            fromDate: [''],
            toDate: [''],
            minAmount: [null],
            maxAmount: [null]
        });
    }

    ngOnInit(): void {
        this.searchTransactions();
    }

    searchTransactions(): void {
        this.loading = true;
        this.errorMessage = '';

        const formValue = this.filterForm.value;
        const filter: TransactionFilter = {
            accountId: formValue.accountId || undefined,
            status: formValue.status || undefined,
            fromDate: formValue.fromDate || undefined,
            toDate: formValue.toDate || undefined,
            minAmount: formValue.minAmount || undefined,
            maxAmount: formValue.maxAmount || undefined
        };

        this.adminService.searchTransactions(filter, this.pageIndex, this.pageSize).subscribe({
            next: (page: Page<TransactionResponse>) => {
                this.transactions = page.content;
                this.totalElements = page.totalElements;
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = 'Failed to load transactions';
                this.loading = false;
                console.error('Error loading transactions:', error);
            }
        });
    }

    onFilterChange(): void {
        this.pageIndex = 0;
        this.searchTransactions();
    }

    onPageChange(event: PageEvent): void {
        this.pageIndex = event.pageIndex;
        this.pageSize = event.pageSize;
        this.searchTransactions();
    }

    clearFilters(): void {
        this.filterForm.reset();
        this.pageIndex = 0;
        this.searchTransactions();
    }

    reverseTransaction(transactionId: string): void {
        if (!confirm('Are you sure you want to reverse this transaction? This action cannot be undone.')) {
            return;
        }

        this.loading = true;
        this.errorMessage = '';
        this.successMessage = '';

        this.adminService.reverseTransaction(transactionId).subscribe({
            next: (response) => {
                this.loading = false;
                this.successMessage = `Transaction ${transactionId} reversed successfully`;
                this.searchTransactions(); // Refresh the list
            },
            error: (error) => {
                this.loading = false;
                this.errorMessage = error.error?.message || 'Failed to reverse transaction';
            }
        });
    }

    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    }

    formatCurrency(amount: number, currency: string): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency || 'USD'
        }).format(amount);
    }

    clearMessages(): void {
        this.errorMessage = '';
        this.successMessage = '';
    }
}
