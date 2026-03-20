import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RegistroService {

  private apiUrl = 'https://countriesnow.space/api/v0.1/';
  private apiTeams = 'https://api.football-data.org/v4/competitions/WC/teams';


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
    return this.http.get<any>(this.apiTeams, {
      headers: {
        'X-Auth-Token': 'fa6c5ddedd8748a2ba406b03aee84411'
      }
    }).pipe(
      map(res => res.teams)
    );
  }
}
