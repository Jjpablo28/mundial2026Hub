import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { TradeWebSocketService } from "../trade-screen/trade-websocket.service";
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../security/AuthService';

@Component({
  selector: 'app-trade-screen',
  standalone: false,
  templateUrl: './trade-screen.html',
  styleUrl: './trade-screen.scss',
})
export class TradeScreen implements OnInit, OnDestroy {

  sessionUuid: string = '';
  myUserId: number = 0;
  toUserId: number = 0;

  misStickersInput: string = '';
  susStickersInput: string = '';

  misStickersDisponibles: any[] = [];
  susStickersDisponibles: any[] = [];

  esperandoContraparte: boolean = true;
  logs: Array<{ text: string, color: string }> = [];

  private subNotifications!: Subscription;
  private subErrors!: Subscription;

  constructor(
    private wsService: TradeWebSocketService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.sessionUuid = this.wsService.getSessionUuid();
    this.myUserId = this.authService.getUserId() || this.wsService.getMyUserId();

    this.addLog('Conectado a la mesa segura. Esperando contraparte...', '#AAAAAA');

    // ✅ Escuchamos notificaciones — BehaviorSubject emite el último valor al suscribirse
    // Si JOINED ya llegó antes de navegar aquí, lo procesamos de inmediato
    this.subNotifications = this.wsService.getNotifications().pipe(
      filter(msg => !!msg)
    ).subscribe((msg: string) => {
      console.log('[TradeScreen] Notificación:', msg);
      this.addLog(`[INFO] ${msg}`, '#AAAAAA');

      if (msg.startsWith("JOINED:")) {
        const contraparteId = Number(msg.split(":")[1]);
        this.toUserId = contraparteId;
        this.esperandoContraparte = false;

        this.addLog(`¡Contraparte conectada (ID: ${contraparteId})! Cargando inventarios...`, '#E1FF57');
        this.cargarInventario(this.myUserId, true);
        this.cargarInventario(this.toUserId, false);
        this.cdr.detectChanges();
      }

      if (msg.includes("exitoso")) {
        this.addLog(`✅ ${msg}`, '#00FF99');
        this.cargarInventario(this.myUserId, true);
        this.cdr.detectChanges();
      }
    });

    this.subErrors = this.wsService.getErrors().subscribe(err => {
      this.addLog(`[ERROR] ${err}`, '#ff4d4d');
      this.cdr.detectChanges();
    });

    // ✅ Si el usuario es B, ya sabe que está en la mesa — carga su inventario
    if (this.myUserId && this.myUserId !== 0) {
      this.cargarInventario(this.myUserId, true);
    }
  }

  addLog(text: string, color: string): void {
    this.logs.push({ text: `[${new Date().toLocaleTimeString()}] ${text}`, color });
    this.cdr.detectChanges();
  }

  cargarInventario(userId: number, esMio: boolean): void {
    this.wsService.getStickersId(userId).subscribe({
      next: (res: any[]) => {
        if (esMio) {
          this.misStickersDisponibles = res.filter(s => s && s.quantity > 1);
          this.misStickersDisponibles.forEach((sticker, index) => {
            if (!sticker?.stickerId) return;
            this.wsService.getStickerInfo(sticker.stickerId).subscribe({
              next: (info: any) => {
                this.misStickersDisponibles[index] = { ...sticker, playerName: info.playerName };
                this.cdr.detectChanges();
              }
            });
          });
        } else {
          this.susStickersDisponibles = res;
          this.susStickersDisponibles.forEach((sticker, index) => {
            if (!sticker?.stickerId) return;
            this.wsService.getStickerInfo(sticker.stickerId).subscribe({
              next: (info: any) => {
                this.susStickersDisponibles[index] = { ...sticker, playerName: info.playerName };
                this.cdr.detectChanges();
              }
            });
          });
        }
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error("Error al cargar inventario:", err);
        this.addLog(`Error al cargar inventario del usuario ${userId}.`, '#ff4d4d');
      }
    });
  }

  enviarPropuestaFinal(): void {
    if (this.toUserId === 0) {
      alert("Todavía no hay contraparte conectada.");
      return;
    }
    if (!this.misStickersInput || !this.susStickersInput) {
      alert("Por favor ingresa los códigos de las láminas.");
      return;
    }

    const myStickers = this.misStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);
    const theirStickers = this.susStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);

    if (myStickers.length !== theirStickers.length) {
      alert(`Paridad incorrecta: ofreces ${myStickers.length} pero recibirías ${theirStickers.length}.`);
      return;
    }

    const payload = {
      sessionUuid: this.sessionUuid,
      fromUserId: this.myUserId,
      toUserId: this.toUserId,
      fromStickerIds: myStickers,
      toStickerIds: theirStickers
    };

    this.addLog("Enviando propuesta al servidor...", '#ffffff');
    this.wsService.sendProposal(payload);
  }

  ngOnDestroy(): void {
    if (this.subNotifications) this.subNotifications.unsubscribe();
    if (this.subErrors) this.subErrors.unsubscribe();
  }
}
