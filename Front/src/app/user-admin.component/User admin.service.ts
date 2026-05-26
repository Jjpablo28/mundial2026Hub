import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface UserDTO {
  id: number;
  name: string;
  userName: string;
  email: string;
  identification: string;
  country: string;
  city: string;
  role: string;
  enabled: boolean;
  totalPoints: number;
  packets: number;
  avatarUrl: string;
  favoriteTeam: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private apiUrl = 'https://mundial2026hub.onrender.com/users';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(this.apiUrl);
  }

  // PUT /users/{id} con body
  updateUser(id: number, data: Partial<UserDTO>): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, data);
  }

  // PUT /users/{id}/role?role=ADMIN — el backend usa @RequestParam
  updateRole(id: number, role: string): Observable<any> {
    const params = new HttpParams().set('role', role);
    return this.http.put<any>(`${this.apiUrl}/${id}/role`, null, { params });
  }

  // PUT /users/{id} con enabled — no existe endpoint propio, se reutiliza updateUser
  toggleEnabled(id: number, enabled: boolean): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, { enabled });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
