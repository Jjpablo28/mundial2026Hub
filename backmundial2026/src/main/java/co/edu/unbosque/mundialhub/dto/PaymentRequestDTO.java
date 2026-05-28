package co.edu.unbosque.mundialhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para estructurar,
 * almacenar y mapear la información técnica de las transacciones de pago con
 * Mercado Pago.
 * <p>
 * Sirve como puente de comunicación entre el frontend, el backend y el SDK de
 * la pasarela de pagos, permitiendo el seguimiento detallado de estados de
 * cobro, montos de dinero, monedas de transacción, cantidades de tokens
 * adquiridos y auditorías temporales de aprobación.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see BigDecimal
 * @see LocalDateTime
 */
public class PaymentRequestDTO {

	/**
	 * Identificador numérico único (ID interno) del registro de solicitud de pago
	 * en el sistema.
	 */
	private Long id;

	/**
	 * Identificador de la preferencia de pago generado por la API de Mercado Pago
	 * (Checkout Pro).
	 */
	private String mpPreferenceId;

	/**
	 * Identificador único final de la transacción/cobro emitido por la
	 * infraestructura de Mercado Pago.
	 */
	private Long mpPaymentId;

	/**
	 * Estado actual de la transacción (ej. approved, pending, rejected, cancelled).
	 */
	private String status;

	/**
	 * Subestado o desglose detallado del estado del pago emitido por la pasarela de
	 * cobros (ej. accredited).
	 */
	private String statusDetail;

	/**
	 * Monto económico total de la transacción monetaria calculada con precisión
	 * matemática exacta.
	 */
	private BigDecimal amount;

	/**
	 * Código ISO internacional representativo de la divisa utilizada en el cobro
	 * (ej. COP, USD).
	 */
	private String currency;

	/**
	 * Bandera lógica que determina si el pago se encuentra liquidado, conciliado y
	 * aprobado con éxito.
	 */
	private Boolean paid;

	/**
	 * Marca de tiempo exacta (Fecha y Hora) en la que se completó y acreditó la
	 * transacción financiera.
	 */
	private LocalDateTime paidAt;

	/**
	 * Cantidad de unidades físicas o de tokens digitales solicitados para su
	 * adquisición.
	 */
	private Integer quantity;

	/**
	 * Constructor por defecto (sin parámetros) requerido por los frameworks de
	 * mapeo de objetos de datos y serialización JSON (ej. Jackson) para peticiones
	 * HTTP.
	 */
	public PaymentRequestDTO() {
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización de
	 * todas las propiedades de control técnico y monetario del flujo de pago.
	 *
	 * @param id             ID único de la solicitud local.
	 * @param mpPreferenceId ID de preferencia de Mercado Pago.
	 * @param mpPaymentId    ID de pago final de Mercado Pago.
	 * @param status         Estado de la transacción.
	 * @param statusDetail   Detalle específico del estado del pago.
	 * @param amount         Monto financiero total exacto.
	 * @param currency       Tipo de divisa monetaria utilizada.
	 * @param paid           Estatus booleano de conciliación.
	 * @param paidAt         Fecha y hora exacta de la acreditación.
	 * @param quantity       Cantidad de tokens comprados.
	 */
	public PaymentRequestDTO(Long id, String mpPreferenceId, Long mpPaymentId, String status, String statusDetail,
			BigDecimal amount, String currency, Boolean paid, LocalDateTime paidAt, Integer quantity) {
		super();
		this.id = id;
		this.mpPreferenceId = mpPreferenceId;
		this.mpPaymentId = mpPaymentId;
		this.status = status;
		this.statusDetail = statusDetail;
		this.amount = amount;
		this.currency = currency;
		this.paid = paid;
		this.paidAt = paidAt;
		this.quantity = quantity;
	}

	/**
	 * Obtiene el identificador interno de la transacción de pago local.
	 * 
	 * @return Un entero largo con el ID de la base de datos.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece o modifica el identificador local de la solicitud de pago.
	 * 
	 * @param id El nuevo identificador único del registro.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el ID de preferencia emitido por Mercado Pago.
	 * 
	 * @return Una cadena de texto con la firma de preferencia externa.
	 */
	public String getMpPreferenceId() {
		return mpPreferenceId;
	}

	/**
	 * Establece o modifica el ID de la preferencia de pago de Mercado Pago.
	 * 
	 * @param mpPreferenceId La cadena con el identificador de preferencia de la
	 *                       pasarela.
	 */
	public void setMpPreferenceId(String mpPreferenceId) {
		this.mpPreferenceId = mpPreferenceId;
	}

	/**
	 * Obtiene el identificador final del pago emitido por el procesador externo de
	 * Mercado Pago.
	 * 
	 * @return Un entero largo con el ID de cobro definitivo.
	 */
	public Long getMpPaymentId() {
		return mpPaymentId;
	}

	/**
	 * Establece o modifica el ID final de la transacción monetaria en el procesador
	 * externo.
	 * 
	 * @param mpPaymentId El nuevo ID de pago de Mercado Pago.
	 */
	public void setMpPaymentId(Long mpPaymentId) {
		this.mpPaymentId = mpPaymentId;
	}

	/**
	 * Obtiene la etiqueta textual del estado actual del proceso de pago.
	 * 
	 * @return El estado de la transacción.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Establece o modifica el estado de la solicitud de pago.
	 * 
	 * @param status Cadena de texto representativa del nuevo estado.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Obtiene el desglose detallado complementario del estado del pago.
	 * 
	 * @return El subestado del cobro.
	 */
	public String getStatusDetail() {
		return statusDetail;
	}

	/**
	 * Establece o modifica el detalle específico del estado de Mercado Pago.
	 * 
	 * @param statusDetail Cadena de texto explicativa del subestado técnico.
	 */
	public void setStatusDetail(String statusDetail) {
		this.statusDetail = statusDetail;
	}

	/**
	 * Obtiene el valor o cantidad económica calculada para la transacción
	 * monetaria.
	 * 
	 * @return Un objeto {@link BigDecimal} con el monto financiero exacto.
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * Establece o modifica el valor económico exacto de la operación comercial.
	 * 
	 * @param amount El nuevo monto financiero calculado en formato
	 *               {@link BigDecimal}.
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Obtiene el tipo de divisa oficial utilizado para la liquidación del cobro.
	 * 
	 * @return El código ISO de la moneda configurada.
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * Establece o modifica el identificador de la divisa de cobro.
	 * 
	 * @param currency El código internacional ISO de la moneda correspondiente.
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * Consulta si el pago ya se encuentra liquidado y aprobado formalmente.
	 * 
	 * @return Un valor booleano {@code true} si está pagado, o {@code false} en
	 *         caso contrario.
	 */
	public Boolean isPaid() {
		return paid;
	}

	/**
	 * Modifica el estatus lógico de liquidación de la solicitud de pago.
	 * 
	 * @param paid El nuevo estado lógico de aprobación.
	 */
	public void setPaid(Boolean paid) {
		this.paid = paid;
	}

	/**
	 * Obtiene el registro temporal exacto en el que se efectuó y concilió el cobro
	 * financiero.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la marca de tiempo de
	 *         acreditación.
	 */
	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	/**
	 * Establece la marca de tiempo exacta en la que se concilia el pago en el
	 * sistema.
	 * 
	 * @param paidAt La fecha y hora de la transacción aprobada.
	 */
	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	/**
	 * Obtiene el total de tokens o artículos numéricos solicitados para la
	 * transacción actual.
	 * 
	 * @return El número entero de artículos/tokens a procesar.
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * Establece o modifica el total de artículos o tokens digitales asociados al
	 * carrito de compra.
	 * 
	 * @param quantity El nuevo valor entero del volumen de unidades.
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}