import { Routes } from '@angular/router';
import { LoginComponent } from './login.component/login.component';
import { RegistroComponent } from './registro.component/registro.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'register', component: RegistroComponent }
];
