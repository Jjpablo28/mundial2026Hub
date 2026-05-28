package co.edu.unbosque.mundialhub.model;

/**
 * Enumerador oficial que define los niveles de escasez, exclusividad y rareza
 * asignados a las láminas (stickers) dentro de la plataforma MundialHub.
 * <p>
 * Estos valores determinan la probabilidad de aparición de los cromos al abrir
 * sobres, así como su valoración e impacto en los sistemas de gamificación y
 * mercados de intercambio.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 */
public enum Rarity {
	/**
	 * Lámina de categoría Común. Representa a la mayoría de jugadores con alta tasa
	 * de aparición.
	 */
	COMMON,

	/**
	 * Lámina de categoría Rara. Posee una dificultad moderada de obtención en los
	 * sobres básicos.
	 */
	RARE,

	/**
	 * Lámina de categoría Épica. Destinada a jugadores estrella con baja
	 * probabilidad de aparición.
	 */
	EPIC,

	/**
	 * Lámina de categoría Legendaria. Reservada para las máximas figuras históricas
	 * o escudos especiales con probabilidad de aparición mínima.
	 */
	LEGENDARY
}