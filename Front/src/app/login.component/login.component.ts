import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { LoginService } from './login.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  form = {
    user: '',
    password: ''
  };

  constructor(
    private loginService: LoginService,
    private router: Router
  ) {}

  login() {
    this.loginService.login(this.form).subscribe({
      next: () => {
        alert('Login OK');
        console.log(this.form);
      },
      error: () => alert('Error en login')
    });
  }
}
