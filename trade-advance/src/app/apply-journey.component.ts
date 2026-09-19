import { Component, ElementRef, OnDestroy, ViewChild, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import template from './apply-journey.component.html?raw';
import styles from './apply-journey.component.css?raw';

type StageId = 'apply' | 'waiting' | 'vkyc' | 'processing' | 'notify';
type StageStatus = 'pending' | 'active' | 'done' | 'skipped' | 'failed';

interface TimelineStep {
  id: StageId;
  label: string;
  status: StageStatus;
}

interface ApplyInput {
  dealerCode: string;
  dealerName: string;
  dealerVintageMonths: number;
  lastThreeMonthAverageDisbursal: number;
  rcuFraudFlagged: boolean;
  requestedAmount: number;
}

interface VkycInput {
  dealerName: string;
  phoneNumber: string;
  email: string;
  aadhaar: string;
  pan: string;
}

interface ApplicationView {
  applicationId: number;
  dealerCode: string;
  dealerName: string;
  requestedAmount: number;
  approxApprovalAmount: number;
  eligible: boolean | null;
  status: string;
  statusLabel: string;
  deviationPopup: boolean;
  approvalTier: string;
  sanctionedAmount: number | null;
  scfReference: string | null;
  rejectionReason: string | null;
  createdDate: string;
}

interface VkycView {
  applicationId: number;
  dealerName: string;
  phoneMasked: string;
  emailMasked: string;
  aadhaarMasked: string;
  panMasked: string;
  vkycStatus: string;
  applicationStatus: string;
  applicationStatusLabel: string;
  rejectionReason: string | null;
  sanctionedAmount: number | null;
  scfReference: string | null;
}

const TERMINAL_STATUSES = new Set(['COMPLETED', 'FAILED', 'DEVIATION_REJECTED']);
const POLL_INTERVAL_MS = 3000;

@Component({
  selector: 'app-apply-journey',
  template,
  styles: [styles],
})
export class ApplyJourneyComponent implements OnDestroy {
  private readonly http = inject(HttpClient);
  readonly api = '/api';

  @ViewChild('cameraVideo') cameraVideoRef?: ElementRef<HTMLVideoElement>;
  @ViewChild('captureCanvas') captureCanvasRef?: ElementRef<HTMLCanvasElement>;

  stage: StageId = 'apply';
  application?: ApplicationView;
  submitError = '';
  submitLoading = false;
  pollHandle?: ReturnType<typeof window.setInterval>;

  showCreditPopup = false;

  cameraStream?: MediaStream;
  cameraError = '';
  capturedImage = '';
  vkycSubmitting = false;
  vkycError = '';
  vkycResult?: VkycView;

  timeline: TimelineStep[] = [
    { id: 'apply', label: 'Apply Now', status: 'active' },
    { id: 'waiting', label: 'Credit review', status: 'pending' },
    { id: 'vkyc', label: 'VKYC', status: 'pending' },
    { id: 'processing', label: 'Processing', status: 'pending' },
    { id: 'notify', label: 'Notify user', status: 'pending' },
  ];

  apply: ApplyInput = {
    dealerCode: 'DLR-1048',
    dealerName: 'Apex Auto Centre',
    dealerVintageMonths: 14,
    lastThreeMonthAverageDisbursal: 285000,
    rcuFraudFlagged: false,
    requestedAmount: 200000,
  };

  vkyc: VkycInput = {
    dealerName: this.apply.dealerName,
    phoneNumber: '',
    email: '',
    aadhaar: '',
    pan: '',
  };

  ngOnDestroy(): void {
    this.stopPolling();
    this.stopCamera();
  }

  private setStatus(id: StageId, status: StageStatus): void {
    const step = this.timeline.find((entry) => entry.id === id);
    if (step) step.status = status;
  }

  private goTo(stage: StageId): void {
    if (this.stage !== stage) this.setStatus(this.stage, 'done');
    this.stage = stage;
    this.setStatus(stage, 'active');
  }

  submitApplication(): void {
    this.submitLoading = true;
    this.submitError = '';
    this.vkyc.dealerName = this.apply.dealerName;

    this.http.post<ApplicationView>(`${this.api}/applications`, this.apply).subscribe({
      next: (application) => {
        this.submitLoading = false;
        this.application = application;
        this.routeFromApplication(application);
      },
      error: (err: HttpErrorResponse) => {
        this.submitLoading = false;
        this.submitError = err.error?.message || 'The application could not be submitted. Please try again.';
      },
    });
  }

  private routeFromApplication(application: ApplicationView): void {
    if (application.deviationPopup) {
      this.showCreditPopup = true;
      this.goTo('waiting');
      this.startPolling();
      return;
    }
    if (application.status === 'VKYC_PENDING') {
      this.goTo('vkyc');
      return;
    }
    this.applyTerminalStatus(application);
  }

  dismissCreditPopup(): void {
    this.showCreditPopup = false;
  }

  private startPolling(): void {
    this.stopPolling();
    this.pollHandle = window.setInterval(() => this.refreshApplication(), POLL_INTERVAL_MS);
  }

  private stopPolling(): void {
    if (this.pollHandle) {
      window.clearInterval(this.pollHandle);
      this.pollHandle = undefined;
    }
  }

  private refreshApplication(): void {
    if (!this.application) return;
    this.http.get<ApplicationView>(`${this.api}/applications/${this.application.applicationId}`).subscribe({
      next: (application) => {
        this.application = application;
        if (application.status !== 'DEVIATION_PENDING') {
          this.stopPolling();
          this.showCreditPopup = false;
          if (application.status === 'VKYC_PENDING') {
            this.goTo('vkyc');
          } else {
            this.applyTerminalStatus(application);
          }
        }
      },
      error: () => {
        // transient poll failure -- keep trying on the next interval
      },
    });
  }

  private applyTerminalStatus(application: ApplicationView): void {
    if (TERMINAL_STATUSES.has(application.status)) {
      this.setStatus('vkyc', this.stage === 'vkyc' ? 'skipped' : 'done');
      this.setStatus('processing', 'done');
      this.goTo('notify');
    }
  }

  // --- VKYC camera capture ---

  async startCamera(): Promise<void> {
    this.cameraError = '';
    this.capturedImage = '';
    try {
      this.cameraStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false });
      if (this.cameraVideoRef) {
        this.cameraVideoRef.nativeElement.srcObject = this.cameraStream;
        await this.cameraVideoRef.nativeElement.play();
      }
    } catch (err) {
      this.cameraError = 'Camera access was denied or is unavailable. Please allow camera access to continue.';
    }
  }

  capturePhoto(): void {
    const video = this.cameraVideoRef?.nativeElement;
    const canvas = this.captureCanvasRef?.nativeElement;
    if (!video || !canvas) return;
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    this.capturedImage = canvas.toDataURL('image/jpeg', 0.85);
    this.stopCamera();
  }

  retakePhoto(): void {
    this.capturedImage = '';
    this.startCamera();
  }

  private stopCamera(): void {
    this.cameraStream?.getTracks().forEach((track) => track.stop());
    this.cameraStream = undefined;
  }

  submitVkyc(): void {
    if (!this.application || !this.capturedImage) return;
    this.vkycSubmitting = true;
    this.vkycError = '';
    this.http
      .post<VkycView>(`${this.api}/applications/${this.application.applicationId}/vkyc`, {
        dealerName: this.vkyc.dealerName,
        phoneNumber: this.vkyc.phoneNumber,
        email: this.vkyc.email,
        aadhaar: this.vkyc.aadhaar,
        pan: this.vkyc.pan,
        imageDataUrl: this.capturedImage,
      })
      .subscribe({
        next: (result) => {
          this.vkycSubmitting = false;
          this.vkycResult = result;
          this.setStatus('vkyc', 'done');
          this.setStatus('processing', 'done');
          this.goTo('notify');
        },
        error: (err: HttpErrorResponse) => {
          this.vkycSubmitting = false;
          this.vkycError = err.error?.message || 'VKYC submission failed. Please try again.';
        },
      });
  }

  get finalStatusLabel(): string {
    if (this.vkycResult) return this.vkycResult.applicationStatusLabel;
    return this.application?.statusLabel || '';
  }

  get finalRejected(): boolean {
    const status = this.vkycResult?.applicationStatus || this.application?.status;
    return status === 'FAILED' || status === 'DEVIATION_REJECTED';
  }

  get finalRejectionReason(): string {
    return this.vkycResult?.rejectionReason || this.application?.rejectionReason || '';
  }

  get finalSanctionedAmount(): number {
    return this.vkycResult?.sanctionedAmount || this.application?.sanctionedAmount || 0;
  }

  get finalScfReference(): string {
    return this.vkycResult?.scfReference || this.application?.scfReference || '';
  }

  restart(): void {
    this.stopPolling();
    this.stopCamera();
    this.stage = 'apply';
    this.application = undefined;
    this.submitError = '';
    this.showCreditPopup = false;
    this.capturedImage = '';
    this.cameraError = '';
    this.vkycError = '';
    this.vkycResult = undefined;
    this.vkyc = { dealerName: this.apply.dealerName, phoneNumber: '', email: '', aadhaar: '', pan: '' };
    this.timeline = this.timeline.map((step, index) => ({
      ...step,
      status: index === 0 ? 'active' : 'pending',
    }));
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(value || 0);
  }
}
