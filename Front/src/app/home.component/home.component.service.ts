import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { observableToBeFn } from 'rxjs/internal/testing/TestScheduler';
import { AuthService } from '../security/AuthService';


@Injectable({
  providedIn: 'root'

})

export class HomeComponentService {
  constructor(private http: HttpClient, private authService: AuthService) {
  }

  private apiUrl = 'http://localhost:8080/users/';


  getUserById(id: any): Observable<any> {

    const headers = this.authService.createAuthHeaders();
    return this.http.get(this.apiUrl + id, {
      headers: headers

    });
  }

  updateUser(data: any, id: any): Observable<any> {
    const headers = this.authService.createAuthHeaders();
    return this.http.put(this.apiUrl + id, data, {
      headers: headers
    });
  }

  updatePassword(data: any, id: any): Observable<any> {
    const headers = this.authService.createAuthHeaders();
    return this.http.put(this.apiUrl + id + '/password', data, {
      headers: headers
    });
  }

  deleteAccount(id: any): Observable<any> {
    const headers = this.authService.createAuthHeaders();
    return this.http.delete(this.apiUrl + id, {
      headers: headers
    });
  }

}
