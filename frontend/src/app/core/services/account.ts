import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AccountResponse } from '../models/account.model';
import { TransactionLogResponse, Page } from '../models/transaction.model';
import { TransactionStatus } from '../models/transfer.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  constructor(private http: HttpClient) { }

  getAccount(accountId: string): Observable<AccountResponse> {
    return this.http.get<AccountResponse>(`${environment.apiUrl}/accounts/${accountId}`);
  }

  getBalance(accountId: string): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/accounts/${accountId}/balance`);
  }

  getTransactions(
    accountId: string,
    from?: string,
    to?: string,
    status?: TransactionStatus,
    direction: 'ALL' | 'SENT' | 'RECEIVED' = 'ALL',
    page: number = 0,
    size: number = 20,
    sort: string = 'createdOn,desc'
  ): Observable<Page<TransactionLogResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort)
      .set('direction', direction);

    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    if (status) params = params.set('status', status);

    return this.http.get<Page<TransactionLogResponse>>(
      `${environment.apiUrl}/accounts/${accountId}/transactions`,
      { params }
    );
  }
}