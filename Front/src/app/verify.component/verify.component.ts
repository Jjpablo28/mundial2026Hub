import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { RegistroService } from '../registro.component/registro.service';
import { AuthService } from '../security/AuthService';

// Ajusta esta interfaz a lo que devuelve tu registroService.getTeams()
export interface Team {
  name: string;
  crest: string;
  shortName: string;
  fifaRanking: string;
  groupName: string;
}

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
  errorMessage: string = '';

  // ===== ONBOARDING =====
  showOnboarding: boolean = false;
  onboardingStep: 1 | 2 | 3 = 1;   // 1: país, 2: equipo favorito, 3: avatar
  isSavingOnboarding: boolean = false;

  // Paso 1 — País
  countries: string[] = [];
  cities: string[] = [];
  selectedCountry: string = '';
  selectedCity: string = '';

  // Paso 2 — Equipo favorito
  teams: Team[] = [];
  filteredTeams: Team[] = [];
  teamSearch: string = '';
  selectedTeam: Team | null = null;

  // Paso 3 — Avatar
  avatars: string[] = [
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/2_hyyqec.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/4_cylwgo.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/1_hgwg6q.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/6_mnllri.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/9_hyeyqu.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/7_vckmsj.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/3_edl267.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/10_s4uxo8.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/5_zvitrg.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/8_p1wx2e.png'
  ];
  selectedAvatar: string = '';

  constructor(
    private registroService: RegistroService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {}

  // ===========================
  // VERIFICACIÓN
  // ===========================

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

        if (res.token) {
          this.authService.saveToken(res.token);
          this.authService.isAuthenticatedSubject.next(true);
        }
        sessionStorage.removeItem('_v_id');
        sessionStorage.removeItem('_v_pw');

        this.openOnboarding();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Código incorrecto. Inténtalo de nuevo.';
        this.showErrorPopup = true;

        this.cdr.detectChanges();
        setTimeout(() => {
          this.showErrorPopup = false;
          this.cdr.detectChanges();
        }, 5000);
      }
    });
  }

  // ===========================
  // ONBOARDING
  // ===========================

  openOnboarding() {
    this.registroService.getTeams().subscribe((res: any) => {
      this.teams = res;
      this.filteredTeams = res;
      this.cdr.detectChanges();
    });

    this.onboardingStep = 1;
    this.showOnboarding = true;
    this.cdr.detectChanges();
  }



  filterTeams() {
    const q = this.teamSearch.toLowerCase();
    this.filteredTeams = this.teams.filter(t =>
      t.name.toLowerCase().includes(q)
    );
  }

  selectTeam(team: Team) {
    this.selectedTeam = this.selectedTeam?.name === team.name ? null : team;
  }

  selectAvatar(avatar: string) {
    this.selectedAvatar = avatar;
  }

  nextStep() {
    if (this.onboardingStep < 2) {
      this.onboardingStep = (this.onboardingStep + 1) as 1 | 2;
      this.cdr.detectChanges();
    }
  }

  prevStep() {
    if (this.onboardingStep > 1) {
      this.onboardingStep = (this.onboardingStep - 1) as 1 | 2;
      this.cdr.detectChanges();
    }
  }

  canAdvanceStep2(): boolean {
    return !!this.selectedTeam;
  }

  canFinish(): boolean {
    return !!this.selectedAvatar;
  }

  finishOnboarding() {
    if (!this.canFinish()) return;
    this.isSavingOnboarding = true;

    const userId = this.authService.getUserId();

    this.registroService.updateProfile(userId, {
      favoriteTeam: this.selectedTeam?.name,
      avatarUrl: this.selectedAvatar
    }).subscribe({
      next: (response) => {
        // ✅ El PUT devuelve token nuevo — actualizarlo
        if (response.token) {
          this.authService.saveToken(response.token);
        }
        if (this.selectedAvatar) {
          this.authService.actualizarAvatar(this.selectedAvatar);
        }
        if (this.selectedTeam) {
          this.authService.actualizarEquipo(this.selectedTeam);
        }
        this.isSavingOnboarding = false;
        this.showOnboarding = false;
        this.router.navigate(['/home']);
      },
      error: (err) => {
        console.error('Error guardando perfil:', err);
        this.isSavingOnboarding = false;
        this.showOnboarding = false;
        this.router.navigate(['/home']); // no bloqueamos al usuario
      }
    });
  }


  skipOnboarding() {
    this.showOnboarding = false;
    this.router.navigate(['/home']);
  }

  // ===========================
  // REENVIAR CÓDIGO
  // ===========================

  resendCode() {
    if (this.isResending) return;
    this.email = localStorage.getItem('email') || '';
    this.isResending = true;
    this.showErrorPopup = false;
    this.showPopup = false;

    this.registroService.resendCode(this.email).subscribe({
      next: () => {
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
}
