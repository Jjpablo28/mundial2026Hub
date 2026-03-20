import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {

  logout(): void {
    localStorage.clear();
  }

  saveToken(token: string) {
    localStorage.setItem('token', token);
  }
}
