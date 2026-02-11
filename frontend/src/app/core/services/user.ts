import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserProfileResponse, ProfileUpdateRequest, ChangePasswordRequest } from '../models/user.model';
import { AccountResponse } from '../models/account.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private http: HttpClient) { }

    getProfile(): Observable<UserProfileResponse> {
        return this.http.get<UserProfileResponse>(`${environment.apiUrl}/user/profile`);
    }

    updateProfile(request: ProfileUpdateRequest): Observable<UserProfileResponse> {
        return this.http.put<UserProfileResponse>(`${environment.apiUrl}/user/profile`, request);
    }

    changePassword(request: ChangePasswordRequest): Observable<void> {
        return this.http.put<void>(`${environment.apiUrl}/user/password`, request);
    }

    getMyAccounts(): Observable<AccountResponse[]> {
        return this.http.get<AccountResponse[]>(`${environment.apiUrl}/user/accounts`);
    }
}
