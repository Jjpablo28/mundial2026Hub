import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../security/AuthService';
import {
  SupportService,
  SupportTicketDTO,
  SupportStats,
  TicketStatus,
  TicketType,
  CreateTicketPayload,
} from './support.service';

// ─── Tipos de vista ────────────────────────────────────────────────────────────
type AdminView = 'dashboard' | 'tickets' | 'create';
type UserView  = 'mis-tickets' | 'solicitar';
type ActiveView = AdminView | UserView;

// ─── Mapeo de tipo amigable → TicketType backend ──────────────────────────────
const USER_TYPE_MAP: Record<string, TicketType> = {
  'sobres':        'ADD_PACKETS',
  'tokens':        'ADD_TOKENS',
  'cuenta':        'UNLOCK_ACCOUNT',
  'verificacion':  'RESET_VERIFICATION',
  'otro':          'CUSTOM',
};

@Component({
  selector: 'app-support',
  standalone: false,
  templateUrl: './support-component.html',
  styleUrls: ['./support-component.scss'],
})
export class SupportComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  // ─── Rol y usuario ────────────────────────────────────────────────────────
  isAdmin   = false;
  currentUserId: number | null = null;

  // ─── Estado de vista ──────────────────────────────────────────────────────
  activeView: ActiveView = 'dashboard';
  isLoading        = false;
  isDashboardLoading = false;   // spinner exclusivo del dashboard
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';
  showToast  = false;

  // ─── Datos compartidos ────────────────────────────────────────────────────
  tickets: SupportTicketDTO[] = [];
  totalTickets = 0;
  currentPage  = 0;
  readonly pageSize = 10;
  selectedStatus: TicketStatus | '' = '';

  // ─── Solo admin ───────────────────────────────────────────────────────────
  stats: SupportStats = { open: 0, in_progress: 0, resolved: 0, rejected: 0 };
  showActionModal  = false;
  modalAction: 'resolve' | 'reject' = 'resolve';
  selectedTicket: SupportTicketDTO | null = null;
  actionResolution = '';
  createForm!: FormGroup;

  // ─── Solo usuario ─────────────────────────────────────────────────────────
  userRequestForm!: FormGroup;
  selectedUserType = 'sobres';

  // ─── Constantes UI ────────────────────────────────────────────────────────
  readonly TICKET_TYPES: { value: TicketType; label: string; icon: string; needsQty: boolean }[] = [
    { value: 'ADD_PACKETS',        label: 'Agregar Sobres',         icon: 'ti-package',          needsQty: true  },
    { value: 'ADD_TOKENS',         label: 'Agregar Tokens',         icon: 'ti-coin',             needsQty: true  },
    { value: 'REFUND_TOKENS',      label: 'Reembolso de Tokens',    icon: 'ti-arrow-back-up',    needsQty: true  },
    { value: 'UNLOCK_ACCOUNT',     label: 'Desbloquear Cuenta',     icon: 'ti-lock-open',        needsQty: false },
    { value: 'REVOKE_ACCOUNT',     label: 'Suspender Cuenta',       icon: 'ti-ban',              needsQty: false },
    { value: 'RESET_VERIFICATION', label: 'Reiniciar Verificación', icon: 'ti-rotate-clockwise', needsQty: false },
    { value: 'CUSTOM',             label: 'Acción Personalizada',   icon: 'ti-pencil',           needsQty: false },
  ];

  readonly USER_TICKET_TYPES: { key: string; label: string; icon: string; needsQty: boolean }[] = [
    { key: 'sobres',       label: 'Me faltan sobres',      icon: 'ti-package',          needsQty: true  },
    { key: 'tokens',       label: 'Problema con tokens',   icon: 'ti-coin',             needsQty: true  },
    { key: 'cuenta',       label: 'Cuenta bloqueada',      icon: 'ti-lock-open',        needsQty: false },
    { key: 'verificacion', label: 'Verificación fallida',  icon: 'ti-rotate-clockwise', needsQty: false },
    { key: 'otro',         label: 'Otro problema',         icon: 'ti-pencil',           needsQty: false },
  ];

  readonly STATUS_FILTERS: { value: TicketStatus | ''; label: string }[] = [
    { value: '',            label: 'Todos'      },
    { value: 'OPEN',        label: 'Abiertos'   },
    { value: 'IN_PROGRESS', label: 'En proceso' },
    { value: 'RESOLVED',    label: 'Resueltos'  },
    { value: 'REJECTED',    label: 'Rechazados' },
  ];

  constructor(
    private authService: AuthService,
    private supportService: SupportService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
  ) {}

  // ─── Lifecycle ────────────────────────────────────────────────────────────

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.isAdmin       = role === 'ROLE_ADMIN' || role === 'ROLE_SUPPORT';
    this.currentUserId = this.authService.getUserId();

    this.buildAdminForm();
    this.buildUserForm();

    if (this.isAdmin) {
      this.activeView = 'dashboard';
      this.loadStats();
      this.loadAllTickets(true);
    } else {
      this.activeView = 'mis-tickets';
      this.loadUserTickets();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─── Formularios ─────────────────────────────────────────────────────────

  buildAdminForm(): void {
    this.createForm = this.fb.group({
      targetUserId: [null, [Validators.required, Validators.min(1)]],
      ticketType:   [null,  Validators.required],
      quantity:     [null],
      notes:        ['',   Validators.maxLength(500)],
    });

    this.createForm.get('ticketType')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((type: TicketType) => {
        const qty = this.createForm.get('quantity')!;
        if (this.needsQuantity(type)) {
          qty.setValidators([Validators.required, Validators.min(1)]);
        } else {
          qty.clearValidators();
          qty.setValue(null);
        }
        qty.updateValueAndValidity();
      });
  }

  buildUserForm(): void {
    this.userRequestForm = this.fb.group({
      description: ['', [Validators.required, Validators.maxLength(500)]],
      quantity:    [null],
    });
  }

  needsQuantity(type: TicketType | null | string): boolean {
    if (!type) return false;
    // Para admin: verifica por TicketType
    if (['ADD_PACKETS', 'ADD_TOKENS', 'REFUND_TOKENS'].includes(type as string)) return true;
    // Para usuario: verifica por key amigable
    return this.USER_TICKET_TYPES.find(t => t.key === type)?.needsQty ?? false;
  }

  get selectedTicketType(): TicketType | null {
    return this.createForm.get('ticketType')?.value ?? null;
  }

  get selectedUserTypeNeedsQty(): boolean {
    return this.USER_TICKET_TYPES.find(t => t.key === this.selectedUserType)?.needsQty ?? false;
  }

  // ─── Carga de datos ───────────────────────────────────────────────────────

  loadStats(): void {
    this.supportService.getStats()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next:  s  => { this.stats = s; this.cdr.markForCheck(); },
        error: () => this.notify('No se pudieron cargar las estadísticas', 'error'),
      });
  }

  loadAllTickets(forDashboard = false): void {
    if (forDashboard) {
      this.isDashboardLoading = true;
    } else {
      this.isLoading = true;
    }
    this.cdr.markForCheck();
    const status = this.selectedStatus || undefined;

    // Timeout de 65 s — Render free tier puede tardar hasta ~60 s en despertar
    const timeoutId = setTimeout(() => {
      if (forDashboard) this.isDashboardLoading = false;
      else this.isLoading = false;
      this.notify('El servidor tardó demasiado. Intenta de nuevo.', 'error');
      this.cdr.markForCheck();
    }, 65000);

    this.supportService
      .getAllTickets(this.currentPage, this.pageSize, status as TicketStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: page => {
          clearTimeout(timeoutId);
          this.tickets      = page.content;
          this.totalTickets = page.totalElements;
          if (forDashboard) this.isDashboardLoading = false;
          else this.isLoading = false;
          this.cdr.markForCheck();
        },
        error: () => {
          clearTimeout(timeoutId);
          if (forDashboard) this.isDashboardLoading = false;
          else this.isLoading = false;
          this.notify('Error al cargar los tickets', 'error');
          this.cdr.markForCheck();
        },
      });
  }

  loadUserTickets(): void {
    if (!this.currentUserId) return;
    this.isLoading = true;
    this.cdr.markForCheck();
    this.supportService
      .getTicketsByUser(this.currentUserId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: tickets => {
          this.tickets      = this.selectedStatus
            ? tickets.filter(t => t.status === this.selectedStatus)
            : tickets;
          this.totalTickets = this.tickets.length;
          this.isLoading    = false;
          this.cdr.markForCheck();
        },
        error: () => {
          this.isLoading = false;
          this.notify('Error al cargar tus tickets', 'error');
          this.cdr.markForCheck();
        },
      });
  }

  // ─── Creación admin ───────────────────────────────────────────────────────

  submitAdminCreate(): void {
    if (this.createForm.invalid) { this.createForm.markAllAsTouched(); return; }
    this.isLoading = true;

    const agentId = this.currentUserId!;
    const payload: CreateTicketPayload = {
      targetUserId: this.createForm.value.targetUserId,
      ticketType:   this.createForm.value.ticketType,
      quantity:     this.needsQuantity(this.createForm.value.ticketType)
        ? this.createForm.value.quantity
        : null,
      notes: this.createForm.value.notes || undefined,
    };

    this.supportService.createTicket(payload, agentId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.isLoading = false;
          this.notify(res.message || 'Ticket creado exitosamente', 'success');
          this.createForm.reset();
          this.setView('tickets');
          this.loadAllTickets(false);
          this.loadStats();
        },
        error: err => {
          this.isLoading = false;
          this.notify(err?.error?.message || 'Error al crear el ticket', 'error');
        },
      });
  }

  // ─── Solicitud usuario ────────────────────────────────────────────────────

  submitUserRequest(): void {
    if (this.userRequestForm.invalid) { this.userRequestForm.markAllAsTouched(); return; }
    this.isLoading = true;

    const ticketType = USER_TYPE_MAP[this.selectedUserType] ?? 'CUSTOM';
    const payload: CreateTicketPayload = {
      targetUserId: this.currentUserId!,   // el ticket es para el propio usuario
      ticketType,
      quantity: this.selectedUserTypeNeedsQty
        ? this.userRequestForm.value.quantity
        : null,
      notes: this.userRequestForm.value.description,
    };

    // agentId = 0 indica que aún no hay agente asignado (ticket abierto por el propio usuario)
    this.supportService.createTicket(payload, 0)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.isLoading = false;
          this.notify('Solicitud enviada. Un agente la revisará pronto.', 'success');
          this.userRequestForm.reset();
          this.selectedUserType = 'sobres';
          this.setView('mis-tickets');
          this.loadUserTickets();
        },
        error: err => {
          this.isLoading = false;
          this.notify(err?.error?.message || 'Error al enviar la solicitud', 'error');
        },
      });
  }

  // ─── Acciones admin sobre tickets ─────────────────────────────────────────

  openActionModal(ticket: SupportTicketDTO, action: 'resolve' | 'reject'): void {
    this.selectedTicket  = ticket;
    this.modalAction     = action;
    this.actionResolution = '';
    this.showActionModal = true;
  }

  closeActionModal(): void {
    this.showActionModal = false;
    this.selectedTicket  = null;
    this.actionResolution = '';
  }

  confirmAction(): void {
    if (!this.selectedTicket?.id || !this.actionResolution.trim()) return;
    this.isLoading = true;
    const agentId = this.currentUserId!;

    const obs = this.modalAction === 'resolve'
      ? this.supportService.resolveTicket(this.selectedTicket.id, this.actionResolution, agentId)
      : this.supportService.rejectTicket(this.selectedTicket.id, this.actionResolution, agentId);

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.isLoading = false;
        this.notify(res.message, 'success');
        this.closeActionModal();
        this.loadAllTickets(false);
        this.loadStats();
      },
      error: err => {
        this.isLoading = false;
        this.notify(err?.error?.message || 'Error al procesar la acción', 'error');
      },
    });
  }

  // ─── Paginación y filtros ─────────────────────────────────────────────────

  changePage(page: number): void {
    this.currentPage = page;
    this.isAdmin ? this.loadAllTickets(this.activeView === 'dashboard') : this.loadUserTickets();
  }

  applyFilter(status: TicketStatus | ''): void {
    this.selectedStatus = status;
    this.currentPage    = 0;
    this.isAdmin ? this.loadAllTickets(this.activeView === 'dashboard') : this.loadUserTickets();
  }

  get totalPages(): number { return Math.ceil(this.totalTickets / this.pageSize); }
  get pages(): number[]    { return Array.from({ length: this.totalPages }, (_, i) => i); }

  // ─── Navegación ───────────────────────────────────────────────────────────

  setView(view: ActiveView): void {
    this.activeView = view;
    if (view === 'tickets')     this.loadAllTickets(false);
    if (view === 'dashboard')   { this.loadStats(); this.loadAllTickets(true); }
    if (view === 'mis-tickets') this.loadUserTickets();
  }

  setUserType(key: string): void { this.selectedUserType = key; }

  // ─── UI helpers ───────────────────────────────────────────────────────────

  getTicketTypeLabel(type: TicketType): string { return this.TICKET_TYPES.find(t => t.value === type)?.label ?? type; }
  getTicketTypeIcon(type: TicketType):  string { return this.TICKET_TYPES.find(t => t.value === type)?.icon  ?? 'ti-ticket'; }

  getStatusClass(status: TicketStatus): string {
    const map: Record<TicketStatus, string> = {
      OPEN:        'badge--open',
      IN_PROGRESS: 'badge--progress',
      RESOLVED:    'badge--resolved',
      REJECTED:    'badge--rejected',
    };
    return map[status] ?? '';
  }

  getStatusLabel(status: TicketStatus): string {
    const map: Record<TicketStatus, string> = {
      OPEN:        'Abierto',
      IN_PROGRESS: 'En proceso',
      RESOLVED:    'Resuelto',
      REJECTED:    'Rechazado',
    };
    return map[status] ?? status;
  }

  isFieldInvalid(form: FormGroup, field: string): boolean {
    const ctrl = form.get(field);
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  notify(msg: string, type: 'success' | 'error'): void {
    this.toastMessage = msg;
    this.toastType    = type;
    this.showToast    = true;
    setTimeout(() => (this.showToast = false), 3500);
  }
}
