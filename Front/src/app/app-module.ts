import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule, NgForOf, NgIf} from '@angular/common';
import {HttpClientModule, HttpClient, provideHttpClient, HTTP_INTERCEPTORS} from '@angular/common/http';
import {LoginComponent} from './login.component/login.component';
import {HeaderComponent} from './core/header/header.component';
import {FooterComponent} from './core/footer/footer.component';
import {RegistroComponent} from './registro.component/registro.component';



@NgModule({
  declarations: [
    App,
    LoginComponent,
    HeaderComponent,
    FooterComponent,
    RegistroComponent

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
    /*{ provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },*/
    provideHttpClient()
  ],
  bootstrap: [App]
})
export class AppModule { }
