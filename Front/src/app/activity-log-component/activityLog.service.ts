import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserActivityLogDTO {
  id: number;
  userId: number;
  userName: string;
  eventType: string;
  detail: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class ActivityLogService {
  private apiUrl = 'https://mundial2026hub.onrender.com/activity-logs';

  constructor(private http: HttpClient) {}

  getLogsByUserPaged(userId: number, page = 0, size = 20): Observable<PageResponse<UserActivityLogDTO>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<UserActivityLogDTO>>(`${this.apiUrl}/user/${userId}/paged`, { params });
  }

  getAllLogsPaged(page = 0, size = 20): Observable<PageResponse<UserActivityLogDTO>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<UserActivityLogDTO>>(`${this.apiUrl}/all/paged`, { params });
  }
}
