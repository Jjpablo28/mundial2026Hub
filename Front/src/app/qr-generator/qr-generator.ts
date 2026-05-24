import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { TradeHttpService, QrResponse } from './trade-http.service';
import { TradeWebSocketService } from '../trade-screen/trade-websocket.service';
import { Router } from '@angular/router';
import { AuthService } from '../security/AuthService';
import { Subscription } from 'rxjs';

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
  uniendose: boolean = false; // ✅ Estado de carga para el join

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

    this.notificationSub = this.wsService.getNotifications().subscribe({
      next: (notificacion) => {
        console.log("✉️ Notificación recibida del servidor:", notificacion);
        // El Usuario A recibe este evento cuando el B se une
        if (notificacion.includes("JOINED") || notificacion.includes("conectado") || notificacion.includes("START")) {
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

        // Guardamos la sesión y conectamos el WebSocket para esperar al Usuario B
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

  // ✅ FIX CRÍTICO: El join ahora hace primero el HTTP y luego abre el WebSocket
  unirseAIntercambio(): void {
    if (!this.sessionUuidInput.trim()) {
      alert("Por favor ingresa un ID de sesión válido.");
      return;
    }

    const uuidDestino = this.sessionUuidInput.trim();
    this.uniendose = true;
    this.cdr.detectChanges();

    // Paso 1: Registrar al Usuario B en la sesión vía HTTP
    this.httpService.joinSession(uuidDestino, this.myUserId).subscribe({
      next: (mensaje: string) => {
        console.log("✅ Unión HTTP exitosa:", mensaje);

        // Paso 2: Guardar datos de sesión en el servicio compartido
        this.wsService.setSessionData(uuidDestino, this.myUserId);
        const token = this.authService.getToken() || '';

        // Paso 3: Conectar WebSocket y luego navegar a la mesa
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
