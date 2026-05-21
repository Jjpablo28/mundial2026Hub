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
        console.log("Respuesta del backend:", res);

        const container = document.getElementById('paymentCard-container');
        if (container) {
          container.innerHTML = '';
        }

        // Llamamos al método pasando exactamente los 4 parámetros definidos en tu archivo de servicio
        await this.paymentService.renderPaymentBrick(
          'paymentCard-container',
          res.id,
          res.amount,
          (status: string) => {
            this.ngZone.run(() => {
              this.paymentStatus = status;
              console.log("Estado del pago actualizado en Angular:", this.paymentStatus);

              if (status === 'approved') {
                setTimeout(() => {
                  this.router.navigate(['/home']);
                }, 2500);
              }
            });
          }
        );
        this.cargandoBrick = false;
      },
      error: (err) => {
        this.cargandoBrick = false;
        console.error("Error al obtener preferencia", err);
        this.errorMessage = err.error?.error || 'Error al procesar la solicitud.';
      }
    });
  }
}
