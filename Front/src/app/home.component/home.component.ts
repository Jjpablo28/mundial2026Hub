import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuthService } from '../security/AuthService';
import { HomeComponentService } from './home.component.service';
import { RegistroService } from '../registro.component/registro.service';

export interface Team {
  name: string;
  crest: string;
}

@Component({
  selector: 'app-home.component',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})


export class HomeComponent implements OnInit {

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


  public userRole: 'admin' | 'user' = 'user';
  public userName: string = '';
  public name: string = '';
  public email: string = '';
  public oldPassword: string = '';
  public newPassword: string = '';
  imageUrl: string = this.avatarUrls[0];
  public showOldPass: boolean = false;
  public showNewPass: boolean = false;

  public country: string = '';
  public city: string = '';
  public countries: string[] = [];
  public cities: string[] = [];
  public teams: Team[] = [];


  public isDropdownOpen: boolean = false;
  public selectedTeam: Team | null = null;

  public menuActivo: string = 'dashboard';


  selectAvatar(url: string) {
    this.imageUrl = url;
  }
  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  cambiarMenu(menu: string): void {
    this.menuActivo = menu;
  }

  logout() {
    this.authService.logout();
  }

  deleteAccount() {
    let id: number | null = this.authService.getUserId();
    this.homeService.deleteAccount(id).subscribe({
      next: res => {
        alert('cuenta eliminada con exito');
        this.logout()

      },
      error: err => {
        console.log(err);
      }
    });
  }

  getUser() {
    let id: number | null = this.authService.getUserId();
    this.homeService.getUserById(id).subscribe({
      next: res => {
        this.email = res.email;
        this.name = res.name;
        this.country = res.country;
        this.city = res.city;
        this.imageUrl = res.avatarUrl;

        if (res.favoriteTeam && this.teams && this.teams.length > 0) {
          const equipoEncontrado = this.teams.find(t => t.name === res.favoriteTeam);
          this.cdr.detectChanges();
          if (equipoEncontrado) {
            this.selectedTeam = equipoEncontrado;
          } else {
            this.selectedTeam = { name: res.favoriteTeam, crest: '' };
          }
        }
        this.onCountryChange(this.country);
        this.cdr.detectChanges();
      },
      error: err => {
        console.log(err);
      }
    });

  }

  selectTeam(team: Team) {
    this.selectedTeam = team;
    this.isDropdownOpen = false;

  }

  loadCountries() {
    this.registroService.getCountries().subscribe(res => {
      this.countries = res;
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

  loadTeams() {
    this.registroService.getTeams().subscribe({
      next: res => {
        this.teams = res;
        this.cdr.detectChanges();
        console.log(res)
      },
      error: err => {

      }
    });
  }

  updatePassword() {
    let id: number | null = this.authService.getUserId();
    const body = {
      oldPassword: this.oldPassword,
      newPassword: this.newPassword
    };

    this.homeService.updatePassword(body, id).subscribe({
      next: res => {
        console.log(res);

      },
      error: err => {
        console.log(err);
      }
    });

  }

  updateUser() {
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
        console.log(res);
        if (res.token) {
          this.authService.saveToken(res.token);
        }
      },
      error: err => {
        console.log(err);
      }
    });
  }

  ngOnInit(): void {
    this.getUser();
    this.cdr.detectChanges();
    this.loadCountries();
    this.cdr.detectChanges();
    this.loadTeams();
    this.cdr.detectChanges();

    this.userName = this.authService.getUserName() || 'Usuario';

  }
}
