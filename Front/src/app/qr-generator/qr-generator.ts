import { Component, OnDestroy, OnInit, ChangeDetectorRef, ElementRef, ViewChild } from '@angular/core';
import { TradeHttpService } from './trade-http.service';
import { TradeWebSocketService } from '../trade-screen/trade-websocket.service';
import { Router } from '@angular/router';
import { AuthService } from '../security/AuthService';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

/** Modos de unión disponibles para el Usuario B */
type JoinMode = 'code' | 'email' | 'uuid';

@Component({
  selector: 'app-qr-generator',
  templateUrl: './qr-generator.html',
  styleUrls: ['./qr-generator.scss'],
  standalone: false
})
export class QrGenerator implements OnInit, OnDestroy {

  // ── Estado del usuario ───────────────────────────────────────────────────
  myUserId!: number;

  // ── Estado del QR generado (Usuario A) ──────────────────────────────────
  qrData: any = null;
  qrCode: string = '';
  /** Código alfanumérico corto a mostrar junto al QR (ej: "AB3X7Q"). */
  shortCodeDisplay: string = '';
  cargandoSala: boolean = false;

  // ── Estado del formulario de unión (Usuario B) ───────────────────────────
  /** Modo seleccionado por el usuario para unirse: código corto, email o UUID. */
  joinMode: JoinMode = 'code';

  /** Input para el código alfanumérico (ej: "AB3X7Q"). */
  shortCodeInput: string = '';

  /** Input para el email del Usuario A. */
  emailInput: string = '';

  /** Input para el UUID completo (modo avanzado / escaneo manual). */
  sessionUuidInput: string = '';

  uniendose: boolean = false;

  // ── Feedback ─────────────────────────────────────────────────────────────
  showError: boolean = false;
  errorMessage: string = '';
  showSuccess: boolean = false;

  // ── Scanner QR ──────────────────────────────────────────────────────────
  escaneando = false;
  scannedUuid: string | null = null;
  private scanStream: MediaStream | null = null;
  private scanInterval: any = null;
  @ViewChild('scannerVideo') scannerVideo!: ElementRef<HTMLVideoElement>;

  // ── Internos ─────────────────────────────────────────────────────────────
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
      filter(msg => !!msg && msg.includes('JOINED'))
    ).subscribe({
      next: () => {
        console.log('✉️ JOINED recibido, navegando a la mesa...');
        this.router.navigate(['/websocket']);
      }
    });
  }

  // ── Generar sala (Usuario A) ─────────────────────────────────────────────

  generarSala(): void {
    this.qrCode = '';
    this.qrData = null;
    this.shortCodeDisplay = '';
    this.cargandoSala = true;
    this.cdr.detectChanges();

    this.httpService.createSession(this.myUserId).subscribe({
      next: (response) => {
        this.qrData = response;
        this.qrCode = response.qrBase64;
        this.shortCodeDisplay = response.shortCode;   // ← mostrar código corto
        this.cargandoSala = false;
        this.cdr.detectChanges();

        this.wsService.setSessionData(response.uuid, this.myUserId);
        const token = this.authService.getToken() || '';
        this.wsService.connect(token, this.myUserId, () => {
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.cargandoSala = false;
        this.showErrorMsg('Error de comunicación con MundialHub.');
      }
    });
  }

  cargarNuevoQr(): void {
    this.generarSala();
  }

  // ── Unirse a una sesión (Usuario B) ──────────────────────────────────────

  /** Punto de entrada único: delega al método correcto según el modo activo. */
  unirseAIntercambio(): void {
    if (this.joinMode === 'code') {
      this.unirPorCodigo();
    } else if (this.joinMode === 'email') {
      this.unirPorEmail();
    } else {
      this.unirPorUuid();
    }
  }

  /** Une al usuario mediante el código alfanumérico corto (ej: "AB3X7Q"). */
  private unirPorCodigo(): void {
    const code = this.shortCodeInput.trim().toUpperCase();
    if (!code || code.length !== 6) {
      this.showErrorMsg('El código debe tener exactamente 6 caracteres (ej: AB3X7Q).');
      return;
    }
    this.uniendose = true;
    this.cdr.detectChanges();

    const token = this.authService.getToken() || '';
    this.wsService.setSessionData('', this.myUserId); // UUID se desconoce aún; se completa en el callback
    this.wsService.connect(token, this.myUserId, () => {
      this.httpService.joinByShortCode(code, this.myUserId).subscribe({
        next: (mensaje) => this.onJoinSuccess(mensaje),
        error: (err) => this.onJoinError(err)
      });
    });
  }

  /** Une al usuario buscando la sesión activa del correo ingresado. */
  private unirPorEmail(): void {
    const email = this.emailInput.trim().toLowerCase();
    if (!email || !email.includes('@')) {
      this.showErrorMsg('Por favor ingresa un correo electrónico válido.');
      return;
    }
    this.uniendose = true;
    this.cdr.detectChanges();

    const token = this.authService.getToken() || '';
    this.wsService.setSessionData('', this.myUserId);
    this.wsService.connect(token, this.myUserId, () => {
      this.httpService.joinByEmail(email, this.myUserId).subscribe({
        next: (mensaje) => this.onJoinSuccess(mensaje),
        error: (err) => this.onJoinError(err)
      });
    });
  }

  /** Une al usuario mediante el UUID completo (modo avanzado / escaneo QR). */
  private unirPorUuid(): void {
    const uuid = this.sessionUuidInput.trim();
    if (!uuid) {
      this.showErrorMsg('Por favor ingresa un ID de sesión válido.');
      return;
    }
    this.uniendose = true;
    this.cdr.detectChanges();

    const token = this.authService.getToken() || '';
    this.wsService.setSessionData(uuid, this.myUserId);
    this.wsService.connect(token, this.myUserId, () => {
      this.httpService.joinSession(uuid, this.myUserId).subscribe({
        next: (mensaje) => this.onJoinSuccess(mensaje),
        error: (err) => this.onJoinError(err)
      });
    });
  }

  // ── Callbacks compartidos de unión ───────────────────────────────────────

  private onJoinSuccess(mensaje: string): void {
    const match = mensaje.match(/ID:\s*(\d+)/);
    this.userAId = match ? Number(match[1]) : 0;
    this.uniendose = false;
    this.cdr.detectChanges();

    // Notificar al Usuario A que B se unió (garantiza entrega del JOINED en edge cases)
    const sessionUuid = this.wsService.getSessionUuid();
    if (sessionUuid) {
      this.wsService.notifyJoin(sessionUuid, this.myUserId, this.userAId);
    }

    this.router.navigate(['/websocket']);
  }

  private onJoinError(err: any): void {
    this.uniendose = false;
    this.wsService.disconnect();
    this.showErrorMsg(err?.error || 'Error al unirse a la sesión.');
  }

  // ── Cambio de modo de unión ──────────────────────────────────────────────

  setJoinMode(mode: JoinMode): void {
    this.joinMode = mode;
    this.cdr.detectChanges();
  }

  // ── Scanner QR ──────────────────────────────────────────────────────────

  toggleScanner(): void {
    if (this.escaneando) {
      this.detenerScanner();
    } else {
      this.iniciarScanner();
    }
  }

  async iniciarScanner(): Promise<void> {
    this.scannedUuid = null;
    this.escaneando = true;
    await new Promise(r => setTimeout(r, 100));

    try {
      this.scanStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      });

      const video = this.scannerVideo.nativeElement;
      video.srcObject = this.scanStream;
      await video.play();

      const jsQR = (await import('jsqr')).default;
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d')!;

      this.scanInterval = setInterval(() => {
        if (video.readyState === video.HAVE_ENOUGH_DATA) {
          canvas.width = video.videoWidth;
          canvas.height = video.videoHeight;
          ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

          const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
          const code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: 'dontInvert'
          });

          if (code) {
            // QR detectado — cambiar a modo UUID y poblar el input
            this.sessionUuidInput = code.data;
            this.scannedUuid = code.data;
            this.joinMode = 'uuid';
            this.detenerScanner();
            this.cdr.detectChanges();
          }
        }
      }, 250);

    } catch (err) {
      console.error('Error al acceder a la cámara:', err);
      this.escaneando = false;
    }
  }

  detenerScanner(): void {
    clearInterval(this.scanInterval);
    this.scanInterval = null;
    if (this.scanStream) {
      this.scanStream.getTracks().forEach(t => t.stop());
      this.scanStream = null;
    }
    this.escaneando = false;
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private showErrorMsg(msg: string): void {
    this.errorMessage = msg;
    this.showError = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showError = false; this.cdr.detectChanges(); }, 5000);
  }

  ngOnDestroy(): void {
    if (this.notificationSub) this.notificationSub.unsubscribe();
    this.detenerScanner();
  }
}
