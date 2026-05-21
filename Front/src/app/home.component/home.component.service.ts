import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { observableToBeFn } from 'rxjs/internal/testing/TestScheduler';
import { AuthService } from '../security/AuthService';


@Injectable({
  providedIn: 'root'

})

export class HomeComponentService {
  constructor(private http: HttpClient, private authService: AuthService) {
  }

  private apiUrl = 'https://mundial2026hub.onrender.com/users/';
  private urlFootball = 'https://mundial2026hub.onrender.com/teams/';


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

  getMatches(): Observable<any[]> {
    return this.http.get<any>(this.urlFootball + 'matches').pipe(
      map(res => {
        const dataArray = res.matches ? res.matches : res;

        return dataArray.map((m: any) => ({
          id: m.id,
          utcDate: m.utcDate,
          stage: m.stage,
          matchday: m.matchday,
          status: m.status, // "FINISHED", "SCHEDULED", etc.
          homeTeam: m.homeTeam,
          awayTeam: m.awayTeam,
          score: m.score
        }));
      })
    );
  }

  getMatchById(id: string | number): Observable<any> {
    return this.http.get<any>(`${this.urlFootball}matches/${id}`).pipe(
      map(m => ({
        id: m.id,
        date: m.utcDate,
        status: m.status,
        group: m.group,
        competitionInfo: `${m.competition?.name || 'Competición'}, ${m.stage || ''}`,
        homeScore: m.score?.fullTime?.home,
        awayScore: m.score?.fullTime?.away,
        homeTeam: { name: m.homeTeam?.shortName || m.homeTeam?.name, crest: m.homeTeam?.crest, tla: m.homeTeam?.tla },
        awayTeam: { name: m.awayTeam?.shortName || m.awayTeam?.name, crest: m.awayTeam?.crest, tla: m.awayTeam?.tla }
      }))
    );
  }

  seleccionarPartido(id: number | null) {
    this.partidoSeleccionadoSource.next(id);
  }
  getStandings(): Observable<any[]> {
    return this.http.get<any>(this.urlFootball + 'standings').pipe(
      map(res => {
        const data = res.standings ? res.standings : res;
        return data.filter((s: any) => s.type === 'TOTAL' && s.group);
      })
    );
  }
  private partidoSeleccionadoSource = new BehaviorSubject<number | null>(null);
  public partidoSeleccionado$ = this.partidoSeleccionadoSource.asObservable();
}
