import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { ApplyJourneyComponent } from './apply-journey.component';
import { DeviationApprovalComponent } from './deviation-approval.component';
import { UserManagementComponent } from './user-management.component';

@NgModule({
  declarations: [AppComponent, ApplyJourneyComponent, DeviationApprovalComponent, UserManagementComponent],
  imports: [BrowserModule, HttpClientModule, FormsModule],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}