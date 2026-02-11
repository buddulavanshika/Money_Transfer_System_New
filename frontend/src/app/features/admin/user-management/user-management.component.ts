import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin';
import { UserCreateRequest, UserResponse, UserUpdateRequest } from '../../../core/models/admin.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  users: UserResponse[] = [];
  createUserForm: FormGroup;
  editUserForm: FormGroup;
  showCreateForm = false;
  showEditForm = false;
  selectedUser: UserResponse | null = null;
  loading = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.createUserForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      roles: [['USER']]
    });

    this.editUserForm = this.fb.group({
      fullName: ['', [Validators.minLength(3), Validators.maxLength(100)]],
      email: ['', [Validators.email]],
      roles: [[]],
      enabled: [true]
    });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load users: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (this.showCreateForm) {
      this.showEditForm = false;
      this.createUserForm.reset({ roles: ['USER'] });
      this.error = null;
      this.success = null;
    }
  }

  createUser(): void {
    if (this.createUserForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = null;
    const request: UserCreateRequest = this.createUserForm.value;

    this.adminService.createUser(request).subscribe({
      next: (user) => {
        this.success = `User "${user.username}" created successfully!`;
        this.createUserForm.reset({ roles: ['USER'] });
        this.showCreateForm = false;
        this.loadUsers();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = 'Failed to create user: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  editUser(user: UserResponse): void {
    this.selectedUser = user;
    this.showEditForm = true;
    this.showCreateForm = false;
    this.error = null;
    this.success = null;

    this.editUserForm.patchValue({
      fullName: user.fullName,
      email: user.email,
      roles: user.roles,
      enabled: user.enabled
    });
  }

  updateUser(): void {
    if (this.editUserForm.invalid || !this.selectedUser) {
      return;
    }

    this.loading = true;
    this.error = null;
    const request: UserUpdateRequest = this.editUserForm.value;

    this.adminService.updateUser(this.selectedUser.id, request).subscribe({
      next: (user) => {
        this.success = `User "${user.username}" updated successfully!`;
        this.showEditForm = false;
        this.selectedUser = null;
        this.loadUsers();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = 'Failed to update user: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  deleteUser(user: UserResponse): void {
    if (!confirm(`Are you sure you want to delete user "${user.username}"? This will disable their account.`)) {
      return;
    }

    this.loading = true;
    this.error = null;

    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        this.success = `User "${user.username}" deleted successfully!`;
        this.loadUsers();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = 'Failed to delete user: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  cancelEdit(): void {
    this.showEditForm = false;
    this.selectedUser = null;
    this.editUserForm.reset();
    this.error = null;
  }

  toggleRole(role: string, formGroup: FormGroup): void {
    const roles = formGroup.get('roles')?.value || [];
    const index = roles.indexOf(role);

    if (index > -1) {
      roles.splice(index, 1);
    } else {
      roles.push(role);
    }

    formGroup.patchValue({ roles });
  }

  hasRole(role: string, formGroup: FormGroup): boolean {
    const roles = formGroup.get('roles')?.value || [];
    return roles.includes(role);
  }
}
