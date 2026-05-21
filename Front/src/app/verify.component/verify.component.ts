import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { RegistroService } from '../registro.component/registro.service';
import { AuthService } from '../security/AuthService';

@Component({
  selector: 'app-verify.component',
  standalone: false,
  templateUrl: './verify.component.html',
  styleUrl: './verify.component.scss'
})
export class VerifyComponent implements OnInit {
  verificationCode: string = '';
  isLoading: boolean = false;
  email: string | null = '';

  showPopup: boolean = false;
  isResending: boolean = false;
  showErrorPopup: boolean = false;
  showEmailModal: boolean = false;
  recoveryEmail: string = '';

  constructor(
    private registroService: RegistroService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  verify() {
    this.email = localStorage.getItem('email');
    this.isLoading = true;

    const body = {
      email: this.email,
      code: this.verificationCode
    };

    this.registroService.verify(body).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.router.navigate(['/home']);
      },
      error: (err) => {
        this.isLoading = false;
        alert('Código inválido o expirado');
      }
    });
  }

  resendCode() {
    if (this.isResending) return;

    this.email = localStorage.getItem('email') || '';
    this.isResending = true;
    this.showErrorPopup = false;
    this.showPopup = false;

    this.registroService.resendCode(this.email).subscribe({
      next: (res) => {
        this.isResending = false;
        this.showPopup = true;

        this.cdr.detectChanges();

        setTimeout(() => {
          this.showPopup = false;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        this.isResending = false;

        if (err.status === 404) {
          this.showEmailModal = true;
        } else {
          this.showErrorPopup = true;
          setTimeout(() => {
            this.showErrorPopup = false;
            this.cdr.detectChanges();
          }, 5000);
        }

        this.cdr.detectChanges();
      }
    });
  }

  resendWithNewEmail() {
    if (!this.recoveryEmail) return;

    localStorage.setItem('email', this.recoveryEmail);
    this.email = this.recoveryEmail;

    this.showEmailModal = false;

    this.resendCode();
  }

  ngOnInit(): void {}
}
