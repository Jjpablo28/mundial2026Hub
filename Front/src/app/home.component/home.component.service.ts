import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import {map, Observable} from 'rxjs';
import { observableToBeFn } from 'rxjs/internal/testing/TestScheduler';
import { AuthService } from '../security/AuthService';



@Injectable({
  providedIn: 'root'

})

export class HomeComponentService{
  constructor(private http: HttpClient, private authService: AuthService) {
  }
  private apiUrl = 'http://localhost:8080/users/';



  getUserById (id:any): Observable<any>{

    const headers = this.authService.createAuthHeaders();
    return this.http.get(this.apiUrl+id, {
      headers: headers,

    });  }
}
