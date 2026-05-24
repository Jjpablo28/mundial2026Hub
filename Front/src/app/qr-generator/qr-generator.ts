import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { TradeHttpService, QrResponse } from './trade-http.service';
import { TradeWebSocketService } from '../trade-screen/trade-websocket.service';
import { Router } from '@angular/router';
import { AuthService } from '../security/AuthService';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-qr-generator',
  templateUrl: './qr-generator.html',
  styleUrls: ['./qr-generator.scss'],
  standalone: false
})
export class QrGenerator implements OnInit, OnDestroy {
  myUserId!: number;
  qrData: any = null;
  sessionUuidInput: string = '';
  qrCode: string = '';
  cargandoSala: boolean = false;
  uniendose: boolean = false;

  private notificationSub!: Subscription;

  constructor(
    private httpService: TradeHttpService,
    private wsService: TradeWebSocketService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.myUserId = this.authService.getUserId() || 1;

    // ✅ FIX: filter(msg => !!msg) descarta el string vacío inicial del BehaviorSubject
    // Sin esto, '' se emite al suscribirse y aunque no navega, puede causar problemas
    // Con BehaviorSubject, si JOINED llegó antes de navegar aquí, se emite de inmediato
    this.notificationSub = this.wsService.getNotifications().pipe(
      filter(msg => !!msg) // Descarta string vacío ''
    ).subscribe({
      next: (notificacion) => {
        console.log("✉️ Notificación recibida del servidor:", notificacion);
        if (notificacion.includes("JOINED") || notificacion.includes("conectado") || notificacion.includes("START")) {
          console.log("🚀 Navegando a la mesa de intercambio...");
          this.router.navigate(['/websocket']);
        }
      }
    });
  }

  generarSala(): void {
    console.log("⏳ Disparando creación de sala bajo demanda...");

    this.qrCode = '';
    this.qrData = null;
    this.cargandoSala = true;
    this.cdr.detectChanges();

    this.httpService.createSession(this.myUserId).subscribe({
      next: (response: any) => {
        this.qrData = response;
        this.qrCode = response.qrBase64;
        this.cargandoSala = false;

        console.log("✅ Código QR generado:", response.uuid);
        this.cdr.detectChanges();

        this.wsService.setSessionData(response.uuid, this.myUserId);
        const token = this.authService.getToken() || '';

        this.wsService.connect(token, this.myUserId, (status) => {
          console.log("🟢 Túnel WebSocket abierto, esperando contraparte:", status);
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        console.error("❌ Error HTTP al crear sesión:", err);
        this.cargandoSala = false;
        this.cdr.detectChanges();
        alert("Error de comunicación con MundialHub. Intenta de nuevo.");
      }
    });
  }

  cargarNuevoQr(): void {
    this.generarSala();
  }

  unirseAIntercambio(): void {
    if (!this.sessionUuidInput.trim()) {
      alert("Por favor ingresa un ID de sesión válido.");
      return;
    }

    const uuidDestino = this.sessionUuidInput.trim();
    this.uniendose = true;
    this.cdr.detectChanges();

    this.httpService.joinSession(uuidDestino, this.myUserId).subscribe({
      next: (mensaje: string) => {
        console.log("✅ Unión HTTP exitosa:", mensaje);

        this.wsService.setSessionData(uuidDestino, this.myUserId);
        const token = this.authService.getToken() || '';

        this.wsService.connect(token, this.myUserId, (status) => {
          console.log("🟢 WebSocket conectado como Usuario B:", status);
          this.uniendose = false;
          this.cdr.detectChanges();
          this.router.navigate(['/websocket']);
        });
      },
      error: (err) => {
        console.error("❌ Error al unirse a la sesión:", err);
        this.uniendose = false;
        this.cdr.detectChanges();
        const mensaje = err?.error || err?.message || "Error desconocido al unirse a la sesión.";
        alert(mensaje);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.notificationSub) {
      this.notificationSub.unsubscribe();
    }
  }
}
