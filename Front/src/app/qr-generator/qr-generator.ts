import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { TradeHttpService } from './trade-http.service';
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

  // ✅ Guardamos el userAId para poder notificarle cuando B conecte el WS
  private userAId: number = 0;
  private sessionUuidDestino: string = '';

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

    // ✅ filter descarta el '' inicial del BehaviorSubject
    this.notificationSub = this.wsService.getNotifications().pipe(
      filter(msg => !!msg)
    ).subscribe({
      next: (notificacion) => {
        console.log("✉️ Notificación recibida del servidor:", notificacion);
        if (notificacion.includes("JOINED")) {
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

    // Paso 1: HTTP join para registrar al Usuario B en la sesión
    this.httpService.joinSession(uuidDestino, this.myUserId).subscribe({
      next: (mensaje: string) => {
        console.log("✅ Unión HTTP exitosa:", mensaje);

        // Extraemos el userAId del mensaje del servidor
        // "Te has unido exitosamente a la sesión de intercambio con el usuario ID: 15"
        const match = mensaje.match(/ID:\s*(\d+)/);
        this.userAId = match ? Number(match[1]) : 0;
        this.sessionUuidDestino = uuidDestino;

        console.log("👤 Usuario A identificado:", this.userAId);

        this.wsService.setSessionData(uuidDestino, this.myUserId);
        const token = this.authService.getToken() || '';

        // Paso 2: Conectar WebSocket
        this.wsService.connect(token, this.myUserId, (status) => {
          console.log("🟢 WebSocket conectado como Usuario B:", status);

          // ✅ Paso 3: AHORA que el WS está listo, notificamos al Usuario A por STOMP
          // Esto garantiza que el mensaje llega DESPUÉS de que ambos están suscritos
          this.wsService.notifyJoin(uuidDestino, this.myUserId, this.userAId);

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
