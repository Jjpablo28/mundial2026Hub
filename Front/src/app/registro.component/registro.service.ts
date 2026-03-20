import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RegistroService {

  private apiUrl = 'https://countriesnow.space/api/v0.1/';
  private apiTeams = 'http://localhost:8080/auth/teams';


  constructor(private http: HttpClient) {
  }


  getCountries(): Observable<string[]> {
    return this.http.get<any>(this.apiUrl + 'countries').pipe(
      map(res => res.data.map((item: any) => item.country))
    );
  }

  getCitiesByCountry(countryName: string): Observable<string[]> {
    return this.http.get<any>(this.apiUrl + 'countries').pipe(
      map(res => {
        const country = res.data.find((c: any) => c.country === countryName);
        return country ? country.cities : [];
      })
    );
  }

  getTeams(): Observable<any[]> {
    return this.http.get<any>(this.apiTeams).pipe(
      map(res => {
        return res.teams.map((t: any) => t.name);
      })
    );
  }
}
