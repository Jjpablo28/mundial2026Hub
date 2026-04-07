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


  public userRole: 'admin' | 'user' = 'user';
  public userName: string = '';
  public name: string = '';
  public email: string = '';
  public imageUrl: string = '';

  public country: string = '';
  public city: string = '';
  public countries: string[] = [];
  public cities: string[] = [];
  public teams: Team[] = [];


  public isDropdownOpen: boolean = false;
  public selectedTeam: Team | null = null;

  public menuActivo: string = 'dashboard';

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }
  cambiarMenu(menu: string): void {
    this.menuActivo = menu;
  }

  logout() {
    this.authService.logout();
  }

  getuser() {
    let id: number | null = this.authService.getUserId();
    this.homeService.getUserById(id).subscribe({
      next: res => {
        console.log(res);

        this.email = res.email;
        this.name = res.name;
        this.country = res.country;
        this.city = res.city;
        this.imageUrl=res.avatarUrl;
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
      this.teams=res;
        this.cdr.detectChanges();
      },
      error: err => {

      }
    });
  }

  ngOnInit(): void {
    this.getuser();
    this.loadCountries();
    this.loadTeams()
    this.cdr.detectChanges();

    this.userName = this.authService.getUserName() || 'Usuario';

  }
}
