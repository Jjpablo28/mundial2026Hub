import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { LoginService } from './login.service';
import { AuthGuard } from '../security/AuthGuard';
import { AuthService } from '../security/AuthService';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  showPass: boolean = false;
  showConfirmPass: boolean = false;

  identifier: string = '';
  password: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  errorMessage: string = '';
  showNewPasswordModal = false;
  showErrorPopup = false;

  constructor(
    private loginService: LoginService,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
  }

  login() {
    const body = {
      identifier: this.identifier,
      password: this.password
    };
    this.loginService.login(body).subscribe({
      next: (response) => {
        if (response.token) {
          this.authService.saveToken(response.token);
          console.log('Login exitoso. Token guardado.');
          this.authService.isAuthenticatedSubject.next(true);
          const role = this.authService.getRole();
        }
        this.router.navigate(['/home']);
        console.log('Registro exitoso', response, 'Data', body);
      },
      error: (err) => console.log('Error en login', err)
    });
  }

  showForgotModal: boolean = false;
  forgotEmail: string = '';
  emailF: string = '';

  updatePassword() {

    this.emailF = localStorage.getItem('email') || '';

    if (this.newPassword == this.confirmPassword) {
      this.loginService.rememberPassword(this.emailF, this.newPassword).subscribe({
        next: (response) => {
          this.showNewPasswordModal = false;

          this.errorMessage = 'contraseña cambiada';
          this.showErrorPopup = true;

          setTimeout(() => {
            this.showErrorPopup = false;
            this.cdr.detectChanges();
          }, 5000);
        },
        error: (err) => {
  console.log(err)
        }
      });
    } else {
      this.showForgotModal = false;
      this.errorMessage = 'Las contraseñas no coinciden';
      this.showErrorPopup = true;

      setTimeout(() => {
        this.showErrorPopup = false;
        this.cdr.detectChanges();
      }, 5000);
    }

  }

  checkEmail() {
    this.loginService.findEmail(this.forgotEmail).subscribe({
      next: (response) => {
        if (response == true) {
          localStorage.setItem('email', this.forgotEmail);
          this.forgotEmail = '';
          this.showNewPasswordModal = true;
          this.showForgotModal=false


          this.cdr.detectChanges();

        } else {
          this.showForgotModal = false;
          this.errorMessage = 'El correo no existe';
          this.showErrorPopup = true;

          setTimeout(() => {
            this.showErrorPopup = false;
            this.cdr.detectChanges();
          }, 5000);
          this.forgotEmail = '';
          this.cdr.detectChanges();

        }
      },
      error: (err) => {
      }
    });
  }
}
