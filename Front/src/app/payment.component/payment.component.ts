import { Component, OnInit, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { PaymentService } from './payment.component.service';

@Component({
  selector: 'app-payment.component',
  standalone: false,
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.scss',
})
export class PaymentComponent implements OnInit {

  tokensToBuy = 500;
  currency = 'COP';
  errorMessage = '';
  cargandoBrick = false;
  paymentStatus = '';

  constructor(
    private paymentService: PaymentService,
    private ngZone: NgZone,
    private router: Router
  ) {}

  async ngOnInit() {
    await this.paymentService.loadSdk();
    this.paymentService.initMp();
  }

  prepararCompra() {
    if (this.tokensToBuy < 500) {
      this.errorMessage = 'La cantidad mínima de compra son 500 tokens ($5.000 COP).';
      return;
    }

    this.errorMessage = '';
    this.cargandoBrick = true;
    this.paymentStatus = '';

    this.paymentService.createPreference(this.tokensToBuy, this.currency).subscribe({
      next: async (res) => {
        const container = document.getElementById('paymentCard-container');
        if (container) container.innerHTML = '';

        await this.paymentService.renderPaymentBrick(
          'paymentCard-container',
          res.id,
          res.amount,
          (status: string) => {
            this.ngZone.run(() => {
              this.paymentStatus = status;
              if (status === 'approved') {
                setTimeout(() => this.router.navigate(['/home']), 2500);
              }
              // ✅ Mostrar error si el pago fue rechazado
              if (status === 'rejected') {
                this.errorMessage = 'Tu pago fue rechazado. Verifica los datos de tu tarjeta.';
              }
            });
          }
        );
        this.cargandoBrick = false;
      },
      error: (err) => {
        this.cargandoBrick = false;
        // ✅ Mensaje más específico según el error
        if (err.status === 400) {
          this.errorMessage = err.error?.error || 'Solicitud inválida.';
        } else if (err.status === 0) {
          this.errorMessage = 'Sin conexión con el servidor. Intenta más tarde.';
        } else {
          this.errorMessage = err.error?.error || 'Error al procesar la solicitud.';
        }
      }
    });
  }
}
