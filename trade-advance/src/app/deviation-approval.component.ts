import { Component, OnInit, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import template from './deviation-approval.component.html?raw';
import styles from './deviation-approval.component.css?raw';

export interface DeviationView {
  applicationId: number;
  dealerName: string;
  applicationDate: string;
  requestedAmount: number;
  deviationCode: string;
  deviationReason: string;
  deviationStatus: string;
  createdDate: string;
}

@Component({
  selector: 'app-deviation-approval',
  template,
  styles: [styles],
})
export class DeviationApprovalComponent implements OnInit {
  private readonly http = inject(HttpClient);
  readonly api = '/api';

  loading = false;
  error = '';
  items: DeviationView[] = [];
  remarksByApplication: Record<number, string> = {};
  busyApplicationId: number | null = null;
  message = '';

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.error = '';
    this.http.get<DeviationView[]>(`${this.api}/admin/deviations`).subscribe({
      next: (items) => {
        this.items = items;
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load pending deviations.';
        this.loading = false;
      },
    });
  }

  approve(item: DeviationView): void {
    this.decide(item, 'approve');
  }

  reject(item: DeviationView): void {
    this.decide(item, 'reject');
  }

  private decide(item: DeviationView, action: 'approve' | 'reject'): void {
    this.busyApplicationId = item.applicationId;
    this.message = '';
    this.error = '';
    const remarks = this.remarksByApplication[item.applicationId] || '';
    this.http
      .post(`${this.api}/admin/deviations/${item.applicationId}/${action}`, { remarks })
      .subscribe({
        next: () => {
          this.busyApplicationId = null;
          this.message = `Application #${item.applicationId} ${action === 'approve' ? 'approved' : 'rejected'}.`;
          this.refresh();
        },
        error: (err: HttpErrorResponse) => {
          this.busyApplicationId = null;
          this.error = err.error?.message || 'This application may have already been processed.';
          this.refresh();
        },
      });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(value || 0);
  }
}
