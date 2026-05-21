import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Team } from '../home.component/home.component';
import { AuthService } from '../security/AuthService';
import { HomeComponentService } from '../home.component/home.component.service';
import { RegistroService } from '../registro.component/registro.service';

@Component({
  selector: 'app-account',
  standalone: false,
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss',
})
export class AccountComponent implements OnInit {

  constructor(private authService: AuthService,
              private homeService: HomeComponentService,
              private registroService: RegistroService,
              private cdr: ChangeDetectorRef) {
  }

  avatarUrls: string[] = [
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

  imageUrl: string = this.avatarUrls[0];

  public userName: string = '';
  public name: string = '';
  public email: string = '';
  public oldPassword: string = '';
  public newPassword: string = '';
  public showOldPass: boolean = false;
  public showNewPass: boolean = false;

  public country: string = '';
  public city: string = '';
  public countries: string[] = [];
  public cities: string[] = [];
  public teams: Team[] = [];

  public toast = { show: false, message: '', type: 'success' };
  private toastTimeout: any;

  public menuActivo: string = 'dashboard';
  public isDropdownOpen: boolean = false;
  public selectedTeam: Team | null = null;
  public isUpdating: boolean = false;
  public isLoading: boolean = true;

  private originalData: any = {};

  get hasChanges(): boolean {
    const currentTeamName = this.selectedTeam ? this.selectedTeam.name : null;
    return (
      this.name !== this.originalData.name ||
      this.userName !== this.originalData.userName ||
      this.country !== this.originalData.country ||
      this.city !== this.originalData.city ||
      currentTeamName !== this.originalData.favoriteTeam ||
      this.imageUrl !== this.originalData.avatarUrl
    );
  }

  selectAvatar(url: string) {
    this.imageUrl = url;
    this.authService.actualizarAvatar(url);
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectTeam(team: Team) {
    this.selectedTeam = team;
    this.isDropdownOpen = false;
    this.authService.actualizarEquipo(team);
  }

  updateUser() {
    if (!this.hasChanges) {
      this.showToast('No se detectaron cambios para actualizar.', 'info');
      return;
    }

    this.isUpdating = true;
    let id: number | null = this.authService.getUserId();
    const body = {
      userName: this.userName,
      name: this.name,
      email: this.email,
      country: this.country,
      city: this.city,
      favoriteTeam: this.selectedTeam ? this.selectedTeam.name : null,
      avatarUrl: this.imageUrl
    };

    this.homeService.updateUser(body, id).subscribe({
      next: res => {
        if (res.token) {
          this.authService.saveToken(res.token);
        }

        this.authService.actualizarEstadoUsuario({
          name: this.name,
          userName: this.userName,
          country: this.country,
          city: this.city,
          favoriteTeam: body.favoriteTeam,
          avatarUrl: this.imageUrl
        });


        this.authService.actualizarAvatar(this.imageUrl);
        this.authService.actualizarEquipo(this.selectedTeam);

        this.originalData = {
          name: this.name,
          userName: this.userName,
          country: this.country,
          city: this.city,
          favoriteTeam: this.selectedTeam ? this.selectedTeam.name : null,
          avatarUrl: this.imageUrl
        };

        this.isUpdating = false;
        this.showToast('¡Información actualizada con éxito!', 'success');
        this.cdr.detectChanges();
      },
      error: err => {
        console.log(err);
        this.isUpdating = false;
        this.showToast('Hubo un error al guardar los cambios.', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  onCountryChange(country: string) {
    if (!country) return;

    this.cities = [];

    this.registroService.getCitiesByCountry(country)
      .subscribe(cities => {
        this.cities = cities;
        this.cdr.detectChanges();
      });
  }

  deleteAccount() {
    let id: number | null = this.authService.getUserId();
    this.homeService.deleteAccount(id).subscribe({
      next: res => {
        this.logout();
      },
      error: err => {
        console.log(err);
      }
    });
  }
  cambiarMenu(menu: string): void {
    if (this.menuActivo === menu) {
      this.menuActivo = 'dashboard';
    } else {
      this.menuActivo = menu;
    }
  }
  updatePassword() {

    if (!this.oldPassword || !this.newPassword) {
      this.showToast('Ingresa la contraseña actual y la nueva para continuar.', 'info');
      return;
    }

    if (this.oldPassword === this.newPassword) {
      this.showToast('La nueva contraseña debe ser diferente a la actual.', 'info');
      return;
    }

    this.isUpdating = true;
    let id: number | null = this.authService.getUserId();
    const body = {
      oldPassword: this.oldPassword,
      newPassword: this.newPassword
    };

    this.homeService.updatePassword(body, id).subscribe({
      next: res => {
        this.oldPassword = '';
        this.newPassword = '';
        this.isUpdating = false;
        this.showToast('¡Contraseña actualizada con éxito!', 'success');
        this.cdr.detectChanges();
      },
      error: err => {
        console.log(err);
        this.isUpdating = false;
        this.showToast('Hubo un error al actualizar la contraseña.', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  showToast(message: string, type: 'success' | 'error' | 'info' = 'success') {
    this.toast = { show: true, message, type };

    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
    this.toastTimeout = setTimeout(() => {
      this.closeToast();
    }, 3000);
  }

  closeToast() {
    this.toast.show = false;
    this.cdr.detectChanges();
  }

  logout() {
    this.authService.logout();
  }

  ngOnInit(): void {
    this.teams = this.authService.getTeamsValue() || [];
    this.countries = this.authService.getCountriesValue() || [];
    this.userName = this.authService.getUserName() || 'Usuario';

    const user = this.authService.getUserValue();

    if (user) {
      this.email = user.email;
      this.name = user.name;
      this.imageUrl = user.avatarUrl;
      this.country = user.country;
      this.city = user.city;

      if (user.favoriteTeam && this.teams.length > 0) {
        const equipoEncontrado = this.teams.find(t => t.name === user.favoriteTeam);
        if (equipoEncontrado) {
          this.selectedTeam = equipoEncontrado;
        } else {
          this.selectedTeam = { name: user.favoriteTeam, crest: 'assets/img/placeholder.png', shortName: '', fifaRanking: '', groupName: '' };
        }
      }

      this.originalData = {
        name: this.name,
        userName: this.userName,
        country: this.country,
        city: this.city,
        favoriteTeam: user.favoriteTeam,
        avatarUrl: this.imageUrl
      };

      if (this.country) {
        this.onCountryChange(this.country);
      }
    }

    this.isLoading = false;
    this.cdr.detectChanges();
  }
}
