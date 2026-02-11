import { AccountStatus } from './account.model';
import { TransactionStatus } from './transaction.model';

export interface AccountCreateRequest {
    holderName: string;
    openingBalance: number;
}

export interface AccountUpdateRequest {
    holderName?: string;
    dailyLimit?: number;
}

export interface TransactionFilter {
    accountId?: string;
    status?: TransactionStatus;
    minAmount?: number;
    maxAmount?: number;
    fromDate?: string;
    toDate?: string;
}

export interface TransactionResponse {
    id: string;
    idempotencyKey: string;
    fromAccountId: number;
    toAccountId: number;
    amount: number;
    currency: string;
    status: TransactionStatus;
    failureReason?: string;
    createdOn: string;
}

export interface TransferApprovalRequest {
    approved: boolean;
    rejectionReason?: string;
}

// User Management Interfaces
export interface UserCreateRequest {
    username: string;
    password: string;
    fullName: string;
    email: string;
    roles?: string[];
}

export interface UserUpdateRequest {
    fullName?: string;
    email?: string;
    roles?: string[];
    enabled?: boolean;
}

export interface UserResponse {
    id: number;
    username: string;
    fullName: string;
    email: string;
    roles: string[];
    enabled: boolean;
}
