import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import template from './app.component.html?raw';
import styles from './app.component.css?raw';
import { ADMIN_ROLE, AuthService, CurrentUserInfo, MenuItem } from './auth.service';

interface RuleConfiguration {
  minimumVintageMonths: number;
  minimumThreeMonthAverageDisbursal: number;
  maximumNetBouncePercentage: number;
  rejectWhenRcuFraudFlagged: boolean;
  version: number;
  updatedAt: string;
}

interface DealerInput {
  dealerCode: string;
  dealerName: string;
  dealerVintageMonths: number;
  lastThreeMonthAverageDisbursal: number;
  netBouncePercentage: number;
  rcuFraudFlagged: boolean;
}

export interface RuleOutcome {
  ruleId: string;
  label: string;
  passed: boolean;
  actual: string;
  expected: string;
  reason: string;
}

export interface EligibilityResult {
  dealerCode: string;
  dealerName: string;
  status: 'ELIGIBLE' | 'NOT_ELIGIBLE';
  eligible: boolean;
  nextScreen: 'APPLY_FOR_TRADE_ADVANCE';
  ruleVersion: number;
  evaluatedAt: string;
  outcomes: RuleOutcome[];
}

export type BorrowerType = 'INDIVIDUAL_OR_PROPRIETOR' | 'DIRECTOR_OR_PARTNER';

export interface CreditCheckConfiguration {
  minBureauScoreIndividualProprietor: number;
  minBureauScoreDirectorPartner: number;
  consumerBureauTaAmountCeiling: number;
  minCommercialCibilScore: number;
  maxCmr: number;
  minBusinessVintageMonths: number;
  minTvscsVintageMonths: number;
  minTaLimit: number;
  maxTaLimit: number;
  externalRatingRequiredAboveTaAmount: number;
  minPenetrationPercentage: number;
  minPositiveTradeReferences: number;
  version: number;
  updatedAt: string;
}

export interface CreditCheckInput {
  dealerCode: string;
  dealerName: string;
  borrowerType: BorrowerType;
  taAmountRequested: number;
  bureauScore: number;
  commercialCibilScore: number | null;
  cmr: number | null;
  delinquency30PlusLast6Months: boolean;
  severeDelinquencyLast3Years: boolean;
  onNegativeOrWilfulDefaulterList: boolean;
  cibilConsentObtained: boolean;
  businessVintageMonths: number;
  tvscsVintageMonths: number;
  kycCompleted: boolean;
  retailFinanceVolumeLast6Months: number | null;
  requiredFinancialDocumentsSubmitted: boolean;
  positivePatLastTwoYears: boolean;
  salesIncreasingTrend: boolean;
  negativeNetWorth: boolean;
  penetrationPercentage: number;
  positiveTradeReferencesCount: number;
  thirtyPlusPercentage: number;
  nationalAverageThirtyPlusPercentage: number;
  ninetyPlusPercentage: number;
  nationalAverageNinetyPlusPercentage: number;
  policyActionedDealer: boolean;
  dealerBlocked: boolean;
  externalCreditRatingValidated: boolean;
}

export interface CreditCheckResult {
  dealerCode: string;
  dealerName: string;
  status: 'CREDIT_APPROVED' | 'CREDIT_DECLINED';
  approved: boolean;
  nextScreen: 'APPLY_FOR_TRADE_ADVANCE';
  ruleVersion: number;
  evaluatedAt: string;
  outcomes: RuleOutcome[];
}

type TabId =
  | 'evaluate'
  | 'configure'
  | 'credit'
  | 'credit-configure'
  | 'journey'
  | 'deviation-approval'
  | 'user-management';

const ROUTE_TO_TAB: Record<string, TabId> = {
  '/evaluate': 'evaluate',
  '/configure': 'configure',
  '/credit': 'credit',
  '/credit-configure': 'credit-configure',
  '/journey': 'journey',
  '/deviation-approval': 'deviation-approval',
  '/user-management': 'user-management',
};

@Component({
  selector: 'app-root',
  template,
  styles: [styles],
})
export class AppComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  readonly api = '/api';

  authLoading = true;
  currentUser?: CurrentUserInfo;
  menus: MenuItem[] = [];
  menusError = '';

  activeTab: TabId = 'journey';
  loading = false;
  saving = false;
  error = '';
  saveMessage = '';
  result?: EligibilityResult;

  creditLoading = false;
  creditSaving = false;
  creditError = '';
  creditSaveMessage = '';
  creditResult?: CreditCheckResult;

  config: RuleConfiguration = {
    minimumVintageMonths: 6,
    minimumThreeMonthAverageDisbursal: 200000,
    maximumNetBouncePercentage: 25,
    rejectWhenRcuFraudFlagged: true,
    version: 1,
    updatedAt: new Date().toISOString(),
  };

  dealer: DealerInput = {
    dealerCode: 'DLR-1048',
    dealerName: 'Apex Auto Centre',
    dealerVintageMonths: 14,
    lastThreeMonthAverageDisbursal: 285000,
    netBouncePercentage: 11.5,
    rcuFraudFlagged: false,
  };

  creditConfig: CreditCheckConfiguration = {
    minBureauScoreIndividualProprietor: 730,
    minBureauScoreDirectorPartner: 700,
    consumerBureauTaAmountCeiling: 1000000,
    minCommercialCibilScore: 700,
    maxCmr: 6,
    minBusinessVintageMonths: 12,
    minTvscsVintageMonths: 6,
    minTaLimit: 200000,
    maxTaLimit: 1000000000,
    externalRatingRequiredAboveTaAmount: 10000000,
    minPenetrationPercentage: 10,
    minPositiveTradeReferences: 2,
    version: 1,
    updatedAt: new Date().toISOString(),
  };

  creditDealer: CreditCheckInput = {
    dealerCode: 'DLR-1048',
    dealerName: 'Apex Auto Centre',
    borrowerType: 'INDIVIDUAL_OR_PROPRIETOR',
    taAmountRequested: 500000,
    bureauScore: 745,
    commercialCibilScore: null,
    cmr: null,
    delinquency30PlusLast6Months: false,
    severeDelinquencyLast3Years: false,
    onNegativeOrWilfulDefaulterList: false,
    cibilConsentObtained: true,
    businessVintageMonths: 14,
    tvscsVintageMonths: 14,
    kycCompleted: true,
    retailFinanceVolumeLast6Months: 700000,
    requiredFinancialDocumentsSubmitted: true,
    positivePatLastTwoYears: true,
    salesIncreasingTrend: true,
    negativeNetWorth: false,
    penetrationPercentage: 15,
    positiveTradeReferencesCount: 2,
    thirtyPlusPercentage: 1,
    nationalAverageThirtyPlusPercentage: 2,
    ninetyPlusPercentage: 1,
    nationalAverageNinetyPlusPercentage: 2,
    policyActionedDealer: false,
    dealerBlocked: false,
    externalCreditRatingValidated: false,
  };

  ngOnInit(): void {
    this.authService.fetchCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.authLoading = false;
        if (user.authenticated) this.loadMenus();
      },
      error: () => {
        this.currentUser = { authenticated: false };
        this.authLoading = false;
      },
    });
  }

  login(): void {
    this.authService.login();
  }

  logout(): void {
    this.authService.logout();
  }

  get isAdmin(): boolean {
    return !!this.currentUser?.roles?.includes(ADMIN_ROLE);
  }

  loadMenus(): void {
    this.authService.fetchMenus().subscribe({
      next: (menus) => {
        this.menus = menus;
        const tabs = menus.map((m) => ROUTE_TO_TAB[m.route]).filter((t): t is TabId => !!t);
        if (tabs.length && !tabs.includes(this.activeTab)) {
          this.activeTab = tabs[0];
        }
        if (this.activeTab === 'evaluate' || this.activeTab === 'configure') this.loadConfiguration();
        if (this.activeTab === 'credit' || this.activeTab === 'credit-configure') this.loadCreditConfiguration();
      },
      error: () => {
        this.menusError = 'Could not load your menu. Please refresh.';
      },
    });
  }

  setTab(tab: TabId): void {
    this.activeTab = tab;
    if (tab === 'evaluate' || tab === 'configure') this.loadConfiguration();
    if (tab === 'credit' || tab === 'credit-configure') this.loadCreditConfiguration();
  }

  hasMenu(route: string): boolean {
    return this.menus.some((m) => m.route === route);
  }

  loadConfiguration(): void {
    this.http.get<RuleConfiguration>(`${this.api}/rules/config`).subscribe({
      next: (config) => (this.config = config),
      error: () => (this.error = 'Could not load the active rule configuration.'),
    });
  }

  evaluate(): void {
    this.loading = true;
    this.error = '';
    this.result = undefined;
    this.http
      .post<EligibilityResult>(`${this.api}/eligibility/evaluate`, this.dealer)
      .subscribe({
        next: (result) => {
          this.result = result;
          this.loading = false;
        },
        error: () => {
          this.error = 'Evaluation failed. Check the backend connection and input values.';
          this.loading = false;
        },
      });
  }

  saveRules(): void {
    this.saving = true;
    this.error = '';
    this.saveMessage = '';
    const payload = {
      minimumVintageMonths: Number(this.config.minimumVintageMonths),
      minimumThreeMonthAverageDisbursal: Number(
        this.config.minimumThreeMonthAverageDisbursal,
      ),
      maximumNetBouncePercentage: Number(this.config.maximumNetBouncePercentage),
      rejectWhenRcuFraudFlagged: this.config.rejectWhenRcuFraudFlagged,
    };
    this.http
      .put<RuleConfiguration>(`${this.api}/rules/config`, payload)
      .subscribe({
        next: (config) => {
          this.config = config;
          this.saveMessage = `Rule set v${config.version} is now active.`;
          this.saving = false;
        },
        error: () => {
          this.error = 'Rule changes could not be saved.';
          this.saving = false;
        },
      });
  }

  loadCreditConfiguration(): void {
    this.http.get<CreditCheckConfiguration>(`${this.api}/credit/config`).subscribe({
      next: (config) => (this.creditConfig = config),
      error: () => (this.creditError = 'Could not load the active credit policy configuration.'),
    });
  }

  evaluateCredit(): void {
    this.creditLoading = true;
    this.creditError = '';
    this.creditResult = undefined;
    this.http
      .post<CreditCheckResult>(`${this.api}/credit/evaluate`, this.creditDealer)
      .subscribe({
        next: (result) => {
          this.creditResult = result;
          this.creditLoading = false;
        },
        error: () => {
          this.creditError = 'Credit evaluation failed. Check the backend connection and input values.';
          this.creditLoading = false;
        },
      });
  }

  saveCreditRules(): void {
    this.creditSaving = true;
    this.creditError = '';
    this.creditSaveMessage = '';
    const payload = {
      minBureauScoreIndividualProprietor: Number(this.creditConfig.minBureauScoreIndividualProprietor),
      minBureauScoreDirectorPartner: Number(this.creditConfig.minBureauScoreDirectorPartner),
      consumerBureauTaAmountCeiling: Number(this.creditConfig.consumerBureauTaAmountCeiling),
      minCommercialCibilScore: Number(this.creditConfig.minCommercialCibilScore),
      maxCmr: Number(this.creditConfig.maxCmr),
      minBusinessVintageMonths: Number(this.creditConfig.minBusinessVintageMonths),
      minTvscsVintageMonths: Number(this.creditConfig.minTvscsVintageMonths),
      minTaLimit: Number(this.creditConfig.minTaLimit),
      maxTaLimit: Number(this.creditConfig.maxTaLimit),
      externalRatingRequiredAboveTaAmount: Number(this.creditConfig.externalRatingRequiredAboveTaAmount),
      minPenetrationPercentage: Number(this.creditConfig.minPenetrationPercentage),
      minPositiveTradeReferences: Number(this.creditConfig.minPositiveTradeReferences),
    };
    this.http
      .put<CreditCheckConfiguration>(`${this.api}/credit/config`, payload)
      .subscribe({
        next: (config) => {
          this.creditConfig = config;
          this.creditSaveMessage = `Credit policy v${config.version} is now active.`;
          this.creditSaving = false;
        },
        error: () => {
          this.creditError = 'Credit policy changes could not be saved.';
          this.creditSaving = false;
        },
      });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(value);
  }
}
