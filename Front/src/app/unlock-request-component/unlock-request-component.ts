import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SupportService } from '../support-component/support.service';

@Component({
  selector: 'app-unlock-request',
  standalone: false,
  templateUrl: 'unlock-request-component.html',
  styleUrl: 'unlock-request-component.scss'
})
export class UnlockRequestComponent implements OnInit {

  state: 'loading' | 'success' | 'error' = 'loading';
  errorMsg = '';

  constructor(
    private route: ActivatedRoute,
    private supportService: SupportService
  ) {}

  ngOnInit(): void {
    const userId = Number(this.route.snapshot.queryParamMap.get('userId'));

    if (!userId) {
      this.state    = 'error';
      this.errorMsg = 'Enlace inválido o expirado.';
      return;
    }

    this.supportService.createTicket(
      {
        targetUserId: userId,
        ticketType:   'UNLOCK_ACCOUNT',
        notes:        'Solicitud de desbloqueo generada desde correo de cuenta bloqueada.'
      },
      1 // agentId del sistema — ajusta si tienes un ID de sistema definido
    ).subscribe({
      next:  () => this.state = 'success',
      error: (err) => {
        this.state    = 'error';
        this.errorMsg = err?.error?.message || 'No se pudo enviar la solicitud. Intenta de nuevo más tarde.';
      }
    });
  }
}
