package co.edu.unbosque.mundialhub.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidad de persistencia que representa la tabla "payments" en la base de
 * datos.
 * <p>
 * Se encarga de mapear el modelo de datos relacional para el control financiero
 * de las transacciones y compras integradas con la pasarela externa de Mercado
 * Pago. Almacena el estado del ciclo de vida del cobro, los montos monetarios,
 * la cantidad de artículos/tokens adquiridos y establece la relación de
 * auditoría con el usuario comprador.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see Entity
 * @see Table
 * @see User
 */
@Entity
@Table(name = "payments")
public class Payments {

	/**
	 * Identificador numérico único y autoincremental (llave primaria) de la
	 * transacción local.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Identificador único de preferencia generado por el checkout de Mercado Pago
	 * para procesar el cobro.
	 */
	@Column(name = "mp_preference_id", unique = true)
	private String mpPreferenceId;

	/**
	 * Identificador definitivo de la transacción emitido por Mercado Pago una vez
	 * procesada.
	 */
	@Column(name = "mp_payment_id")
	private Long mpPaymentId;

	/**
	 * Cantidad total de paquetes o tokens digitales solicitados en la orden de
	 * compra.
	 */
	@Column(name = "quantity")
	private int quantity;

	/** Estado actual del flujo de pago (ej. "pending", "approved", "rejected"). */
	@Column(name = "status")
	private String status;

	/**
	 * Desglose técnico o subestado del cobro reportado por la pasarela de pagos
	 * (ej. "accredited").
	 */
	@Column(name = "status_detail")
	private String statusDetail;

	/**
	 * Valor o importe económico total de la operación comercial con precisión
	 * matemática decimal.
	 */
	@Column(name = "amount")
	private BigDecimal amount;

	/**
	 * Código ISO internacional representativo de la divisa monetaria del cobro (ej.
	 * "COP").
	 */
	@Column(name = "currency")
	private String currency;

	/**
	 * Bandera lógica que determina si la pasarela validó y aprobó el recaudo del
	 * dinero de forma exitosa.
	 */
	@Column(name = "paid")
	private boolean paid;

	/**
	 * Registro cronológico exacto en el que se efectuó y acreditó formalmente el
	 * pago.
	 */
	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	/**
	 * Relación de muchos a uno que asocia la transacción financiera con el usuario
	 * titular de la compra.
	 */
	@ManyToOne
	private User user;

	/**
	 * Registro cronológico que audita la creación inicial de la solicitud de cobro
	 * en el sistema.
	 */
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * Método de ciclo de vida JPA encargado de interceptar el flujo previo a la
	 * inserción física del registro. Inicializa por defecto la fecha de creación al
	 * tiempo actual, configura la bandera de pago en falso y establece el estado
	 * transaccional inicial como "pending".
	 */
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.paid = false;
		this.status = "pending";
	}

	/**
	 * Constructor por defecto (sin parámetros) requerido obligatoriamente por la
	 * especificación de Jakarta Persistence (JPA) para la reconstrucción de
	 * instancias y mapeo relacional.
	 */
	public Payments() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor completo parametrizado para la instanciación de un registro
	 * histórico de pago con todas sus variables de control y relaciones de
	 * auditoría asignadas.
	 *
	 * @param id             Identificador único local.
	 * @param mpPreferenceId Firma o ID de preferencia de Mercado Pago.
	 * @param mpPaymentId    Identificador de cobro de Mercado Pago.
	 * @param quantity       Unidades de artículos/tokens comprados.
	 * @param status         Estado actual del pago.
	 * @param statusDetail   Detalle extendido del estado.
	 * @param amount         Importe o monto económico exacto.
	 * @param currency       Tipo de divisa internacional.
	 * @param paid           Estatus booleano de conciliación monetaria.
	 * @param paidAt         Fecha y hora de acreditación del cobro.
	 * @param user           Usuario asociado que ejecuta la compra (Entidad
	 *                       {@link User}).
	 * @param createdAt      Fecha y hora de radicación de la orden.
	 */
	public Payments(Long id, String mpPreferenceId, Long mpPaymentId, int quantity, String status, String statusDetail,
			BigDecimal amount, String currency, boolean paid, LocalDateTime paidAt, User user,
			LocalDateTime createdAt) {
		super();
		this.id = id;
		this.mpPreferenceId = mpPreferenceId;
		this.mpPaymentId = mpPaymentId;
		this.quantity = quantity;
		this.status = status;
		this.statusDetail = statusDetail;
		this.amount = amount;
		this.currency = currency;
		this.paid = paid;
		this.paidAt = paidAt;
		this.user = user;
		this.createdAt = createdAt;
	}

	/**
	 * Obtiene el identificador de la transacción local de base de datos.
	 * 
	 * @return Un entero largo con el ID correspondiente.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador de la transacción local de base de datos.
	 * 
	 * @param id El nuevo identificador único del registro.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el código de preferencia emitido por la pasarela de Mercado Pago.
	 * 
	 * @return Cadena de texto con la firma del checkout.
	 */
	public String getMpPreferenceId() {
		return mpPreferenceId;
	}

	/**
	 * Define la preferencia de cobro generada por la API externa.
	 * 
	 * @param mpPreferenceId La cadena con el nuevo identificador de preferencia.
	 */
	public void setMpPreferenceId(String mpPreferenceId) {
		this.mpPreferenceId = mpPreferenceId;
	}

	/**
	 * Obtiene el ID final de la transacción bancaria de Mercado Pago.
	 * 
	 * @return Un entero largo con la llave del pago externo.
	 */
	public Long getMpPaymentId() {
		return mpPaymentId;
	}

	/**
	 * Establece el ID final de la transacción bancaria de Mercado Pago.
	 * 
	 * @param mpPaymentId El nuevo identificador de pago externo.
	 */
	public void setMpPaymentId(Long mpPaymentId) {
		this.mpPaymentId = mpPaymentId;
	}

	/**
	 * Obtiene la nomenclatura textual del estado de la transacción.
	 * 
	 * @return El estado del pago.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Modifica el estado de la transacción actual.
	 * 
	 * @param status El nuevo estado del flujo.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Obtiene el desglose o subestado complementario del cobro.
	 * 
	 * @return El detalle específico de la pasarela.
	 */
	public String getStatusDetail() {
		return statusDetail;
	}

	/**
	 * Modifica el desglose o subestado técnico de la pasarela.
	 * 
	 * @param statusDetail Cadena explicativa del subestado.
	 */
	public void setStatusDetail(String statusDetail) {
		this.statusDetail = statusDetail;
	}

	/**
	 * Obtiene el importe económico exacto calculado para la transacción.
	 * 
	 * @return Un objeto {@link BigDecimal} con el monto financiero.
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * Establece el importe económico exacto para la transacción.
	 * 
	 * @param amount El nuevo monto en formato {@link BigDecimal}.
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Obtiene el estándar de divisa utilizado para el cobro.
	 * 
	 * @return Código ISO de la divisa.
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * Define el estándar internacional de la moneda para la transacción.
	 * 
	 * @param currency El código ISO de la moneda (ej. "COP").
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * Consulta si la transacción financiera ha sido debidamente liquidada y
	 * recaudada.
	 * 
	 * @return {@code true} si el dinero fue cobrado exitosamente; {@code false} de
	 *         lo contrario.
	 */
	public boolean isPaid() {
		return paid;
	}

	/**
	 * Modifica la bandera lógica de liquidación y recaudo del pago.
	 * 
	 * @param paid El nuevo estado lógico de acreditación.
	 */
	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	/**
	 * Obtiene el registro cronológico exacto de la aprobación del cobro.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la estampa temporal de pago.
	 */
	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	/**
	 * Establece el registro cronológico en el que se aprueba el cobro.
	 * 
	 * @param paidAt La fecha y hora exacta de la transacción aprobada.
	 */
	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	/**
	 * Obtiene la entidad del usuario comprador asociado a la transacción.
	 * 
	 * @return El objeto {@link User} vinculado.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Vincula la orden de pago a un usuario específico del sistema.
	 * 
	 * @param user La entidad {@link User} del cliente comprador.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Obtiene la marca de tiempo de creación de la solicitud local.
	 * 
	 * @return Un objeto {@link LocalDateTime} de auditoría inicial.
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Establece la marca de tiempo de creación de la solicitud local.
	 * 
	 * @param createdAt Objeto {@link LocalDateTime} correspondiente al instante de
	 *                  creación.
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Obtiene el volumen o volumen de unidades de artículos asignados a la compra.
	 * 
	 * @return El número entero de ítems.
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Configura el volumen total de artículos a procesar en la transacción actual.
	 * 
	 * @param quantity La cantidad entera de ítems a adquirir.
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}