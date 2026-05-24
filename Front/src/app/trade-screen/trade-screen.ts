import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TradeWebSocketService } from './trade-websocket.service';
import { Subscription } from 'rxjs';
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

  // ✅ FIX: Ya no está hardcodeado. Se recibe dinámicamente por la notificación JOINED
  toUserId: number = 0;

  misStickersInput: string = '';
  susStickersInput: string = '';

  misStickersDisponibles: any[] = [];
  susStickersDisponibles: any[] = [];

  esperandoContraparte: boolean = true; // ✅ Estado para mostrar en el template
  logs: Array<{ text: string, color: string }> = [];

  private subNotifications!: Subscription;
  private subErrors!: Subscription;

  constructor(
    private wsService: TradeWebSocketService,
    private authService: AuthService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.sessionUuid = this.wsService.getSessionUuid();
    this.myUserId = this.authService.getUserId() || this.wsService.getMyUserId();

    this.logs.push({
      text: `[${new Date().toLocaleTimeString()}] Conectado a la mesa segura. Esperando contraparte...`,
      color: '#AAAAAA'
    });

    // ✅ FIX PRINCIPAL: Escuchamos la notificación JOINED para saber el ID del otro usuario
    this.subNotifications = this.wsService.getNotifications().subscribe(msg => {
      this.logs.push({ text: `[INFO] ${msg}`, color: '#AAAAAA' });
      this.cdr.detectChanges();

      // El backend envía "JOINED:{userBId}" cuando el Usuario B se une
      if (msg.startsWith("JOINED:")) {
        const contraparteId = Number(msg.split(":")[1]);
        this.toUserId = contraparteId;
        this.esperandoContraparte = false;

        this.logs.push({
          text: `[${new Date().toLocaleTimeString()}] ¡Contraparte conectada (ID: ${contraparteId})! Cargando inventarios...`,
          color: '#E1FF57'
        });

        // Cargamos ambos inventarios ahora que ya sabemos quién es la contraparte
        this.cargarInventario(this.myUserId, true);
        this.cargarInventario(this.toUserId, false);
        this.cdr.detectChanges();
      }

      // Si el intercambio se completó con éxito
      if (msg.includes("exitoso") || msg.includes("éxito")) {
        this.logs.push({
          text: `[${new Date().toLocaleTimeString()}] ✅ ${msg}`,
          color: '#00FF99'
        });
        // Recargamos los inventarios actualizados tras el intercambio
        this.cargarInventario(this.myUserId, true);
        this.cdr.detectChanges();
      }
    });

    this.subErrors = this.wsService.getErrors().subscribe(err => {
      this.logs.push({ text: `[ERROR] ${err}`, color: '#ff4d4d' });
      this.cdr.detectChanges();
    });

    // ✅ FIX PARA USUARIO B: Si ya tiene un toUserId guardado en el servicio (navegó directo),
    // carga su propio inventario sin esperar notificación.
    // Nota: El Usuario B no recibirá el JOINED porque él fue quien disparó el join.
    // Sus láminas propias las cargamos de inmediato. Las del otro se cargan al recibir el JOINED
    // que el servidor le manda a él también (ver TradeSessionController).
    const userIdDelServicio = this.wsService.getMyUserId();
    if (userIdDelServicio && userIdDelServicio !== 0) {
      this.cargarInventario(this.myUserId, true);
    }
  }

  cargarInventario(userId: number, esMio: boolean): void {
    this.wsService.getStickersId(userId).subscribe({
      next: (res: any[]) => {
        if (esMio) {
          // Solo mostramos láminas repetidas (quantity > 1) para intercambiar
          this.misStickersDisponibles = res.filter(
            sticker => sticker && sticker.quantity > 1
          );

          // Enriquecemos con el nombre del jugador
          this.misStickersDisponibles.forEach((sticker: any, index: number) => {
            if (!sticker || !sticker.stickerId) return;

            this.wsService.getStickerInfo(sticker.stickerId).subscribe({
              next: (info: any) => {
                this.misStickersDisponibles[index] = {
                  ...sticker,
                  playerName: info.playerName,
                };
                this.cdr.detectChanges();
              },
              error: (err: any) => console.error("Error al obtener info de lámina:", err)
            });
          });

        } else {
          // Para la contraparte mostramos todas sus láminas disponibles
          this.susStickersDisponibles = res;

          // También enriquecemos las láminas de la contraparte con nombre
          this.susStickersDisponibles.forEach((sticker: any, index: number) => {
            if (!sticker || !sticker.stickerId) return;

            this.wsService.getStickerInfo(sticker.stickerId).subscribe({
              next: (info: any) => {
                this.susStickersDisponibles[index] = {
                  ...sticker,
                  playerName: info.playerName,
                };
                this.cdr.detectChanges();
              },
              error: (err: any) => console.error("Error al obtener info de lámina de contraparte:", err)
            });
          });
        }

        this.cdr.detectChanges();
      },
      error: err => {
        console.error("Error al cargar inventario:", err);
        this.logs.push({ text: `[ERROR] No se pudo cargar el inventario del usuario ${userId}.`, color: '#ff4d4d' });
        this.cdr.detectChanges();
      }
    });
  }

  enviarPropuestaFinal(): void {
    // ✅ Validación adicional: verificar que la contraparte esté conectada
    if (this.toUserId === 0) {
      alert("Todavía no hay contraparte conectada. Espera a que la otra persona se una.");
      return;
    }

    if (!this.misStickersInput || !this.susStickersInput) {
      alert("Por favor ingresa los códigos de las láminas a intercambiar.");
      return;
    }

    const myStickers = this.misStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);
    const theirStickers = this.susStickersInput.split(',').map(id => Number(id.trim())).filter(n => !isNaN(n) && n > 0);

    // Validación de paridad en el frontend antes de enviar
    if (myStickers.length !== theirStickers.length) {
      alert(`Paridad incorrecta: Tú ofreces ${myStickers.length} lámina(s) pero recibirías ${theirStickers.length}. Deben ser la misma cantidad.`);
      return;
    }

    const payload = {
      sessionUuid: this.sessionUuid,
      fromUserId: this.myUserId,
      toUserId: this.toUserId,
      fromStickerIds: myStickers,
      toStickerIds: theirStickers
    };

    this.logs.push({
      text: `[${new Date().toLocaleTimeString()}] Despachando propuesta atómica al servidor...`,
      color: '#ffffff'
    });
    this.wsService.sendProposal(payload);
  }

  ngOnDestroy(): void {
    if (this.subNotifications) this.subNotifications.unsubscribe();
    if (this.subErrors) this.subErrors.unsubscribe();
    this.wsService.disconnect();
  }
}
