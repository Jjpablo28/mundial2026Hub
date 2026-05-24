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

    // ✅ Escuchamos notificaciones — BehaviorSubject emite el último valor al suscribirse
    // Si JOINED ya llegó antes de navegar aquí, lo procesamos de inmediato
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
        this.cargarInventarioB(this.toUserId, true);
        this.cdr.detectChanges();
      }

      if (msg.startsWith("SYNC_OFFER:")) {
        try {
          const offerJson = msg.substring("SYNC_OFFER:".length);
          const offer = JSON.parse(offerJson);
          // Lo que el otro ofrece (fromIds) = yo voy a recibir
          // Lo que el otro pide (toIds) = yo voy a dar
          this.susStickersInput = (offer.fromIds || []).join(', ');
          this.misStickersInput = (offer.toIds || []).join(', ');
          this.cdr.detectChanges();
        } catch (e) {
          console.warn('[TradeScreen] Error parseando SYNC_OFFER:', e);
        }
      }

      if (msg === "READY:TRUE") {
        this.counterpartReady = true;
        this.addLog('¡Tu contraparte está lista! Confirma cuando estés listo.', '#E1FF57');
        this.cdr.detectChanges();
        if (this.myReady) {
          this.ejecutarIntercambio();
        }
      }

      if (msg.includes("exitoso")) {
        this.addLog(`✅ ${msg}`, '#00FF99');
        this.mostrarPopupExito();
        this.cdr.detectChanges();
      }
    });

    this.subErrors = this.wsService.getErrors().subscribe(err => {
      this.addLog(`[ERROR] ${err}`, '#ff4d4d');
      this.myReady = false;
      this.counterpartReady = false;
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
          alert("No hay fichas disponibles")
        }
        this.cdr.detectChanges();
      },
      error: err => {
        console.error("Error al cargar inventario:", err);
        this.addLog(`Error al cargar inventario del usuario ${userId}.`, '#ff4d4d');
      }
    });
  }

  cargarInventarioB(userId: number, esMio: boolean): void {
    this.wsService.getStickersId(userId).subscribe({
      next: (res: any[]) => {
        if (esMio) {
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
        } else {
          alert("No hay fichas disponibles")
        }
        this.cdr.detectChanges();
      },
      error: err => {
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

  // ── Helpers para parsear inputs ──────────────────────────────────────────
  getMisIds(): number[] {
    return this.misStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);
  }

  getSusIds(): number[] {
    return this.susStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);
  }

  // ── Click en lámina propia → agregar a "mis láminas a entregar" ──────────
  agregarMiLamina(stickerId: number): void {
    const ids = this.misStickersInput
      ? this.misStickersInput.split(',').map(s => s.trim()).filter(s => s)
      : [];
    if (!ids.includes(String(stickerId))) {
      ids.push(String(stickerId));
      this.misStickersInput = ids.join(', ');
    }
    this.sincronizarOferta();
  }

  // ── Click en lámina del contraparte → agregar a "láminas que voy a recibir" ──
  agregarSuLamina(stickerId: number): void {
    const ids = this.susStickersInput
      ? this.susStickersInput.split(',').map(s => s.trim()).filter(s => s)
      : [];
    if (!ids.includes(String(stickerId))) {
      ids.push(String(stickerId));
      this.susStickersInput = ids.join(', ');
    }
    this.sincronizarOferta();
  }

  removerMiLamina(stickerId: number): void {
    this.misStickersInput = this.misStickersInput
      .split(',').map(s => s.trim()).filter(s => s && Number(s) !== stickerId).join(', ');
    this.sincronizarOferta();
  }

  removerSuLamina(stickerId: number): void {
    this.susStickersInput = this.susStickersInput
      .split(',').map(s => s.trim()).filter(s => s && Number(s) !== stickerId).join(', ');
    this.sincronizarOferta();
  }

  sincronizarOferta(): void {
    if (this.toUserId === 0) return;
    this.wsService.syncOffer(this.sessionUuid, this.myUserId, this.toUserId, {
      fromIds: this.getMisIds(),
      toIds: this.getSusIds()
    });
  }

  // ── Botón "Estoy Listo" ───────────────────────────────────────────────────
  marcarListo(): void {
    if (this.toUserId === 0) {
      alert("Todavía no hay contraparte conectada.");
      return;
    }
    if (this.getMisIds().length === 0 || this.getSusIds().length === 0) {
      alert("Por favor selecciona las láminas para el intercambio.");
      return;
    }
    if (this.getMisIds().length !== this.getSusIds().length) {
      alert(`Paridad incorrecta: ofreces ${this.getMisIds().length} pero recibirías ${this.getSusIds().length}.`);
      return;
    }
    this.myReady = true;
    this.addLog("✋ Marcaste como listo. Esperando a tu contraparte...", '#E1FF57');
    this.wsService.notifyReady(this.sessionUuid, this.myUserId, this.toUserId);
    if (this.counterpartReady) {
      this.ejecutarIntercambio();
    }
  }

  ejecutarIntercambio(): void {
    const payload = {
      sessionUuid: this.sessionUuid,
      fromUserId: this.myUserId,
      toUserId: this.toUserId,
      fromStickerIds: this.getMisIds(),
      toStickerIds: this.getSusIds()
    };
    this.addLog("🔄 Ambos listos. Ejecutando intercambio...", '#ffffff');
    this.wsService.sendProposal(payload);
  }

  mostrarPopupExito(): void {
    const receivedIds = this.getSusIds();
    this.receivedStickers = this.susStickersDisponibles.filter(s => receivedIds.includes(s.stickerId));
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
  desconectarse(): void {
    const confirmar = confirm("¿Seguro que quieres salir de la mesa de intercambio?");
    if (confirmar) {
      this.wsService.disconnect();
      this.router.navigate(['/home']);
    }
  }
  ngOnDestroy(): void {
    if (this.subNotifications) this.subNotifications.unsubscribe();
    if (this.subErrors) this.subErrors.unsubscribe();
  }
}
