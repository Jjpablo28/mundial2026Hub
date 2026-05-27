import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// ─── Enums ────────────────────────────────────────────────────────────────────

export type TicketType =
  | 'ADD_PACKETS'
  | 'ADD_TOKENS'
  | 'RESET_VERIFICATION'
  | 'UNLOCK_ACCOUNT'
  | 'REVOKE_ACCOUNT'
  | 'REFUND_TOKENS'
  | 'CUSTOM';

export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED';

// ─── Interfaces ───────────────────────────────────────────────────────────────

export interface SupportTicketDTO {
  id?: number;
  targetUserId: number;
  targetUserName?: string;
  agentUserId?: number;
  agentUserName?: string;
  ticketType: TicketType;
  status?: TicketStatus;
  quantity?: number | null;
  notes?: string;
  resolution?: string;
  createdAt?: string;
  resolvedAt?: string | null;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
}

export interface SupportStats {
  open: number;
  in_progress: number;
  resolved: number;
  rejected: number;
}

export interface CreateTicketPayload {
  targetUserId: number;
  ticketType: TicketType;
  quantity?: number | null;
  notes?: string;
}

// ─── Service ──────────────────────────────────────────────────────────────────

@Injectable({
  providedIn: 'root'
})
export class SupportService {

  private readonly BASE_URL = 'https://mundial2026hub.onrender.com/support';

  constructor(private http: HttpClient) {}

  /**
   * Crea un nuevo ticket en estado OPEN.
   */
  createTicket(payload: CreateTicketPayload, agentId: number): Observable<ApiResponse<SupportTicketDTO>> {
    const params = new HttpParams().set('agentId', agentId.toString());
    return this.http.post<ApiResponse<SupportTicketDTO>>(
      `${this.BASE_URL}/tickets`,
      payload,
      { params }
    );
  }

  /**
   * Resuelve un ticket aplicando la acción de negocio sobre el usuario destino.
   */
  resolveTicket(ticketId: number, resolution: string, agentId: number): Observable<ApiResponse<SupportTicketDTO>> {
    const params = new HttpParams().set('agentId', agentId.toString());
    return this.http.put<ApiResponse<SupportTicketDTO>>(
      `${this.BASE_URL}/tickets/${ticketId}/resolve`,
      { resolution },
      { params }
    );
  }

  /**
   * Rechaza un ticket sin aplicar ninguna acción.
   */
  rejectTicket(ticketId: number, resolution: string, agentId: number): Observable<ApiResponse<SupportTicketDTO>> {
    const params = new HttpParams().set('agentId', agentId.toString());
    return this.http.put<ApiResponse<SupportTicketDTO>>(
      `${this.BASE_URL}/tickets/${ticketId}/reject`,
      { resolution },
      { params }
    );
  }

  /**
   * Lista todos los tickets paginados, con filtro opcional por estado.
   */
  getAllTickets(
    page = 0,
    size = 20,
    status?: TicketStatus
  ): Observable<PageResponse<SupportTicketDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<SupportTicketDTO>>(
      `${this.BASE_URL}/tickets`,
      { params }
    );
  }

  /**
   * Obtiene un ticket por ID.
   */
  getTicketById(id: number): Observable<SupportTicketDTO> {
    return this.http.get<SupportTicketDTO>(`${this.BASE_URL}/tickets/${id}`);
  }

  /**
   * Obtiene todos los tickets de un usuario destino.
   */
  getTicketsByUser(userId: number): Observable<SupportTicketDTO[]> {
    return this.http.get<SupportTicketDTO[]>(`${this.BASE_URL}/tickets/user/${userId}`);
  }

  /**
   * Obtiene todos los tickets gestionados por un agente.
   */
  getTicketsByAgent(agentId: number): Observable<SupportTicketDTO[]> {
    return this.http.get<SupportTicketDTO[]>(`${this.BASE_URL}/tickets/agent/${agentId}`);
  }

  /**
   * Retorna estadísticas rápidas por estado para el dashboard.
   */
  getStats(): Observable<SupportStats> {
    return this.http.get<SupportStats>(`${this.BASE_URL}/stats`);
  }
}
