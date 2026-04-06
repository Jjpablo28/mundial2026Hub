import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import {map, Observable} from 'rxjs';
import { observableToBeFn } from 'rxjs/internal/testing/TestScheduler';

export interface RegisterRequest {
  userName: string;
  email: string;
  country: string;
  city: string;
  id: string;
  name: string;
}

@Injectable({
  providedIn: 'root'

})

export class RegistroService {

  private apiCountries = 'https://countriesnow.space/api/v0.1/';
  private apiUrl = 'http://localhost:8080/auth/';


  constructor(private http: HttpClient) {
  }


  getCountries(): Observable<string[]> {
    return this.http.get<any>(this.apiCountries + 'countries').pipe(
      map(res => res.data.map((item: any) => item.country))
    );
  }

  getCitiesByCountry(countryName: string): Observable<string[]> {
    return this.http.get<any>(this.apiCountries + 'countries').pipe(
      map(res => {
        const country = res.data.find((c: any) => c.country === countryName);
        return country ? country.cities : [];
      })
    );
  }

  getTeams(): Observable<any[]> {
    return this.http.get<any>(this.apiUrl+'teams').pipe(
      map(res => {
        return res.teams.map((t: any) => t.name);
      })
    );
  }
  register(data: any, rol: string): Observable<any> {
    const params = new HttpParams().set('rol', rol);
    return this.http.post<any>(this.apiUrl + 'register', data, { params });
  }

  resendCode(email: string): Observable<any> {
    const params = new HttpParams().set('identifier', email);
    return this.http.put<any>(this.apiUrl + 'resend', null, { params });  }

  verify(data:any): Observable<any> {
    return this.http.post<any>(this.apiUrl + 'verify', data);
  }
}
