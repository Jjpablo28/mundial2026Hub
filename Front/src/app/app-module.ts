import { APP_INITIALIZER, NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, NgForOf, NgIf } from '@angular/common';

import {
  HttpClientModule,
  HttpClient,
  provideHttpClient,
  HTTP_INTERCEPTORS,
} from '@angular/common/http';
import { LoginComponent } from './login.component/login.component';
import { HeaderComponent } from './core/header/header.component';
import { FooterComponent } from './core/footer/footer.component';
import { RegistroComponent } from './registro.component/registro.component';

import { VerifyComponent } from './verify.component/verify.component';
import { HomeComponent } from './home.component/home.component';
import { Forbidden } from './statusCode/forbidden/forbidden';
import { AuthInterceptor } from './security/AuthInterceptor';
import { LandingComponent } from './landing.component/landing.component';
import { AccountComponent } from './account.component/account.component';
import { AuthService } from './security/AuthService';
import { MatchComponent } from './match.component/match.component';
import { PaymentComponent } from './payment.component/payment.component';
import { Album } from './album/album';
import { QrGenerator } from './qr-generator/qr-generator';
import { TradeScreen } from './trade-screen/trade-screen';

export function initializeApp(authService: AuthService) {
  return () => authService.cargarDatosIniciales();
}
@NgModule({
  declarations: [
    App,
    LoginComponent,
    HeaderComponent,
    FooterComponent,
    RegistroComponent,
    VerifyComponent,
    HomeComponent,
    Forbidden,
    LandingComponent,
    AccountComponent,
    MatchComponent,
    PaymentComponent,
    Album,
    QrGenerator,
    TradeScreen,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    NgForOf,
    /*TranslateModule.forRoot({

      loader: {
        provide: TranslateLoader, // Token para el servicio de carga de traducción.
        useFactory: HttpLoaderFactory, // Función de fábrica que crea una instancia de TranslateHttpLoader.
        deps: [HttpClient] // Dependencias que se inyectarán en la función de fábrica (en este caso, HttpClient).
      }
    }),*/
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),

    provideHttpClient(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AuthService],
      multi: true,
    },
  ],
  bootstrap: [App],
})
export class AppModule {}
