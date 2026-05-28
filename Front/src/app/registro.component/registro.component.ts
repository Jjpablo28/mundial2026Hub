import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RegistroService } from './registro.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../security/AuthService';

@Component({
  selector: 'app-registro',
  standalone: false,
  templateUrl: './registro.component.html',
  styleUrls: ['./registro.component.scss']
})
export class RegistroComponent implements OnInit {
  userName: string = '';
  name: string = '';
  email: string = '';
  id: string = '';
  country: string = '';
  city: string = '';
  team: string | null = null;
  password: string = '';
  isLoading: boolean = false;
  passwordFocused = false;

  showErrorPopup: boolean = false;
  errorMessage: string = '';

  showSuccessPopup: boolean = false;
  successMessage: string = '';

  countries: string[] = [];
  cities: string[] = [];
  teams: string[] = [];

  constructor(
    private registroService: RegistroService,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCountries();
    this.loadTeams();
  }

  loadCountries() {
    this.registroService.getCountries().subscribe(res => {
      this.countries = res;
      this.cdr.detectChanges(); // Asegura que los países se pinten en la interfaz al llegar de GeoNames
    });
  }

  onCountryChange(country: string) {
    if (!country) return;

    this.cities = [];
    this.city = ''; // Limpia la ciudad previamente seleccionada si cambia el país

    this.registroService.getCitiesByCountry(country)
      .subscribe(cities => {
        this.cities = cities;
        this.cdr.detectChanges(); // Asegura que las ciudades se listen inmediatamente en el selector
      });
  }

  loadTeams() {
    this.registroService.getTeams().subscribe(res => {
      this.teams = res;
      this.cdr.detectChanges();
    });
  }

  formatIdentifier() {
    // Si parece email, lo pasa a minúsculas
    if (this.email.includes('@')) {
      this.email = this.email.toLowerCase();
    }
  }

  isPasswordValid(password: string): boolean {
    if (!password || password.length < 8) return false;
    const forbidden = /[<>&"'\/= ]/;
    if (forbidden.test(password)) return false;
    if (/^\d+$/.test(password)) return false;
    return /[a-z]/.test(password)
      && /[A-Z]/.test(password)
      && /[0-9]/.test(password)
      && /[^a-zA-Z0-9<>&"'\/= ]/.test(password);
  }

  register() {
    this.isLoading = true;

    const body = {
      userName: this.userName,
      name: this.name,
      email: this.email,
      identification: this.id,
      country: this.country,
      city: this.city,
      password: this.password
    };

    this.registroService.register(body, 'TEMPORAL').subscribe({
      next: (response) => {
        console.log('Registro exitoso', response);
        sessionStorage.setItem('_v_id', btoa(this.email));
        sessionStorage.setItem('_v_pw', btoa(this.password));
        localStorage.setItem('email', this.email);
        this.isLoading = false;
        this.successMessage = '¡Cuenta creada exitosamente! Verifica tu correo';
        this.showSuccessPopup = true;
        this.cdr.detectChanges();

        setTimeout(() => {
          this.showSuccessPopup = false;
          this.router.navigate(['/verify']);
        }, 1800);
      },
      error: (err) => {
        console.error('Error en registro', err);
        this.isLoading = false;
        this.errorMessage = 'Hubo un error al crear la cuenta';
        this.showErrorPopup = true;
        this.cdr.detectChanges();
        setTimeout(() => {
          this.showErrorPopup = false;
          this.cdr.detectChanges();
        }, 5000);
      }
    });
  }

  hasLower  = (p: string) => /[a-z]/.test(p ?? '');
  hasUpper  = (p: string) => /[A-Z]/.test(p ?? '');
  hasDigit  = (p: string) => /[0-9]/.test(p ?? '');
  hasSymbol = (p: string) => /[^a-zA-Z0-9<>&"'\/= ]/.test(p ?? '');
  hasNoForbidden = (p: string) => !/[<>&"'\/= ]/.test(p ?? '') && !/^[0-9]+$/.test(p ?? '');
}
