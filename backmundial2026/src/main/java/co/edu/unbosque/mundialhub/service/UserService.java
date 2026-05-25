package co.edu.unbosque.mundialhub.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.mundialhub.dto.LoguinRequestDTO;
import co.edu.unbosque.mundialhub.dto.UserDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.User.Role;
import co.edu.unbosque.mundialhub.repository.PaymentRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.repository.UserStickerRepository;
import co.edu.unbosque.mundialhub.util.AESUtil;
import jakarta.transaction.Transactional;

/**
 * Servicio encargado de la gestión de usuarios, autenticación, cifrado de campos 
 * sensibles mediante AES y flujos de códigos de verificación de cuentas.
 * * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
@Service
public class UserService implements CRUDOperation<UserDTO, User> {

	private static final SecureRandom random = new SecureRandom();

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserStickerRepository userStickerRepo;

	@Autowired
	private ModelMapper modelMapper;

	private String code = "";

	/**
	 * Carga el contenido html de una plantilla desde los recursos del sistema.
	 *
	 * @param path Ruta del recurso a cargar.
	 * @return El contenido textual de la plantilla.
	 * @throws IOException Si ocurre una falla en el flujo de entrada/salida.
	 */
	private String loadTemplate(String path) throws IOException {
		var inputStream = getClass().getResourceAsStream(path);
		if (inputStream == null) {
			throw new IOException("Plantilla no encontrada en ruta: " + path);
		}
		return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	}

	/**
	 * Registra un nuevo usuario en la base de datos, encriptando su información 
	 * de identificación y credenciales personales. Envía un correo con el código de activación.
	 *
	 * @param newUserDto DTO con la información de registro del usuario.
	 * @return El DTO mapeado del usuario creado con sus campos legibles desencriptados.
	 * @throws Exceptions.ValidationException Si el nombre de usuario o email ya existen,
	 * o si los datos del formulario no cumplen con los requisitos de seguridad.
	 */
	@Override
	@Transactional
	public UserDTO create(UserDTO newUserDto) {
		validateUser(newUserDto);
		findUsernameAlreadyTaken(newUserDto.getUserName());
		findEmailAlreadyTaken(newUserDto.getEmail());

		try {
			User entity = modelMapper.map(newUserDto, User.class);
			entity.setId(null);

			entity.setPassword(passwordEncoder.encode(newUserDto.getPassword()));

			entity.setName(AESUtil.encrypt(newUserDto.getName()));
			entity.setEmail(AESUtil.encrypt(newUserDto.getEmail()));
			entity.setUserName(AESUtil.encrypt(newUserDto.getUserName()));
			entity.setIdentification(AESUtil.encrypt(newUserDto.getIdentification()));

			entity.setCity(newUserDto.getCity());
			entity.setTotalPoints(5000); // 5000 pruebas, 500 para despliegue final
			entity.setPackets(0);
			entity.setCountry(newUserDto.getCountry());

			entity.setEnabled(false);

			String generatedCode = generateCode();
			entity.setCode(generatedCode);
			entity.setVerificationExpires(LocalDateTime.now().plusMinutes(2));

			entity.setCreatedAt(LocalDateTime.now());
			entity.setUpdatedAt(LocalDateTime.now());

			if (entity.getRole() == null) {
				entity.setRole(Role.USER);
			}

			entity.setAccountNonExpired(true);
			entity.setAccountNonLocked(true);
			entity.setCredentialsNonExpired(true);

			userRepo.save(entity);

			sendConfirmationCode(newUserDto.getEmail(), generatedCode);

			UserDTO createdDto = modelMapper.map(entity, UserDTO.class);
			decryptSensitiveFields(createdDto);

			return createdDto;

		} catch (Exception e) {
			if (e instanceof Exceptions.ValidationException) {
				throw e;
			}
			throw new Exceptions.ValidationException("Error al crear el usuario: " + e.getMessage());
		}
	}

	/**
	 * Tarea programada (ejecución cada 60 segundos) encargada de limpiar de la base de datos 
	 * aquellos códigos de verificación cuyo límite temporal de uso haya expirado.
	 */
	@Scheduled(fixedRate = 60000)
	@Transactional
	public void cleanExpiredVerification() {
		List<User> expired = userRepo.findByVerificationExpires(LocalDateTime.now());
		expired.forEach(t -> {
			t.setCode(null);
			t.setVerificationExpires(null);
			userRepo.save(t);
		});
	}

	/**
	 * Compila y formatea una plantilla HTML inyectándole un código de 6 dígitos 
	 * y delega el envío al servicio de correos.
	 *
	 * @param destinationEmail Dirección de correo destino.
	 * @param code             Código alfanumérico generado de 6 caracteres.
	 * @throws Exceptions.ValidationException Si la plantilla no puede cargarse o el código es inválido.
	 */
	public void sendConfirmationCode(String destinationEmail, String code) {
		if (code == null || code.length() != 6) {
			throw new Exceptions.ValidationException("El código de verificación debe tener exactamente 6 dígitos.");
		}

		String subject = "Código de verificación - Mundial 2026 Hub";
		char[] d = code.toCharArray();

		try {
			String htmlTemplate = loadTemplate("/templates/code-email.html");
			String body = htmlTemplate.replace("{{D1}}", String.valueOf(d[0]))
					.replace("{{D2}}", String.valueOf(d[1]))
					.replace("{{D3}}", String.valueOf(d[2]))
					.replace("{{D4}}", String.valueOf(d[3]))
					.replace("{{D5}}", String.valueOf(d[4]))
					.replace("{{D6}}", String.valueOf(d[5]));

			emailService.sendEmailDefault(destinationEmail, subject, body);
		} catch (IOException e) {
			throw new Exceptions.ValidationException("Error al procesar la plantilla de confirmación: " + e.getMessage());
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Obtiene un usuario a partir de su ID único y retorna sus datos desencriptados.
	 *
	 * @param id Identificador único del usuario.
	 * @return El DTO con la información legible de la cuenta.
	 * @throws Exceptions.UserNotFoundException Si no se encuentra un usuario registrado con el ID.
	 */
	@Transactional
	public UserDTO findById(Long id) {
		User entity = userRepo.findById(id)
				.orElseThrow(() -> new Exceptions.UserNotFoundException(String.valueOf(id)));

		UserDTO dto = modelMapper.map(entity, UserDTO.class);
		decryptSensitiveFields(dto);
		return dto;
	}

	/**
	 * Obtiene todos los usuarios registrados en el sistema de manera legible.
	 *
	 * @return Lista completa de DTOs de usuarios.
	 */
	@Override
	@Transactional
	public List<UserDTO> findAll() {
		List<User> entityList = userRepo.findAll();
		List<UserDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);
			decryptSensitiveFields(dto);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Realiza una actualización parcial de la información de perfil del usuario.
	 *
	 * @param data DTO del usuario con las modificaciones requeridas.
	 * @return El DTO actualizado y desencriptado de la cuenta.
	 * @throws Exceptions.ValidationException   Si el ID del usuario no fue proveído.
	 * @throws Exceptions.UserNotFoundException Si el usuario a modificar no existe.
	 */
	@Transactional
	public UserDTO update(UserDTO data) {
		if (data == null || data.getId() == null) {
			throw new Exceptions.ValidationException("El ID del usuario es obligatorio para realizar modificaciones.");
		}

		User existingUser = userRepo.findById(data.getId())
				.orElseThrow(() -> new Exceptions.UserNotFoundException(String.valueOf(data.getId())));

		if (data.getUserName() != null) {
			existingUser.setUserName(AESUtil.encrypt(data.getUserName()));
		}
		if (data.getName() != null) {
			existingUser.setName(AESUtil.encrypt(data.getName()));
		}
		if (data.getEmail() != null) {
			existingUser.setEmail(AESUtil.encrypt(data.getEmail()));
		}
		if (data.getIdentification() != null) {
			existingUser.setIdentification(AESUtil.encrypt(data.getIdentification()));
		}
		if (data.getPassword() != null) {
			existingUser.setPassword(passwordEncoder.encode(data.getPassword()));
		}
		if (data.getRole() != null) {
			existingUser.setRole(data.getRole());
		}

		existingUser.setUpdatedAt(LocalDateTime.now());
		User updatedEntity = userRepo.save(existingUser);

		UserDTO updatedDto = modelMapper.map(updatedEntity, UserDTO.class);
		decryptSensitiveFields(updatedDto);

		return updatedDto;
	}

	/**
	 * Elimina a un usuario del sistema basándose en su ID.
	 *
	 * @param id Identificador único del usuario a dar de baja.
	 * @throws Exceptions.ValidationException   Si el ID provisto es nulo.
	 * @throws Exceptions.UserNotFoundException Si el usuario indicado no existe.
	 */
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null) {
			throw new Exceptions.ValidationException("El ID del usuario no puede ser nulo.");
		}
		paymentRepository.deleteByUserId(id);
		userStickerRepo.deleyeByUserId(id);
		
		
		
		User userToDelete = userRepo.findById(id)
				.orElseThrow(() -> new Exceptions.UserNotFoundException(String.valueOf(id)));

		userRepo.delete(userToDelete);
	}

	/**
	 * Busca una entidad de usuario basándose en su correo electrónico (efectúa búsqueda encriptada).
	 *
	 * @param email Correo electrónico en texto plano.
	 * @return La entidad {@link User} correspondiente.
	 * @throws Exceptions.UserNotFoundException Si no hay ningún usuario registrado con el correo.
	 */
	@Transactional
	public User findByEmail(String email) {
		String emailEncrypt = AESUtil.encrypt(email);
		return userRepo.findByEmail(emailEncrypt)
				.orElseThrow(() -> new Exceptions.UserNotFoundException("con email: " + email));
	}

	/**
	 * Evalúa si un correo electrónico está registrado en el sistema.
	 *
	 * @param email Correo electrónico a evaluar.
	 * @return true si el correo ya existe en el sistema, de lo contrario false.
	 */
	@Transactional
	public boolean findEmail(String email) {
		String emailEncrypt = AESUtil.encrypt(email);
		return userRepo.findByEmail(emailEncrypt).isPresent();
	}

	/**
	 * Evalúa si un nombre de usuario se encuentra ocupado por otra cuenta.
	 *
	 * @param username Nombre de usuario a evaluar en texto plano.
	 * @return false si está disponible.
	 * @throws Exceptions.ValidationException Si el parámetro es nulo o si el nombre ya existe.
	 */
	@Transactional
	public boolean findUsernameAlreadyTaken(String username) {
		if (username == null) {
			throw new Exceptions.ValidationException("El nombre de usuario no puede ser nulo.");
		}

		String encryptedUsername = AESUtil.encrypt(username);
		Optional<User> found = userRepo.findByUserName(encryptedUsername);
		if (found.isPresent()) {
			throw new Exceptions.ValidationException("El nombre de usuario ya está registrado en el sistema.");
		}
		return false;
	}

	/**
	 * Comprueba la disponibilidad de un correo electrónico.
	 *
	 * @param email Correo en texto plano.
	 * @return false si está disponible.
	 * @throws Exceptions.ValidationException Si el parámetro es nulo o si ya está en uso.
	 */
	@Transactional
	public boolean findEmailAlreadyTaken(String email) {
		if (email == null) {
			throw new Exceptions.ValidationException("El correo electrónico no puede ser nulo.");
		}

		String encryptedEmail = AESUtil.encrypt(email);
		Optional<User> found = userRepo.findByEmail(encryptedEmail);
		if (found.isPresent()) {
			throw new Exceptions.ValidationException("El correo electrónico ya está registrado en el sistema.");
		}
		return false;
	}

	/**
	 * Valida las credenciales de inicio de sesión de un usuario.
	 *
	 * @param username Nombre de usuario legible.
	 * @param password Contraseña entregada en texto plano.
	 * @throws Exceptions.InvalidCredentialsException Si las credenciales ingresadas son incorrectas o inválidas.
	 */
	@Transactional
	public void validateCredentials(String username, String password) {
		Optional<User> userOptional = userRepo.findByUserName(AESUtil.encrypt(username));
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (passwordEncoder.matches(password, user.getPassword())) {
				return; // Credenciales válidas
			}
		}
		throw new Exceptions.InvalidCredentialsException();
	}

	/**
	 * Registra un usuario en el sistema asignándole un rol específico de forma directa.
	 *
	 * @param data DTO del usuario a crear.
	 * @param rol  Nombre del rol textual (Debe concordar con el Enum {@link Role}).
	 * @throws Exceptions.ValidationException Si el rol no existe o si falla el proceso de registro.
	 */
	@Transactional
	public void createWithRol(UserDTO data, String rol) {
		try {
			data.setRole(Role.valueOf(rol.toUpperCase().trim()));
			create(data);
		} catch (IllegalArgumentException e) {
			throw new Exceptions.ValidationException("El rol especificado '" + rol + "' no es válido en el sistema.");
		}
	}

	/**
	 * Da de baja un usuario basándose en su alias público de perfil.
	 *
	 * @param username Nombre de usuario legible en texto plano.
	 * @throws Exceptions.UserNotFoundException Si el usuario indicado no existe en la base de datos.
	 */
	@Transactional
	public void deleteByUsername(String username) {
		User user = userRepo.findByUserName(AESUtil.encrypt(username))
				.orElseThrow(() -> new Exceptions.UserNotFoundException("con nombre de usuario: " + username));
		userRepo.delete(user);
	}

	/**
	 * Modifica los datos completos de un usuario a través de su identificador numérico,
	 * aplicando validaciones contra vulnerabilidades de inyección HTML/Scripts y duplicados.
	 *
	 * @param id      ID único del usuario.
	 * @param newData DTO cargado con los nuevos datos modificados.
	 * @return El DTO con la información salvada y desencriptada.
	 * @throws Exceptions.ValidationException   Si hay caracteres maliciosos, formatos incorrectos o duplicidad de datos.
	 * @throws Exceptions.UserNotFoundException Si no se encuentra el usuario registrado.
	 */
	@Override
	@Transactional
	public UserDTO updateById(Long id, UserDTO newData) {
		validateUserData(newData);

		User existingUser = userRepo.findById(id)
				.orElseThrow(() -> new Exceptions.UserNotFoundException(String.valueOf(id)));

		if (isAnyNewDataConflict(newData, existingUser)) {
			throw new Exceptions.ValidationException("Los datos ingresados entran en conflicto con la cuenta de otro usuario.");
		}

		updateUserFields(existingUser, newData);
		existingUser.setUpdatedAt(LocalDateTime.now());
		userRepo.save(existingUser);

		UserDTO dto = modelMapper.map(existingUser, UserDTO.class);
		decryptSensitiveFields(dto);
		return dto;
	}

	@Override
	@Transactional
	public long count() {
		return userRepo.count();
	}

	@Override
	@Transactional
	public boolean exist(Long id) {
		return userRepo.existsById(id);
	}

	/**
	 * Encripta el identificador único del DTO de login para búsquedas seguras indexadas en la BD.
	 *
	 * @param data DTO con el identificador del login (email o username).
	 * @return El identificador cifrado con el algoritmo AES.
	 * @throws Exceptions.ValidationException Si el identificador se encuentra vacío o es nulo.
	 */
	public String encryptLoginIdentifier(LoguinRequestDTO data) {
		if (data.getIdentifier() == null || data.getIdentifier().isBlank()) {
			throw new Exceptions.ValidationException("El identificador de inicio de sesión es obligatorio.");
		}
		return AESUtil.encrypt(data.getIdentifier().trim());
	}

	/**
	 * Actualiza la contraseña de un usuario mediante la verificación de su correo electrónico.
	 *
	 * @param email   Correo electrónico legible en texto plano.
	 * @param newData DTO que contiene la nueva clave a asignar.
	 * @throws Exceptions.ValidationException Si el formato de la clave no cumple con las directrices de seguridad globales.
	 */
	@Transactional
	public void updatePasswordByEmail(String email, UserDTO newData) {
		if (newData.getPassword() == null) {
			throw new Exceptions.ValidationException("La nueva contraseña es obligatoria.");
		}

		Exceptions exception = new Exceptions();
		if (!exception.isValidPassword(newData.getPassword())) {
			throw new Exceptions.ValidationException("La contraseña no cumple con el formato requerido (mínimo 8 caracteres, mayúscula, minúscula, número y un símbolo permitido).");
		}

		User user = findByEmail(email);
		user.setPassword(passwordEncoder.encode(newData.getPassword()));
		userRepo.save(user);
	}

	/**
	 * Reestablece de forma directa la contraseña de un usuario por olvido a través de su email.
	 *
	 * @param newData DTO cargado con el correo y la nueva contraseña.
	 * @throws Exceptions.ValidationException   Si el formato de contraseña es erróneo.
	 * @throws Exceptions.UserNotFoundException Si el correo no coincide con ninguna cuenta del sistema.
	 */
	@Transactional
	public void rememberPassword(UserDTO newData) {
		Exceptions exception = new Exceptions();
		if (newData.getPassword() == null || !exception.isValidPassword(newData.getPassword())) {
			throw new Exceptions.ValidationException("La estructura de la nueva contraseña no es válida.");
		}

		User user = userRepo.findByEmail(AESUtil.encrypt(newData.getEmail()))
				.orElseThrow(() -> new Exceptions.UserNotFoundException("asociado al correo electrónico provisto."));

		user.setPassword(passwordEncoder.encode(newData.getPassword()));
		userRepo.save(user);
	}

	/**
	 * Cambia el rol del sistema asignado a un usuario específico.
	 *
	 * @param id      ID único del usuario.
	 * @param newData DTO con el nuevo Rol deseado.
	 * @return El DTO mapeado con el rol actualizado.
	 * @throws Exceptions.ValidationException   Si el campo del rol viene nulo.
	 * @throws Exceptions.UserNotFoundException Si no se encuentra el usuario en base de datos.
	 */
	@Transactional
	public UserDTO updateRol(Long id, UserDTO newData) {
		User user = userRepo.findById(id)
				.orElseThrow(() -> new Exceptions.UserNotFoundException(String.valueOf(id)));

		if (newData.getRole() == null) {
			throw new Exceptions.ValidationException("El rol a asignar es obligatorio.");
		}

		user.setRole(newData.getRole());
		userRepo.save(user);
		return modelMapper.map(user, UserDTO.class);
	}

	/**
	 * Verifica la existencia de un usuario mediante su nombre de usuario en texto plano.
	 *
	 * @param user Nombre de usuario a buscar.
	 * @return true si la cuenta existe en el sistema.
	 */
	@Transactional
	public boolean isUserExist(String user) {
		String encrypted = AESUtil.encrypt(user);
		return userRepo.findByUserName(encrypted).isPresent();
	}

	/**
	 * Recupera la información pública de un usuario mediante su alias de cuenta.
	 *
	 * @param user Alias de usuario en texto plano.
	 * @return Una lista con el DTO desencriptado del usuario en caso de encontrarse.
	 */
	@Transactional
	public List<UserDTO> getByUser(String user) {
		Optional<User> optionalUser = userRepo.findByUserName(AESUtil.encrypt(user));
		List<UserDTO> dtoList = new ArrayList<>();

		optionalUser.ifPresent(entity -> {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);
			decryptSensitiveFields(dto);
			dtoList.add(dto);
		});
		return dtoList;
	}

	/**
	 * Actualiza de forma segura la contraseña desde la interfaz de configuración del perfil del usuario,
	 * validando que la contraseña antigua coincida con el registro actual.
	 *
	 * @param id          ID único del usuario.
	 * @param newData     DTO con la nueva contraseña propuesta.
	 * @param oldPassword Contraseña actual proporcionada para verificación en texto plano.
	 * @return El DTO de usuario mapeado.
	 * @throws Exceptions.ValidationException   Si la clave nueva no cumple el patrón o si la clave actual es incorrecta.
	 * @throws Exceptions.UserNotFoundException Si el ID del usuario no se localiza.
	 */
	@Transactional
	public UserDTO updatePassword(Long id, UserDTO newData, String oldPassword) {
		if (newData.getPassword() == null) {
			throw new Exceptions.ValidationException("La nueva contraseña es obligatoria.");
		}

		Exceptions exception = new Exceptions();
		if (!exception.isValidPassword(newData.getPassword())) {
			throw new Exceptions.ValidationException("El formato de la nueva contraseña es inválido.");
		}

		User user = userRepo.findById(id)
				.orElseThrow(() -> new Exceptions.UserNotFoundException(String.valueOf(id)));
		
		if (passwordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(passwordEncoder.encode(newData.getPassword()));
			userRepo.save(user);
			return modelMapper.map(user, UserDTO.class);
		} else {
			throw new Exceptions.ValidationException("La contraseña actual proporcionada es incorrecta.");
		}
	}

	/**
	 * Genera un nuevo código de activación de cuenta e invalida el anterior, enviando 
	 * el token generado a través de correo electrónico.
	 *
	 * @param identifier Nombre de usuario o Email del solicitante en texto plano.
	 * @throws Exceptions.UserNotFoundException Si no se asocia a ninguna cuenta del sistema.
	 * @throws Exceptions.ValidationException   Si la cuenta del usuario ya se encuentra activada/verificada.
	 */
	@Transactional
	public void updateVerificationCodeByUserName(String identifier) {
		User user = null;
		String encryptedId = AESUtil.encrypt(identifier);

		Optional<User> optionalUser = userRepo.findByUserName(encryptedId);
		if (!optionalUser.isPresent()) {
			Optional<User> optionalUserEmail = userRepo.findByEmail(encryptedId);
			if (optionalUserEmail.isPresent()) {
				user = optionalUserEmail.get();
			}
		} else {
			user = optionalUser.get();
		}

		if (user == null) {
			throw new Exceptions.UserNotFoundException("con el identificador: " + identifier);
		}

		if (user.getCode() == null) {
			throw new Exceptions.ValidationException("La cuenta del usuario ya se encuentra verificada.");
		}

		user.setCode(generateCode());
		user.setVerificationExpires(LocalDateTime.now().plusMinutes(5));
		userRepo.save(user);

		sendConfirmationCode(AESUtil.decrypt(user.getEmail()), user.getCode());
	}

	public String decryptUser(String user) {
		if (user != null) {
			return AESUtil.decrypt(user);
		}
		return user;
	}

	public String encryptUser(String user) {
		if (user != null) {
			return AESUtil.encrypt(user);
		}
		return user;
	}

	private String generateCode() {
		int codeGenerated = random.nextInt(900000) + 100000;
		return String.valueOf(codeGenerated);
	}

	/**
	 * Ejecuta las validaciones globales de sanidad frente a símbolos HTML maliciosos 
	 * y verifica el formato estándar de contraseñas y correos.
	 */
	private void validateUserData(UserDTO newData) {
		Exceptions exceptionHandler = new Exceptions();
		if (newData.getName() != null && exceptionHandler.containsHtmlSymbols(newData.getName())) {
			throw new Exceptions.ValidationException("El nombre contiene símbolos HTML o caracteres prohibidos.");
		}
		if (newData.getUserName() != null && exceptionHandler.containsHtmlSymbols(newData.getUserName())) {
			throw new Exceptions.ValidationException("El nombre de usuario contiene símbolos HTML o caracteres prohibidos.");
		}
		if (newData.getPassword() != null && !exceptionHandler.isValidPassword(newData.getPassword())) {
			throw new Exceptions.ValidationException("El formato de la contraseña es inválido.");
		}
		if (newData.getEmail() != null && !exceptionHandler.isGmailValid(newData.getEmail())) {
			throw new Exceptions.ValidationException("La dirección de correo electrónico provista no es un Gmail válido.");
		}
	}

	private boolean isAnyNewDataConflict(UserDTO newData, User existingUser) {
		if (newData.getUserName() != null && !AESUtil.encrypt(newData.getUserName()).equals(existingUser.getUserName())) {
			if (userRepo.findByUserName(AESUtil.encrypt(newData.getUserName())).isPresent()) {
				return true;
			}
		}
		if (newData.getName() != null && !AESUtil.encrypt(newData.getName()).equals(existingUser.getName())) {
			if (userRepo.findByName(AESUtil.encrypt(newData.getName())).isPresent()) {
				return true;
			}
		}
		if (newData.getEmail() != null && !AESUtil.encrypt(newData.getEmail()).equals(existingUser.getEmail())) {
			if (userRepo.findByEmail(AESUtil.encrypt(newData.getEmail())).isPresent()) {
				return true;
			}
		}
		return false;
	}

	private void updateUserFields(User existingUser, UserDTO newData) {
		if (newData.getUserName() != null) {
			String currentDecrypted = AESUtil.decrypt(existingUser.getUserName());
			if (!newData.getUserName().equals(currentDecrypted)) {
				existingUser.setUserName(AESUtil.encrypt(newData.getUserName()));
			}
		}
		if (newData.getName() != null) {
			String currentDecrypted = AESUtil.decrypt(existingUser.getName());
			if (!newData.getName().equals(currentDecrypted)) {
				existingUser.setName(AESUtil.encrypt(newData.getName()));
			}
		}
		if (newData.getEmail() != null) {
			String currentDecrypted = AESUtil.decrypt(existingUser.getEmail());
			if (!newData.getEmail().equals(currentDecrypted)) {
				existingUser.setEmail(AESUtil.encrypt(newData.getEmail()));
			}
		}
		if (newData.getCountry() != null) {
			existingUser.setCountry(newData.getCountry());
		}
		if (newData.getCity() != null) {
			existingUser.setCity(newData.getCity());
		}
		if (newData.getFavoriteTeam() != null) {
			existingUser.setFavoriteTeam(newData.getFavoriteTeam());
		}
		if (newData.getAvatarUrl() != null) {
			existingUser.setAvatarUrl(newData.getAvatarUrl());
		}
	}

	private void decryptSensitiveFields(UserDTO dto) {
		if (dto.getName() != null) {
			dto.setName(AESUtil.decrypt(dto.getName()));
		}
		if (dto.getUserName() != null) {
			dto.setUserName(AESUtil.decrypt(dto.getUserName()));
		}
		if (dto.getEmail() != null) {
			dto.setEmail(AESUtil.decrypt(dto.getEmail()));
		}
		if (dto.getIdentification() != null) {
			dto.setIdentification(AESUtil.decrypt(dto.getIdentification()));
		}
	}

	private void validateUser(UserDTO userDto) {
		if (userDto == null) {
			throw new Exceptions.ValidationException("Los datos del usuario no pueden ser nulos.");
		}
		if (userDto.getUserName() == null || userDto.getUserName().isBlank()) {
			throw new Exceptions.ValidationException("El nombre de usuario es obligatorio.");
		}
		if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
			throw new Exceptions.ValidationException("El correo electrónico es obligatorio.");
		}
		if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
			throw new Exceptions.ValidationException("La contraseña es obligatoria.");
		}
	}
}