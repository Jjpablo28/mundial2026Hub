import { Component } from '@angular/core';
import { AuthService } from '../security/AuthService';

@Component({
  selector: 'app-home.component',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  constructor(private authService: AuthService) {}

  logout() {
    this.authService.logout();
  }
}
