import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../security/AuthService';
import { ActivityLogService, UserActivityLogDTO, PageResponse } from './activityLog.service';

@Component({
  selector: 'app-activity-log',
  standalone: false,
  templateUrl: './activity-log-component.html',
  styleUrl: './activity-log-component.scss'
})
export class ActivityLogComponent implements OnInit {

  logs: UserActivityLogDTO[] = [];
  isLoading = false;
  isAdmin = false;
  userId: number | null = null;

  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  totalElements = 0;

  readonly eventIcons: Record<string, string> = {
    PACK_OPENED:       'fa-box-open',
    PACK_PURCHASED:    'fa-shopping-cart',
    TRADE_COMPLETED:   'fa-exchange-alt',
    PAYMENT_APPROVED:  'fa-check-circle',
    PAYMENT_DECLINED:  'fa-times-circle',
    PROFILE_UPDATED:   'fa-user-edit',
    USER_UPDATED:      'fa-user-edit',
    USER_CREATED:      'fa-user-plus',
    USER_DELETED:      'fa-user-minus',
    ROLE_CHANGED:      'fa-shield-alt',
    PASSWORD_CHANGED:  'fa-key',
    SUPPORT_ADD_PACKETS:      'fa-plus-circle',
    SUPPORT_TICKET_CREATED:   'fa-ticket-alt',
    SUPPORT_TICKET_RESOLVED:  'fa-check-circle',
    SUPPORT_TICKET_REJECTED:  'fa-times-circle',
    LOGIN:                    'fa-sign-in-alt',
    REGISTER:                 'fa-user-plus',
    SUPPORT_UNLOCK: 'fa-unlock',
  };

  readonly eventColors: Record<string, string> = {
    PACK_OPENED:              'log-item event-yellow',
    PACK_PURCHASED:           'log-item event-orange',
    TRADE_COMPLETED:          'log-item event-teal',
    PAYMENT_APPROVED:         'log-item event-green',
    PAYMENT_DECLINED:         'log-item event-red',
    PROFILE_UPDATED:          'log-item event-gray',
    USER_UPDATED:             'log-item event-blue',
    USER_CREATED:             'log-item event-green',
    USER_DELETED:             'log-item event-red',
    ROLE_CHANGED:             'log-item event-yellow',
    PASSWORD_CHANGED:         'log-item event-orange',
    SUPPORT_ADD_PACKETS:      'log-item event-green',
    SUPPORT_TICKET_CREATED:   'log-item event-blue',
    SUPPORT_TICKET_RESOLVED:  'log-item event-teal',
    SUPPORT_TICKET_REJECTED:  'log-item event-red',
    LOGIN:                    'log-item event-purple',
    REGISTER:                 'log-item event-purple',
    SUPPORT_UNLOCK: 'log-item event-yellow',

  };

  activeFilter: string | string[] = '';

  readonly filterOptions: { value: string | string[], label: string, icon: string }[] = [
    { value: 'PACK',                                label: 'Sobres',   icon: 'fa-box-open' },
    { value: 'SUPPORT',                             label: 'Soporte',  icon: 'fa-headset' },
    { value: 'TRADE',                               label: 'Cambios',  icon: 'fa-exchange-alt' },
    { value: 'PAYMENT',                             label: 'Pagos',    icon: 'fa-credit-card' },
    { value: ['USER', 'LOGIN', 'REGISTER', 'ROLE'], label: 'Usuarios', icon: 'fa-user' },
  ];

  get filteredLogs(): UserActivityLogDTO[] {
    if (this.activeFilter === '') return this.logs;
    const prefixes = Array.isArray(this.activeFilter)
      ? this.activeFilter
      : [this.activeFilter];
    return this.logs.filter(l => prefixes.some(p => l.eventType.startsWith(p)));
  }

  setFilter(value: string | string[]): void {
    this.activeFilter = value;
  }

  isActiveFilter(value: string | string[]): boolean {
    return JSON.stringify(this.activeFilter) === JSON.stringify(value);
  }

  constructor(
    private authService: AuthService,
    private activityLogService: ActivityLogService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.isAdmin = role === 'ROLE_ADMIN';
    this.userId = this.authService.getUserId();
    this.loadLogs();
  }

  loadLogs(): void {
    this.isLoading = true;

    const request$ = this.isAdmin
      ? this.activityLogService.getAllLogsPaged(this.currentPage, this.pageSize)
      : this.activityLogService.getLogsByUserPaged(this.userId!, this.currentPage, this.pageSize);

    request$.subscribe({
      next: (res: PageResponse<UserActivityLogDTO>) => {
        this.logs = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadLogs();
  }

  getIcon(eventType: string): string {
    return this.eventIcons[eventType] ?? 'fa-circle';
  }

  getColor(eventType: string): string {
    return this.eventColors[eventType] ?? 'event-gray';
  }

  getPages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  toLocalTime(dateStr: string): Date {
    return new Date(dateStr);
  }
}
