export interface Account {
  id: string;
  holderName: string;
  balance: number;
  status: AccountStatus;
  dailyLimit?: number;
  lastUpdated?: string;
}

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  LOCKED = 'LOCKED',
  CLOSED = 'CLOSED'
}

export interface AccountResponse {
  id: string;
  holderName: string;
  balance: number;
  status: AccountStatus;
  dailyLimit?: number;
  lastUpdated?: string;
}