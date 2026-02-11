export interface UserProfileResponse {
    id: number;
    username: string;
    fullName: string;
    email: string;
    roles: string[];
}

export interface ProfileUpdateRequest {
    fullName?: string;
    email?: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}
