import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RegistroService } from './registro.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registro',
  standalone: false,
  templateUrl: './registro.component.html',
  styleUrls: ['./registro.component.scss']
})
export class RegistroComponent implements OnInit {

  form = {
    user: '',
    email: '',
    country: '',
    city: '',
    team: ''
  };

  countries: string[] = [];
  cities: string[] = [];
  teams: string[] = [];

  constructor(private registroService: RegistroService) {}

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
    console.log(this.form);

  }
}
