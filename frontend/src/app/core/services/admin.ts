import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
    AccountCreateRequest,
    AccountUpdateRequest,
    TransactionFilter,
    TransactionResponse,
    TransferApprovalRequest,
    UserCreateRequest,
    UserUpdateRequest,
    UserResponse
} from '../models/admin.model';
import { AccountResponse } from '../models/account.model';
import { Page } from '../models/transaction.model';

@Injectable({
    providedIn: 'root'
})
export class AdminService {

    constructor(private http: HttpClient) { }

    // Account Management
    createAccount(request: AccountCreateRequest): Observable<AccountResponse> {
        return this.http.post<AccountResponse>(`${environment.apiUrl}/admin/accounts`, request);
    }

    updateAccount(id: string, request: AccountUpdateRequest): Observable<AccountResponse> {
        return this.http.put<AccountResponse>(`${environment.apiUrl}/admin/accounts/${id}`, request);
    }


    deleteAccount(id: string): Observable<void> {
        return this.http.delete<void>(`${environment.apiUrl}/admin/accounts/${id}`);
    }

    // Transaction Management
    searchTransactions(filter: TransactionFilter, page: number = 0, size: number = 20): Observable<Page<TransactionResponse>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (filter.accountId) params = params.set('accountId', filter.accountId);
        if (filter.status) params = params.set('status', filter.status);
        if (filter.minAmount) params = params.set('minAmount', filter.minAmount.toString());
        if (filter.maxAmount) params = params.set('maxAmount', filter.maxAmount.toString());
        if (filter.fromDate) params = params.set('fromDate', filter.fromDate);
        if (filter.toDate) params = params.set('toDate', filter.toDate);

        return this.http.get<Page<TransactionResponse>>(`${environment.apiUrl}/admin/transactions`, { params });
    }

    reverseTransaction(id: string, reason?: string): Observable<void> {
        return this.http.post<void>(`${environment.apiUrl}/admin/transactions/${id}/reverse`, reason || '');
    }

    setGlobalLimit(limit: number): Observable<void> {
        return this.http.post<void>(`${environment.apiUrl}/admin/transactions/limits/global`, limit);
    }

    getGlobalLimit(): Observable<number> {
        return this.http.get<number>(`${environment.apiUrl}/admin/transactions/limits/global`);
    }

    // Transfer Approval
    processApproval(id: number, request: TransferApprovalRequest): Observable<void> {
        return this.http.post<void>(`${environment.apiUrl}/admin/transfers/approvals/${id}`, request);
    }

    // User Management
    createUser(request: UserCreateRequest): Observable<UserResponse> {
        return this.http.post<UserResponse>(`${environment.apiUrl}/admin/users`, request);
    }

    getAllUsers(): Observable<UserResponse[]> {
        return this.http.get<UserResponse[]>(`${environment.apiUrl}/admin/users`);
    }

    getUserById(id: number): Observable<UserResponse> {
        return this.http.get<UserResponse>(`${environment.apiUrl}/admin/users/${id}`);
    }

    updateUser(id: number, request: UserUpdateRequest): Observable<UserResponse> {
        return this.http.put<UserResponse>(`${environment.apiUrl}/admin/users/${id}`, request);
    }

    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${environment.apiUrl}/admin/users/${id}`);
    }
}
