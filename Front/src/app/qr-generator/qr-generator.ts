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

  private userAId: number = 0;
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

    this.notificationSub = this.wsService.getNotifications().pipe(
      filter(msg => !!msg && msg.includes("JOINED"))
    ).subscribe({
      next: (notificacion) => {
        console.log("✉️ JOINED recibido, navegando a la mesa...");
        this.router.navigate(['/websocket']);
      }
    });
  }

  generarSala(): void {
    console.log("⏳ Generando sala...");
    this.qrCode = '';
    this.qrData = null;
    this.cargandoSala = true;
    this.cdr.detectChanges();

    this.httpService.createSession(this.myUserId).subscribe({
      next: (response: any) => {
        this.qrData = response;
        this.qrCode = response.qrBase64;
        this.cargandoSala = false;
        console.log("✅ QR generado:", response.uuid);
        this.cdr.detectChanges();

        this.wsService.setSessionData(response.uuid, this.myUserId);
        const token = this.authService.getToken() || '';

        // ✅ Conectar WebSocket UNA sola vez y esperar
        this.wsService.connect(token, this.myUserId, (status) => {
          console.log("🟢 Esperando contraparte...");
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        console.error("❌ Error al crear sesión:", err);
        this.cargandoSala = false;
        this.cdr.detectChanges();
        alert("Error de comunicación con MundialHub.");
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
        console.log("✅ Join HTTP exitoso:", mensaje);

        const match = mensaje.match(/ID:\s*(\d+)/);
        this.userAId = match ? Number(match[1]) : 0;
        console.log("👤 Usuario A:", this.userAId);

        this.wsService.setSessionData(uuidDestino, this.myUserId);
        const token = this.authService.getToken() || '';

        // ✅ Conectar WS y solo después de estar suscrito enviar el notifyJoin
        this.wsService.connect(token, this.myUserId, (status) => {
          console.log("🟢 WS conectado como B:", status);

          // Navegamos primero para que trade-screen se suscriba a notificaciones
          this.uniendose = false;
          this.cdr.detectChanges();
          this.router.navigate(['/websocket']);

          // Luego notificamos al A con delay para dar tiempo a que todo se estabilice
          setTimeout(() => {
            console.log("📤 Enviando notifyJoin...");
            this.wsService.notifyJoin(uuidDestino, this.myUserId, this.userAId);
          }, 1000);
        });
      },
      error: (err) => {
        console.error("❌ Error al unirse:", err);
        this.uniendose = false;
        this.cdr.detectChanges();
        alert(err?.error || "Error al unirse a la sesión.");
      }
    });
  }

  ngOnDestroy(): void {
    if (this.notificationSub) this.notificationSub.unsubscribe();
  }
}
