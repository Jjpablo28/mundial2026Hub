import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private apiUrl = 'http://localhost:8080/auth/';

  constructor(private http: HttpClient) {}

  login(data: any): Observable<any> {
    return this.http.post(this.apiUrl+'login', data);
  }
  rememberPassword(email: string, password: string): Observable<any> {
    const params = new HttpParams().set('email', email)
      .set('newPassword', password);
    return this.http.put(this.apiUrl + 'rememberPassword', null, { params });
  }
  findEmail(emailF: string): Observable<any> {
    const params = new HttpParams().set('email', emailF);
    return this.http.get(this.apiUrl + 'email',{ params });
  }
}
