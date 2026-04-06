import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
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




  countries: string[] = [];
  cities: string[] = [];
  teams: string[] = [];

  constructor(private registroService: RegistroService, private router: Router, private authService: AuthService) {}


  ngOnInit(): void {
    this.loadCountries();
    this.loadTeams()

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
      });
  }
  loadTeams(){
    this.registroService.getTeams().subscribe(res => {
      this.teams = res;
    });
  }

  register() {
    this.isLoading = true; // Activamos el loader al iniciar

    const body = {
      userName: this.userName,
      name: this.name,
      email: this.email,
      identification: this.id,
      country: this.country,
      city: this.city,
      password: this.password
    };

    this.registroService.register(body,'TEMPORAL').subscribe({
      next: (response) => {
        console.log('Registro exitoso', response);
        localStorage.setItem('email', this.email);
        this.isLoading = false;
        this.router.navigate(['/verify']);
      },
      error: (err) => {
        console.error('Error en registro', err);
        this.isLoading = false;
        alert('Hubo un error al crear la cuenta');
      }
    });
  }
}
