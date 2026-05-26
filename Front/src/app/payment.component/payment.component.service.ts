import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { AuthService } from '../security/AuthService';

declare var MercadoPago: any;

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private mp: any;
  private bricksBuilder: any;
  private publicKey = 'TEST-f4351136-6c27-483f-99b5-7466c1737b30';
  private apiUrl = 'https://mundial2026hub.onrender.com/payments';

  constructor(private http: HttpClient, private authService: AuthService) {}

  loadSdk(): Promise<void> {
    return new Promise((resolve) => {
      if (document.getElementById('mp-sdk')) {
        resolve();
        return;
      }
      const script = document.createElement('script');
      script.id = 'mp-sdk';
      script.src = 'https://sdk.mercadopago.com/js/v2';
      script.onload = () => resolve();
      document.body.appendChild(script);
    });
  }

  initMp() {
    this.mp = new MercadoPago(this.publicKey, { locale: 'es-CO' });
    this.bricksBuilder = this.mp.bricks();
  }

  createPreference(tokenQuantity: number, currency: string): Observable<any> {
    let id: number | null = this.authService.getUserId();

    if (id === null) {
      throw new Error('No se encontró un ID de usuario válido en la sesión.');
    }

    const params = new HttpParams().set('usuario', id);

    const body = {
      quantity: tokenQuantity,
      currency: currency
    };

    return this.http.post<any>(`${this.apiUrl}/preference`, body, { params });
  }

  async renderPaymentBrick(
    containerId: string,
    preferenceId: string,
    amount: number,
    onStatusChange: (status: string) => void
  ) {
    if (!this.bricksBuilder && this.mp) {
      this.bricksBuilder = this.mp.bricks();
    }

    const settings = {
      initialization: {
        amount: amount,
        preferenceId: preferenceId,
      },
      customization: {
        visual: {
          hideFormTitle: true,
          style: { theme: 'default' }
        },
        paymentMethods: {
          creditCard: 'all',
          debitCard: 'all',
          ticket: 'all',
          bankTransfer: 'all',
          maxInstallments: 1
        }
      },
      callbacks: {
        onReady: () => {
          console.log('Payment Brick: Listo para usar de forma segura');
        },
        onSubmit: ({ formData }: any) => {
          return new Promise<void>((resolve, reject) => {
            const paymentData = {
              ...formData,
              preference_id: preferenceId
            };

            this.processPayment(paymentData)
              .then((res: any) => {
                console.log('Pago enviado al servidor:', res);

                let estadoReal = '';
                let puntosActualizados = 0;

                if (res && res.status) {
                  estadoReal = res.status;
                } else if (res && res.response) {
                  try {
                    const parsedResponse = typeof res.response === 'string' ? JSON.parse(res.response) : res.response;
                    estadoReal = parsedResponse.status;
                  } catch (e) {
                    console.error("Error al parsear el sub-objeto response", e);
                  }
                }

                if (estadoReal === 'approved') {
                  // 1. Extraemos los puntos actuales devueltos por tu Spring Boot si vienen en la respuesta
                  // (Por ejemplo, si tu backend retorna el objeto usuario actualizado)
                  if (res.user && res.user.totalPoints !== undefined) {
                    this.authService.setTotalPoints(res.user.totalPoints);
                  } else {
                    // 2. Si el backend no devuelve el usuario completo, recalculamos sumando al flujo actual
                    this.http.get<any>(`https://mundial2026hub.onrender.com/users/${this.authService.getUserId()}`).subscribe({
                      next: (user) => this.authService.setTotalPoints(user.totalPoints)
                    });
                  }
                }

                if (estadoReal) {
                  onStatusChange(estadoReal);
                } else {
                  onStatusChange('rejected');
                }

                resolve();
              })
              .catch((err: any) => {
                console.error('Error procesando el pago en servidor:', err);
                onStatusChange('rejected');
                reject(err);
              });
          });

        },
        onError: (error: any) => {
          console.error('Error en el Brick:', error);
        }
      }
    };

    return await this.bricksBuilder.create('payment', containerId, settings);
  }

  private async processPayment(formData: any) {
    return firstValueFrom(
      this.http.post(`${this.apiUrl}/charge`, formData)
    );
  }
}
