import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login.component/login.component';
import { RegistroComponent } from './registro.component/registro.component';
import { VerifyComponent } from './verify.component/verify.component';
import { HomeComponent } from './home.component/home.component';
import { Forbidden } from './statusCode/forbidden/forbidden';
import { AuthGuard } from './security/AuthGuard';
import { LandingComponent } from './landing.component/landing.component';
import { PaymentComponent } from './payment.component/payment.component';
import {QrGenerator} from './qr-generator/qr-generator';
import {TradeScreen} from './trade-screen/trade-screen';
import {GuestGuard} from './security/guest.guard';


const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'landing', component: LandingComponent },
  { path: 'login',    component: LoginComponent,    canActivate: [GuestGuard] },
  { path: 'register', component: RegistroComponent, canActivate: [GuestGuard] },
  { path: 'verify',   component: VerifyComponent,   canActivate: [GuestGuard] },
  { path: 'forbidden', component: Forbidden },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard], data: { rolesPermitidos: ['USER', 'ADMIN'] }},
  { path: 'payment', component: PaymentComponent },
  { path: 'qr', component: QrGenerator },
  { path: 'websocket', component: TradeScreen },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
