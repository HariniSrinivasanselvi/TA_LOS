import { Component, OnInit, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import template from './user-management.component.html?raw';
import styles from './user-management.component.css?raw';
import { ADMIN_ROLE, DEALER_ROLE } from './auth.service';

export interface UserView {
  userId: number;
  displayName: string;
  email: string;
  roleCode: string | null;
  roleName: string;
}

@Component({
  selector: 'app-user-management',
  template,
  styles: [styles],
})
export class UserManagementComponent implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = '/api';
  readonly dealerRole = DEALER_ROLE;
  readonly adminRole = ADMIN_ROLE;

  loading = false;
  error = '';
  message = '';
  users: UserView[] = [];
  busyUserId: number | null = null;

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.error = '';
    this.http.get<UserView[]>(`${this.api}/admin/users`).subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load users.';
        this.loading = false;
      },
    });
  }

  changeRole(user: UserView, roleCode: string): void {
    if (roleCode === user.roleCode) return;
    this.busyUserId = user.userId;
    this.error = '';
    this.message = '';
    this.http.post<UserView>(`${this.api}/admin/users/${user.userId}/role`, { roleCode }).subscribe({
      next: (updated) => {
        this.busyUserId = null;
        this.message = `${updated.displayName} is now ${updated.roleName}.`;
        this.refresh();
      },
      error: (err: HttpErrorResponse) => {
        this.busyUserId = null;
        this.error = err.error?.message || 'Could not update this user\u2019s role.';
        this.refresh();
      },
    });
  }
}
