import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { TradeWebSocketService } from './trade-websocket.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../security/AuthService';
import { Router } from '@angular/router';

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

  // Ready state
  myReady: boolean = false;
  counterpartReady: boolean = false;

  // Popup
  showSuccessPopup: boolean = false;
  receivedStickers: any[] = [];

  private subNotifications!: Subscription;
  private subErrors!: Subscription;

  constructor(
    private wsService: TradeWebSocketService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.sessionUuid = this.wsService.getSessionUuid();
    this.myUserId = this.authService.getUserId() || this.wsService.getMyUserId();

    this.addLog('Conectado a la mesa segura. Esperando contraparte...', '#AAAAAA');

    this.subNotifications = this.wsService.getNotifications().pipe(
      filter(msg => !!msg)
    ).subscribe(msg => {
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

      // Counterpart marked themselves as ready
      if (msg === "READY:TRUE") {
        this.counterpartReady = true;
        this.addLog('¡Tu contraparte está lista! Confirma cuando estés listo.', '#E1FF57');
        this.cdr.detectChanges();

        // If I was already ready, execute the trade
        if (this.myReady) {
          this.ejecutarIntercambio();
        }
      }

      if (msg.includes("exitoso")) {
        this.addLog(`✅ ${msg}`, '#00FF99');
        // Parse received stickers from the success message and show popup
        this.mostrarPopupExito();
        this.cdr.detectChanges();
      }
    });

    this.subErrors = this.wsService.getErrors().subscribe(err => {
      this.addLog(`[ERROR] ${err}`, '#ff4d4d');
      // Reset ready state on error so users can retry
      this.myReady = false;
      this.counterpartReady = false;
      this.cdr.detectChanges();
    });

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
          // Own inventory: only repeated stickers (quantity > 1)
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
          // Counterpart inventory: ALSO only repeated stickers (quantity > 1)
          this.susStickersDisponibles = res.filter(s => s && s.quantity > 1);
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
      error: err => {
        console.error("Error al cargar inventario:", err);
        this.addLog(`Error al cargar inventario del usuario ${userId}.`, '#ff4d4d');
      }
    });
  }

  // Click on own sticker → add to "mis láminas a entregar"
  agregarMiLamina(stickerId: number): void {
    const ids = this.misStickersInput
      ? this.misStickersInput.split(',').map(s => s.trim()).filter(s => s)
      : [];
    if (!ids.includes(String(stickerId))) {
      ids.push(String(stickerId));
      this.misStickersInput = ids.join(', ');
    }
  }

  // Click on counterpart sticker → add to "láminas que voy a recibir"
  agregarSuLamina(stickerId: number): void {
    const ids = this.susStickersInput
      ? this.susStickersInput.split(',').map(s => s.trim()).filter(s => s)
      : [];
    if (!ids.includes(String(stickerId))) {
      ids.push(String(stickerId));
      this.susStickersInput = ids.join(', ');
    }
  }

  // Remove a sticker ID from an input string
  removerId(input: string, id: number): string {
    return input
      .split(',')
      .map(s => s.trim())
      .filter(s => s && Number(s) !== id)
      .join(', ');
  }

  removerMiLamina(stickerId: number): void {
    this.misStickersInput = this.removerId(this.misStickersInput, stickerId);
  }

  removerSuLamina(stickerId: number): void {
    this.susStickersInput = this.removerId(this.susStickersInput, stickerId);
  }

  // Parse current inputs into arrays
  getMisIds(): number[] {
    return this.misStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);
  }

  getSusIds(): number[] {
    return this.susStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);
  }

  marcarListo(): void {
    if (this.toUserId === 0) {
      alert("Todavía no hay contraparte conectada.");
      return;
    }
    if (!this.misStickersInput || !this.susStickersInput) {
      alert("Por favor selecciona las láminas para el intercambio.");
      return;
    }

    const myStickers = this.getMisIds();
    const theirStickers = this.getSusIds();

    if (myStickers.length === 0 || theirStickers.length === 0) {
      alert("Por favor selecciona al menos una lámina de cada lado.");
      return;
    }

    if (myStickers.length !== theirStickers.length) {
      alert(`Paridad incorrecta: ofreces ${myStickers.length} pero recibirías ${theirStickers.length}.`);
      return;
    }

    this.myReady = true;
    this.addLog("✋ Marcaste como listo. Esperando a tu contraparte...", '#E1FF57');

    // Notify counterpart that this user is ready
    this.wsService.notifyReady(this.sessionUuid, this.myUserId, this.toUserId);

    // If counterpart was already ready, execute trade now
    if (this.counterpartReady) {
      this.ejecutarIntercambio();
    }
  }

  ejecutarIntercambio(): void {
    const myStickers = this.getMisIds();
    const theirStickers = this.getSusIds();

    const payload = {
      sessionUuid: this.sessionUuid,
      fromUserId: this.myUserId,
      toUserId: this.toUserId,
      fromStickerIds: myStickers,
      toStickerIds: theirStickers
    };

    this.addLog("🔄 Ambos listos. Ejecutando intercambio...", '#ffffff');
    this.wsService.sendProposal(payload);
  }

  mostrarPopupExito(): void {
    // Build list of stickers received (those that were in susStickersInput)
    const receivedIds = this.getSusIds();
    this.receivedStickers = this.susStickersDisponibles.filter(s => receivedIds.includes(s.stickerId));
    // If stickers not found in list, show IDs as fallback
    if (this.receivedStickers.length === 0) {
      this.receivedStickers = receivedIds.map(id => ({ stickerId: id, playerName: '' }));
    }
    this.showSuccessPopup = true;
    this.cdr.detectChanges();
  }

  cerrarPopupYRedirigir(): void {
    this.showSuccessPopup = false;
    this.router.navigate(['/home']);
  }

  ngOnDestroy(): void {
    if (this.subNotifications) this.subNotifications.unsubscribe();
    if (this.subErrors) this.subErrors.unsubscribe();
  }
}
