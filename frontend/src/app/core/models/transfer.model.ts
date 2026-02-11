export interface TransferRequest {
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  currency?: string;
  idempotencyKey: string;
}

export interface TransferResponse {
  transactionId: string;
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  currency: string;
  status: TransactionStatus;
  message: string;
  idempotencyKey: string;
  createdOn: string;
}

export enum TransactionStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  PENDING = 'PENDING',
  REVERSED = 'REVERSED'
}