import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RegistroService {

  private apiUrl = 'https://countriesnow.space/api/v0.1/countries';

  constructor(private http: HttpClient) {}


  getCountries(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }


  getCities(country: string): Observable<any> {
    return this.http.post<any>(
      'https://countriesnow.space/api/v0.1/countries/cities',
      { country: country }
    );
  }
}
