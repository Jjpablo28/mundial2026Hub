import { Component, OnInit } from '@angular/core';
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

  countries: any[] = [];
  cities: string[] = [];
  teams: string[] = [];

  constructor(private registroService: RegistroService) {}

  ngOnInit(): void {
    this.loadCountries();
  }

  loadCountries() {
    this.registroService.getCountries().subscribe(res => {
      this.countries = res.data;
    });
  }

  onCountryChange() {
    this.registroService.getCities(this.form.country)
      .subscribe(res => {
        this.cities = res.data;
      });
    this.teams = [];
    this.form.team = '';
  }

  register() {
    console.log(this.form);

  }
}
