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
  public isLoading: boolean = false;

  showSuccessPopup: boolean = false;
  successMessage: string = '';

  constructor(
    private loginService: LoginService,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
  }

  login() {
    // 👇 2. Encendemos el loader apenas hacen clic
    this.isLoading = true;

    const body = {
      identifier: this.identifier,
      password: this.password
    };

    this.loginService.login(body).subscribe({
      next: async (response) => {
        if (response.token) {
          localStorage.clear();
          this.authService.saveToken(response.token);
          this.authService.isAuthenticatedSubject.next(true);
          await this.authService.cargarDatosIniciales();
        }

        this.isLoading = false;
        this.successMessage = '¡Bienvenido de vuelta! 🎉';
        this.showSuccessPopup = true;
        this.cdr.detectChanges();

        setTimeout(() => {
          this.showSuccessPopup = false;
          this.router.navigate(['/home']);
        }, 1500); // espera que el usuario vea el popup antes de navegar
      },
      error: (err) => {
        console.log(err);
        this.errorMessage = err?.error?.message
          ?? err?.error?.error
          ?? 'Error al cambiar la contraseña.';
        this.showErrorPopup = true;
        this.cdr.detectChanges();
        setTimeout(() => {
          this.showErrorPopup = false;
          this.cdr.detectChanges();
        }, 5000);
      }
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
          this.cdr.detectChanges();


          setTimeout(() => {
            this.showErrorPopup = false;
            this.cdr.detectChanges();
          }, 5000);
        },
        error: (err) => {
          console.log(err);
          this.errorMessage = err?.error?.message
            ?? err?.error?.error
            ?? 'Error al cambiar la contraseña.';
          this.showErrorPopup = true;
          this.cdr.detectChanges();
          setTimeout(() => {
            this.showErrorPopup = false;
            this.cdr.detectChanges();
          }, 5000);
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

formatIdentifier() {

  // Si parece email, lo pasa a minúsculas

  if (this.identifier.includes('@')) {

    this.identifier = this.identifier.toLowerCase();

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
          this.cdr.detectChanges();


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
