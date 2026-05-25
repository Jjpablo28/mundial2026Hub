import {Component, OnDestroy, OnInit, ChangeDetectorRef, ElementRef, ViewChild} from '@angular/core';
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

  escaneando = false;
  scannedUuid: string | null = null;
  private scanStream: MediaStream | null = null;
  private scanInterval: any = null;

  @ViewChild('scannerVideo') scannerVideo!: ElementRef<HTMLVideoElement>;


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

    // Pequeño delay para que Angular renderice el <video> antes de asignarlo
    await new Promise(r => setTimeout(r, 100));

    try {
      this.scanStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' } // cámara trasera en móvil
      });

      const video = this.scannerVideo.nativeElement;
      video.srcObject = this.scanStream;
      await video.play();

      // Importar jsqr dinámicamente y escanear fotograma a fotograma
      const jsQR = (await import('jsqr')).default;
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d')!;

      this.scanInterval = setInterval(() => {
        if (video.readyState === video.HAVE_ENOUGH_DATA) {
          canvas.width  = video.videoWidth;
          canvas.height = video.videoHeight;
          ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

          const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
          const code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: 'dontInvert'
          });

          if (code) {
            // QR detectado — poblar el input y detener la cámara
            this.sessionUuidInput = code.data;
            this.scannedUuid = code.data;
            this.detenerScanner();
          }
        }
      }, 250); // checar 4 veces por segundo

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

  ngOnDestroy(): void {
    if (this.notificationSub) this.notificationSub.unsubscribe();
    this.detenerScanner()
  }
}
