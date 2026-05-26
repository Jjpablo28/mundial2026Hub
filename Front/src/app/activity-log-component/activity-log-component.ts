import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../security/AuthService';
import { ActivityLogService, UserActivityLogDTO, PageResponse } from './activityLog.service';

@Component({
  selector: 'app-activity-log',
  standalone: false,
  templateUrl: './activity-log-component.html',
  styleUrl: 'activity-log-component.scss'
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
    PROFILE_UPDATED:   'fa-user-edit'
  };

  readonly eventColors: Record<string, string> = {
    PACK_OPENED:       'event-blue',
    PACK_PURCHASED:    'event-purple',
    TRADE_COMPLETED:   'event-teal',
    PAYMENT_APPROVED:  'event-green',
    PAYMENT_DECLINED:  'event-red',
    PROFILE_UPDATED:   'event-gray'
  };

  constructor(
    private authService: AuthService,
    private activityLogService: ActivityLogService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.isAdmin = role === 'ADMIN' || role === 'admin';
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
}
