import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth';
import { UserService } from '../../core/services/user';
import { TransferService } from '../../core/services/transfer';
import { Navbar } from '../../shared/components/navbar/navbar';
import { TransferRequest } from '../../core/models/transfer.model';
import { AccountResponse } from '../../core/models/account.model';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    Navbar
  ],
  templateUrl: './transfer.html',
  styleUrl: './transfer.scss'
})
export class Transfer implements OnInit {
  transferForm: FormGroup;
  userAccounts: AccountResponse[] = [];
  currentAccount: AccountResponse | null = null;
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private transferService: TransferService,
    private router: Router
  ) {
    this.transferForm = this.fb.group({
      sourceAccountId: ['', [Validators.required]],
      toAccountId: ['', [Validators.required]],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      currency: ['USD', [Validators.required]]
    });

    // Update currentAccount when sourceAccountId changes
    this.transferForm.get('sourceAccountId')?.valueChanges.subscribe(id => {
      this.currentAccount = this.userAccounts.find(acc => acc.id === id) || null;
    });
  }

  ngOnInit(): void {
    this.loadUserAccounts();
  }

  loadUserAccounts(): void {
    this.loading = true;
    this.userService.getMyAccounts().subscribe({
      next: (accounts) => {
        this.userAccounts = accounts.filter(acc => acc.status === 'ACTIVE');
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading accounts:', error);
        this.errorMessage = 'Failed to load your accounts';
        this.loading = false;
      }
    });
  }

  getSelectedAccount(): AccountResponse | null {
    const sourceId = this.transferForm.value.sourceAccountId;
    return this.userAccounts.find(acc => acc.id === sourceId) || null;
  }

  onSubmit(): void {
    if (this.transferForm.invalid) {
      return;
    }

    const formValue = this.transferForm.value;
    const selectedAccount = this.getSelectedAccount();

    // Validate not transferring to self
    if (formValue.sourceAccountId === formValue.destinationAccountId) {
      this.errorMessage = 'Cannot transfer to the same account';
      return;
    }

    // Validate sufficient balance
    if (selectedAccount && formValue.amount > selectedAccount.balance) {
      this.errorMessage = 'Insufficient balance for this transfer';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const transferRequest: TransferRequest = {
      sourceAccountId: formValue.sourceAccountId,
      destinationAccountId: formValue.toAccountId,
      amount: parseFloat(formValue.amount),
      currency: formValue.currency,
      idempotencyKey: this.transferService.generateIdempotencyKey()
    };

    this.transferService.transfer(transferRequest).subscribe({
      next: (response) => {
        this.submitting = false;
        this.successMessage = `Transfer successful! Transaction ID: ${response.transactionId}. ${this.formatCurrency(response.amount, response.currency)} sent to Account ${response.destinationAccountId}`;
        this.transferForm.reset({ currency: 'INR' });

        // Navigate to history after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/history'], {
            queryParams: { accountId: formValue.sourceAccountId }
          });
        }, 3000);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = error.error?.message || 'Transfer failed. Please try again.';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency
    }).format(amount);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}