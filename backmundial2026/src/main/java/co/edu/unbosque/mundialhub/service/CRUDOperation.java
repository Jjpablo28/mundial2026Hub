package co.edu.unbosque.mundialhub.service;

import java.util.List;

/**
 * Interfaz genérica para definir las operaciones CRUD (Crear, Leer, Actualizar,
 * Eliminar) básicas para las entidades de la aplicación.
 * <p>
 * Esta interfaz proporciona un contrato común para los servicios que gestionan
 * las entidades, permitiendo una mayor consistencia y reutilización de código.
 * Define métodos para la creación, lectura de todos los registros, eliminación
 * por ID, actualización por ID, conteo total de registros y verificación de
 * existencia por ID. También incluye métodos específicos para operaciones
 * relacionadas con la seguridad, como encriptación y desencriptación, y
 * actualizaciones específicas de campos como usuario, contraseña, correo
 * electrónico y rol.
 *
 * @param <T> El tipo del objeto DTO (Data Transfer Object) utilizado para
 * transferir datos hacia y desde la capa de servicio.
 * @param <E> El tipo de la entidad JPA (Java Persistence API) gestionada por el
 * servicio.
 */
public interface CRUDOperation<T, E> {
	/**
	 * Crea una nueva entidad a partir de un DTO y un rol asociado.
	 *
	 * @param data El DTO que contiene los datos para la creación de la entidad.
	 * @param rol  El rol asociado a la entidad que se va a crear.
	 * @return Un código de resultado indicando el éxito o el motivo del fallo
	 * (dependiendo de la implementación).
	 */
	public T create(T data);
	
	public int createWithRol(T data, String rol);
	
		/**
	 * Obtiene todos los registros de la entidad y los devuelve como una lista de
	 * DTOs.
	 *
	 * @return Una lista de objetos DTO que representan todos los registros de la
	 * entidad.
	 */
	public List<T> findAll();
	/**
	 * Elimina una entidad por su ID.
	 *
	 * @param id El ID de la entidad a eliminar.
	 * @return Un código de resultado indicando el éxito o el motivo del fallo
	 * (dependiendo de la implementación).
	 */
	public void deleteById(Long id);
	/**
	 * Actualiza una entidad existente por su ID utilizando los datos proporcionados
	 * en el DTO.
	 *
	 * @param id      El ID de la entidad a actualizar.
	 * @param newData El DTO que contiene los nuevos datos para la entidad.
	 * @return Un código de resultado indicando el éxito o el motivo del fallo
	 * (dependiendo de la implementación).
	 */
	public T updateById(Long id, T newData);
	/**
	 * Cuenta el número total de registros de la entidad.
	 *
	 * @return El número total de registros.
	 */
	public long count();
	/**
	 * Verifica si una entidad con el ID especificado existe.
	 *
	 * @param id El ID de la entidad a verificar.
	 * @return {@code true} si la entidad existe, {@code false} en caso contrario.
	 */
	public boolean exist(Long id);

;


}