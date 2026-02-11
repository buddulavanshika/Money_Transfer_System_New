import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user';
import { AccountService } from '../../core/services/account';
import { Navbar } from '../../shared/components/navbar/navbar';
import { TransactionLogResponse, Page } from '../../core/models/transaction.model';
import { TransactionStatus } from '../../core/models/transfer.model';
import { AccountResponse } from '../../core/models/account.model';

interface TransactionDisplay extends TransactionLogResponse {
  type: 'DEBIT' | 'CREDIT';
}

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    Navbar
  ],
  templateUrl: './history.html',
  styleUrl: './history.scss'
})
export class History implements OnInit {
  transactions: TransactionDisplay[] = [];
  displayedColumns: string[] = ['date', 'type', 'account', 'amount', 'currency', 'status'];
  loading = true;
  errorMessage = '';

  userAccounts: AccountResponse[] = [];
  selectedAccountId: string | null = null;

  // Pagination
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;

  // Filters
  filterForm: FormGroup;
  statusOptions = Object.values(TransactionStatus);
  directionOptions = ['ALL', 'SENT', 'RECEIVED'];

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private accountService: AccountService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      status: [''],
      direction: ['ALL'],
      fromDate: [''],
      toDate: ['']
    });
  }

  ngOnInit(): void {
    this.loadUserAccounts();

    // Check for accountId in query params
    this.route.queryParams.subscribe(params => {
      if (params['accountId']) {
        this.selectedAccountId = params['accountId'];
        this.loadTransactions();
      }
    });
  }

  loadUserAccounts(): void {
    this.userService.getMyAccounts().subscribe({
      next: (accounts) => {
        this.userAccounts = accounts;
        if (!this.selectedAccountId && accounts.length > 0) {
          this.selectedAccountId = accounts[0].id;
          this.loadTransactions();
        }
      },
      error: (error) => {
        console.error('Error loading accounts:', error);
        this.errorMessage = 'Failed to load accounts';
      }
    });
  }

  loadTransactions(): void {
    if (!this.selectedAccountId) {
      return;
    }

    this.loading = true;
    const filters = this.filterForm.value;

    this.accountService.getTransactions(
      this.selectedAccountId,
      filters.fromDate || undefined,
      filters.toDate || undefined,
      filters.status || undefined,
      filters.direction || 'ALL',
      this.pageIndex,
      this.pageSize
    ).subscribe({
      next: (page: Page<TransactionLogResponse>) => {
        this.transactions = this.processTransactions(page.content);
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load transaction history';
        this.loading = false;
        console.error('Error loading transactions:', error);
      }
    });
  }

  processTransactions(transactions: TransactionLogResponse[]): TransactionDisplay[] {
    return transactions.map(txn => {
      const isDebit = txn.fromAccountId.toString() === this.selectedAccountId;
      const type: 'DEBIT' | 'CREDIT' = isDebit ? 'DEBIT' : 'CREDIT';
      return {
        ...txn,
        type: type
      };
    });
  }

  onAccountChange(): void {
    this.pageIndex = 0;
    this.loadTransactions();
  }

  onFilterChange(): void {
    this.pageIndex = 0;
    this.loadTransactions();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTransactions();
  }

  clearFilters(): void {
    this.filterForm.reset({ direction: 'ALL' });
    this.pageIndex = 0;
    this.loadTransactions();
  }

  getDebitCount(): number {
    return this.transactions.filter(t => t.type === 'DEBIT').length;
  }

  getCreditCount(): number {
    return this.transactions.filter(t => t.type === 'CREDIT').length;
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

  formatCurrency(amount: number, currency?: string): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD'
    }).format(amount);
  }

  getOtherAccountId(transaction: TransactionDisplay): string {
    return transaction.type === 'DEBIT'
      ? transaction.toAccountId.toString()
      : transaction.fromAccountId.toString();
  }

  refresh(): void {
    this.loadTransactions();
  }
}