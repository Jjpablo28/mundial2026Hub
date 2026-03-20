package co.edu.unbosque.mundialhub.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.User.Role;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.util.AESUtil;

import java.util.Optional;

@Service
public class DatabaseInitializer {

	private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;

	public DatabaseInitializer(UserRepository userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;

	}

	/**
	 * Este método ejecuta toda la lógica de inicialización bajo una sola
	 * transacción. Esto evita que el EntityManager se cierre antes de completar las
	 * relaciones complejas.
	 */
	@Transactional
	public void runDataInitialization() {
		log.info("Iniciando la carga de datos de configuración...");

		createDefaultUser("admin", Role.ADMIN);
		createDefaultUser("normaluser", Role.USER);
		createDefaultUser("temporalUser", Role.TEMPORAL);
		createDefaultUser("supportUser", Role.SUPPORT);
		createDefaultUser("operatorUser", Role.OPERATOR);


		log.info("Proceso de inicialización finalizado con éxito.");
	}

	private void createDefaultUser(String username, Role role) {
		String encryptedUsername = AESUtil.encrypt(username);
		Optional<User> found = userRepo.findByUserName(encryptedUsername);

		if (found.isPresent()) {
			log.info("El usuario '{}' ya existe, omitiendo creación.", username);
		} else {
			User newUser = new User(null, encryptedUsername, null, null, passwordEncoder.encode("1234567890"), null,
					null, null, null, null, null,null,null, null, null, true, true, true, true);
			newUser.setRole(role);
			newUser.setEnabled(true);
			newUser.setAccountNonExpired(true);
			newUser.setAccountNonLocked(true);
			newUser.setCredentialsNonExpired(true);
			userRepo.save(newUser);
			log.info("Precargando usuario: {}", username);
		}
	}


}