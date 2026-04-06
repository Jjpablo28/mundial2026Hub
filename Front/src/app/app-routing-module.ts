import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login.component/login.component';
import { RegistroComponent } from './registro.component/registro.component';
import { VerifyComponent } from './verify.component/verify.component';
import { HomeComponent } from './home.component/home.component';
import { Forbidden } from './statusCode/forbidden/forbidden';
import { AuthGuard } from './security/AuthGuard';
import { LandingComponent } from './landing.component/landing.component';


const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegistroComponent },
  { path: 'verify', component: VerifyComponent },
  { path: 'forbidden', component: Forbidden },
  { path: 'home', component: HomeComponent,  canActivate: [AuthGuard], data: { rolesPermitidos: ['TEMPORAL'] }},
  {path:'landing',component: LandingComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
