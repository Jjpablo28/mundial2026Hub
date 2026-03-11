package co.edu.unbosque.mundialhub.security;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.mundialhub.repository.UserRepository;



/**
 * Implementación del servicio {@link UserDetailsService} de Spring Security.
 * <p>
 * Este servicio es responsable de cargar los detalles de un usuario a partir
 * de su nombre de usuario. Utiliza el {@link UserRepository} para acceder a la
 * información del usuario en la base de datos y devuelve un objeto
 * {@link UserDetails} que Spring Security puede utilizar para la
 * autenticación y autorización.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	/**
	 * Repositorio para acceder a la información de los usuarios en la base de
	 * datos.
	 */
	private final UserRepository userRepository;
	/**
	 * Constructor con dependencia para {@link UserRepository}. Spring injecta
	 * automáticamente esta dependencia.
	 *
	 * @param userRepository El repositorio de usuarios.
	 */
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	/**
	 * Carga los detalles de un usuario por su nombre de usuario. Este método es
	 * requerido por la interfaz {@link UserDetailsService}.
	 *
	 * @param user El nombre de usuario del usuario cuyos detalles se desean cargar.
	 * @return Un objeto {@link UserDetails} que contiene la información del
	 * usuario (implementado por la entidad {@code User} en este caso).
	 * @throws UsernameNotFoundException Si no se encuentra ningún usuario con el
	 * nombre de usuario especificado.
	 */
	@Override
	public UserDetails loadUserByUsername(String user) throws UsernameNotFoundException {
		return userRepository.findByUserName(user)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + user));
	}
}