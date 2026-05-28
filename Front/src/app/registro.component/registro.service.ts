import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable, switchMap, of, catchError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RegistroService {

  private apiGeoNames = 'https://secure.geonames.org/';
  private apiUrl = 'https://mundial2026hub.onrender.com/';

  // ⚠️ REEMPLAZA ESTO CON TU USUARIO ACTIVO DE GEONAMES
  private geonamesUsername = 'cliente';

  constructor(private http: HttpClient) {}

  public updateProfile(userId: number | null, data: { avatarUrl: string; favoriteTeam: string | undefined }): Observable<any> {
    const body = { favoriteTeam: data.favoriteTeam ?? null, avatarUrl: data.avatarUrl };
    return this.http.put(`${this.apiUrl}users/${userId}`, body);
  }

  /**
   * OBTIENE LOS PAÍSES DESDE GEONAMES
   */
  getCountries(): Observable<string[]> {
    const url = `${this.apiGeoNames}countryInfoJSON?lang=es&username=${this.geonamesUsername}`;

    return this.http.get<any>(url).pipe(
      map(res => {
        if (!res || !res.geonames) return [];
        return res.geonames
          .map((item: any) => item.countryName as string)
          .sort((a: string, b: string) => a.localeCompare(b));
      }),
      catchError(() => of([]))
    );
  }

  /**
   * OBTIENE LAS CIUDADES POR PAÍS
   * (Firma exacta con un solo parámetro para tu flujo de 2 selectores)
   */
  getCitiesByCountry(countryName: string): Observable<string[]> {
    if (!countryName) return of([]);

    const countryUrl = `${this.apiGeoNames}countryInfoJSON?lang=es&username=${this.geonamesUsername}`;

    return this.http.get<any>(countryUrl).pipe(
      map(res => {
        if (!res || !res.geonames) return null;
        const found = res.geonames.find(
          (c: any) => c.countryName.toLowerCase().trim() === countryName.toLowerCase().trim()
        );
        return found ? found.countryCode : null;
      }),
      switchMap(countryCode => {
        if (!countryCode) return of([]);

        const citiesUrl = `${this.apiGeoNames}searchJSON?country=${countryCode}&featureClass=P&maxRows=1000&orderby=population&username=${this.geonamesUsername}`;

        return this.http.get<any>(citiesUrl).pipe(
          map(res => {
            if (!res || !res.geonames) return [];
            // Forzamos el mapeo a un arreglo de strings nativo para solucionar el error TS2345
            const cityNames: string[] = res.geonames.map((c: any) => c.name as string);
            const uniqueCities = Array.from(new Set(cityNames));
            return uniqueCities.sort((a, b) => a.localeCompare(b));
          })
        );
      }),
      catchError(() => of([]))
    );
  }

  getTeams(): Observable<any[]> {
    return this.http.get<any>(this.apiUrl + 'teams').pipe(
      map(res => {
        const dataArray = res.teams ? res.teams : res;
        return dataArray.map((t: any) => ({
          name: t.name,
          crest: t.crest,
          shortName: t.shortName,
          fifaRanking: t.fifaRanking,
          groupName: t.groupName
        }));
      })
    );
  }

  register(data: any, rol: string): Observable<any> {
    const params = new HttpParams().set('rol', rol);
    return this.http.post<any>(this.apiUrl + 'auth/' + 'register', data, { params });
  }

  resendCode(email: string): Observable<any> {
    const params = new HttpParams().set('identifier', email);
    return this.http.put<any>(this.apiUrl + 'auth/' + 'resend', null, { params });
  }

  verify(data: any): Observable<any> {
    return this.http.post<any>(this.apiUrl + 'auth/' + 'verify', data);
  }
}
