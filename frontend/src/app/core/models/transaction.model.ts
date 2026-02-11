export interface Transaction {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  currency: string;
  status: TransactionStatus;
  failureReason?: string;
  idempotencyKey: string;
  createdOn: string;
  type?: 'DEBIT' | 'CREDIT';
}

export enum TransactionStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  PENDING = 'PENDING',
  REVERSED = 'REVERSED'
}

export interface TransactionLogResponse {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  currency: string;
  status: TransactionStatus;
  failureReason?: string;
  idempotencyKey: string;
  createdOn: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}