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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.mundialhub.dto.LoguinRequestDTO;
import co.edu.unbosque.mundialhub.dto.UserDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.User.Role;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.util.AESUtil;

import jakarta.mail.MessagingException;

/**
 * Servicio encargado de la lógica de negocio relacionada con la entidad
 * {@link User}.
 * <p>
 * Implementa la interfaz {@link CRUDOperation} y añade lógica de seguridad.
 */

@Service
public class UserService implements CRUDOperation<UserDTO, User> {

	private static final SecureRandom random = new SecureRandom();

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ModelMapper modelMapper;



	// --- Métodos de la Interfaz CRUDOperation ---

	/**
	 * Crea un nuevo usuario. Codifica la contraseña y encripta campos sensibles.
	 *
	 * @param newUserDto El DTO con la información del nuevo usuario.
	 * @return El {@link UserDTO} del usuario creado y desencriptado.
	 * @throws Exceptions Si el nombre de usuario o email ya están registrados, o si
	 *                    hay un error. NOTA: Se asume que UserDTO incluye
	 *                    getPassword().
	 */

	String code = "";
/*
	public UserDTO createU(UserDTO newUserDto) throws IllegalArgumentException {

		if (findUsernameAlreadyTaken(newUserDto.getUserName())) {
			System.err.println("El nombre de usuario ya está registrado.");
			throw new IllegalArgumentException("El nombre de usuario ya está registrado.");
		}
		if (findEmailAlreadyTaken(newUserDto.getEmail())) {
			System.err.println("El email ya está registrado.");
			throw new IllegalArgumentException("El email ya está registrado.");
		}

		try {
			User entity = modelMapper.map(newUserDto, User.class);
			entity.setId(null);

			entity.setPassword(passwordEncoder.encode(newUserDto.getPassword()));

			entity.setName(AESUtil.encrypt(entity.getName()));
			entity.setEmail(AESUtil.encrypt(entity.getEmail()));

			entity.setEnabled(false);

			code = generateCode();

			entity.setCode(code);
			entity.setVerificationExpires(LocalDateTime.now().plusMinutes(10));

			entity.setUserName(AESUtil.encrypt(entity.getUsername()));

			String identificationStr = entity.getIdentification();
			entity.setIdentification(AESUtil.encrypt(identificationStr));

			if (entity.getRole() == null) {
				entity.setRole(Role.USER);
			}
			entity.setAccountNonExpired(true);
			entity.setAccountNonLocked(true);
			entity.setCredentialsNonExpired(true);

			// 4. Persistir la entidad
			userRepo.save(entity);

			UserDTO createdDto = modelMapper.map(entity, UserDTO.class);
			decryptSensitiveFields(createdDto);

			sendConfirmationCode(newUserDto.getEmail(), code);

			return createdDto;

		} catch (IllegalArgumentException e) {
			System.err.println(e);
			throw e;
		} catch (Exception e) {

			throw new IllegalArgumentException("Error al crear el usuario: " + e.getMessage());
		}
	}

	public void sendConfirmationCode(String destinationEmail, String code) throws IOException, MessagingException {

		if (code == null || code.length() != 6) {
			throw new IllegalArgumentException("El código debe tener 6 dígitos");
		}

		String subject = "Código de verificación - Veltrix Pass";

		char[] d = code.toCharArray();

		String htmlTemplate = loadTemplate("/templates/code-email.html");

		String body = htmlTemplate.replace("{{D1}}", String.valueOf(d[0])).replace("{{D2}}", String.valueOf(d[1]))
				.replace("{{D3}}", String.valueOf(d[2])).replace("{{D4}}", String.valueOf(d[3]))
				.replace("{{D5}}", String.valueOf(d[4])).replace("{{D6}}", String.valueOf(d[5]));

		emailService.sendEmailDefault(destinationEmail, subject, body);
	}
*/
	private String loadTemplate(String path) throws IOException {
		return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	private String generateCode() {

		int code = random.nextInt(900000) + 100000; // rango 100000–999999
		return String.valueOf(code);
	}

	/**
	 * Obtiene un usuario por su ID. Usa el método findById(Integer userId) de
	 * UserRepository.
	 */
	public UserDTO findById(Integer id) throws IllegalArgumentException {
		// Sintaxis de lambda corregida: .orElseThrow(() -> new Exceptions(...))
		User entity = userRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		UserDTO dto = modelMapper.map(entity, UserDTO.class);
		decryptSensitiveFields(dto);
		return dto;
	}

	/**
	 * Obtiene todos los usuarios.
	 */
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
	 * Actualiza la información de un usuario.
	 */
	public UserDTO update(UserDTO data) throws IllegalArgumentException {
		// Sintaxis de lambda corregida: .orElseThrow(() -> new Exceptions(...))
		User existingUser = userRepo.findById(data.getId()).orElseThrow(
				() -> new IllegalArgumentException("Usuario no encontrado para actualizar con ID: " + data.getId()));

		try {
			// Actualizaciones (solo ejemplo para campos en el DTO)
			if (data.getUserName() != null) {
				existingUser.setUserName(data.getUserName());
			}

			// Encriptar y actualizar si los datos cambian
			if (data.getName() != null) {
				existingUser.setName(AESUtil.encrypt(data.getName()));
			}
			if (data.getEmail() != null) {
				existingUser.setEmail(AESUtil.encrypt(data.getEmail()));
			}
			if (data.getIdentification() != null) {
				String identificationStr = String.valueOf(data.getIdentification());
				existingUser.setIdentification(AESUtil.encrypt(identificationStr));
			}

			if (data.getRole() != null) {
				existingUser.setRole(data.getRole());
			}

			// Guardar y mapear
			User updatedEntity = userRepo.save(existingUser);
			UserDTO updatedDto = modelMapper.map(updatedEntity, UserDTO.class);
			decryptSensitiveFields(updatedDto);
			return updatedDto;

		} catch (Exception e) {
			throw new IllegalArgumentException("Error al actualizar el usuario: " + e.getMessage());
		}
	}

	public void deleteById(Integer id) throws IllegalArgumentException {
		// Sintaxis de lambda corregida: .orElseThrow(() -> new Exceptions(...))
		User userToDelete = userRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para eliminar con ID: " + id));

		userRepo.delete(userToDelete);
	}

	private void decryptSensitiveFields(UserDTO dto) {
		try {
			if (dto.getName() != null) {
				dto.setName(AESUtil.decrypt(dto.getName()));
			}
			if (dto.getEmail() != null) {
				dto.setEmail(AESUtil.decrypt(dto.getEmail()));
			}

			// Desencriptar 'identification' (String a int)
			String encryptedIdentification = String.valueOf(dto.getIdentification());
			String decryptedIdentificationStr = AESUtil.decrypt(encryptedIdentification);
			dto.setIdentification(decryptedIdentificationStr);

		} catch (Exception e) {
			throw new IllegalArgumentException("Error al desencriptar campos del usuario: " + e.getMessage());
		}
	}

	public User findByEmail(String email) {

		String emailEncrypt = AESUtil.encrypt(email);

		return userRepo.findByEmail(emailEncrypt)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
	}

	/**
	 * Verifica si un nombre de usuario ya está registrado en la base de datos.
	 */
	public boolean findUsernameAlreadyTaken(String username) {
		// CORREGIDO: Uso de findByUserName (según tu UserRepository)
		Optional<User> found = userRepo.findByUserName(username);
		return found.isPresent();
	}

	/**
	 * Verifica si un correo electrónico ya está registrado (busca el valor
	 * encriptado).
	 */
	public boolean findEmailAlreadyTaken(String email) {
		try {
			// Encriptar el email de entrada para la búsqueda
			String encryptedEmail = AESUtil.encrypt(email);
			// CORREGIDO: Uso de findByEmail (según tu UserRepository)
			Optional<User> found = userRepo.findByEmail(encryptedEmail);
			return found.isPresent();
		} catch (Exception e) {
			// En caso de error de encriptación, asumimos que no se encontró
			return false;
		}
	}

	/**
	 * Valida las credenciales de un usuario.
	 */
	public int validateCredentials(String username, String password) {
		// CORREGIDO: Uso de findByUserName
		Optional<User> userOptional = userRepo.findByUserName(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			// Comparar la contraseña ingresada con la codificada de la BD
			if (passwordEncoder.matches(password, user.getPassword())) {
				return 0; // Credenciales válidas
			}
		}
		return 1; // Credenciales inválidas
	}

	@SuppressWarnings("unused")
	private boolean areRequiredFieldsPresent(UserDTO data) {

		return data.getEmail() != null && !data.getEmail().isEmpty() && data.getUserName() != null
				&& !data.getUserName().isEmpty() && data.getPassword() != null && !data.getPassword().isEmpty()
				&& data.getName() != null && !data.getName().isEmpty();

	}

	@Override
	public int createWithRol(UserDTO data, String rol) {
		try {
			data.setRole(Role.valueOf(rol));
		//	createU(data); 
			return 0;
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			return 1;
		}
	}

	@SuppressWarnings("unused")
	private boolean isAnyUserDetailAlreadyTaken(User entity) {
		if (entity.getUserName() != null && userRepo.findByUserName(entity.getUserName()).isPresent()) {
			return true;
		}
		if (entity.getName() != null && userRepo.findByName(entity.getName()).isPresent()) {
			return true;
		}
		if (entity.getEmail() != null && userRepo.findByEmail(entity.getEmail()).isPresent()) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private int validateUserDataContent(User userEntity, Exceptions exceptionHandler) {
		if (userEntity.getName() != null && exceptionHandler.containsHtmlSymbols(userEntity.getName())) {
			return 4;
		}
		if (userEntity.getUserName() != null && exceptionHandler.containsHtmlSymbols(userEntity.getUserName())) {
			return 5;
		}

		if (userEntity.getPassword() != null && !exceptionHandler.isValidPassword(userEntity.getPassword())) {

		}
		if (userEntity.getEmail() != null && !exceptionHandler.isGmailValid(userEntity.getEmail())) {
			return 7;
		}
		return 3;
	}

	@SuppressWarnings("unused")
	private void validateRole(String rol, Exceptions exceptionHandler) {
		if (exceptionHandler.containsRole(rol)) { // Asumiendo que `containsRole` lanza o retorna false para rol
													// incorrecto.
			throw new IllegalArgumentException("El rol es incorrecto");
		}
	}

	@Override
	public List<UserDTO> getAll() {
		List<User> entityList = userRepo.findAll();
		List<UserDTO> dtoList = new ArrayList<>();
		entityList.forEach((entity) -> {

			UserDTO dto = modelMapper.map(entity, UserDTO.class);

			if (dto.getEmail() != null) {
				dto.setEmail(AESUtil.decrypt(dto.getEmail()));
			}
			if (dto.getUserName() != null) {
				dto.setUserName(AESUtil.decrypt(dto.getUserName()));
			}
			if (dto.getName() != null) {
				dto.setName(AESUtil.decrypt(dto.getName()));
			}
			if (dto.getIdentification() != null) {
				dto.setIdentification(AESUtil.decrypt(dto.getIdentification()));
			}
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Elimina un usuario de la base de datos por su ID.
	 *
	 * @param id El ID del usuario a eliminar.
	 * @return 0 si la eliminación fue exitosa, 1 si el usuario con el ID
	 *         especificado no existe.
	 */
	@Override
	public int deleteById(Long id) {
		Optional<User> found = userRepo.findById(id);
		if (found.isPresent()) {
			userRepo.delete(found.get());
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Elimina un usuario de la base de datos por su nombre de usuario (encriptado).
	 *
	 * @param username El nombre de usuario (encriptado) del usuario a eliminar.
	 * @return 0 si la eliminación fue exitosa, 1 si no se encontró ningún usuario
	 *         con el nombre de usuario especificado.
	 */
	public int deleteByUsername(String username) {
		Optional<User> found = userRepo.findByUserName(encryptUser(username));
		if (found.isPresent()) {
			userRepo.delete(found.get());
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public int updateById(Long id, UserDTO newData) {

		if (validateUserData(newData) != 3) {
			return validateUserData(newData);

		}
		Optional<User> foundUserOptional = userRepo.findById(id);

		if (!foundUserOptional.isPresent()) {
			return 2;
		}

		User existingUser = foundUserOptional.get();

		if (isAnyNewDataConflict(newData, existingUser)) {

			return 1;
		}

		updateUserFields(existingUser, newData);

		userRepo.save(existingUser);
		return 0;
	}

	private int validateUserData(UserDTO newData) {
		Exceptions exceptionHandler = new Exceptions();
		if (newData.getName() != null && exceptionHandler.containsHtmlSymbols(newData.getName())) {
			return 4;
		}
		if (newData.getUserName() != null && exceptionHandler.containsHtmlSymbols(newData.getUserName())) {
			return 5;
		}

		if (newData.getPassword() != null && !exceptionHandler.isValidPassword(newData.getPassword())) {
			return 6;

		}
		if (newData.getEmail() != null && !exceptionHandler.isGmailValid(newData.getEmail())) {
			return 7;
		}
		return 3;

	}

	private boolean isAnyNewDataConflict(UserDTO newData, User existingUser) {
		if (newData.getUserName() != null && !newData.getUserName().equals(existingUser.getUserName())) {
			if (userRepo.findByUserName(newData.getUserName()).isPresent()) {
				return true;
			}
		}
		if (newData.getName() != null && !newData.getName().equals(existingUser.getName())) {
			if (userRepo.findByName(newData.getName()).isPresent()) {
				return true;
			}
		}
		if (newData.getEmail() != null && !newData.getEmail().equals(existingUser.getEmail())) {
			if (userRepo.findByEmail(newData.getEmail()).isPresent()) {
				return true;
			}
		}
		return false;
	}

	private void updateUserFields(User existingUser, UserDTO newData) {
		if (newData.getUserName() != null && !newData.getUserName().equals(existingUser.getUserName())) {
			existingUser.setUserName(AESUtil.encrypt(newData.getUserName()));
		}
		if (newData.getName() != null && !newData.getName().equals(existingUser.getName())) {
			existingUser.setName(AESUtil.encrypt(newData.getName()));
		}

		if (newData.getEmail() != null && !newData.getEmail().equals(existingUser.getEmail())) {
			existingUser.setEmail(AESUtil.encrypt(newData.getEmail()));
		}
	}

	@Override
	public long count() {
		return userRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return userRepo.existsById(id);
	}

	@Override
	public User encrypt(UserDTO data) {
		User entity = modelMapper.map(data, User.class);

		if (entity.getUserName() != null) {
			entity.setUserName(AESUtil.encrypt(entity.getUserName()));

		}
		if (entity.getName() != null) {
			entity.setName(AESUtil.encrypt(entity.getName()));

		}
		if (entity.getEmail() != null) {
			entity.setEmail(AESUtil.encrypt(entity.getEmail()));

		}
		if (entity.getName() != null) {
			entity.setName(AESUtil.encrypt(entity.getName()));
		}

		return entity;
	}

	public String encryptLoginIdentifier(LoguinRequestDTO data) {

		if (data.getIdentifier() == null || data.getIdentifier().isBlank()) {
			throw new IllegalArgumentException("Identifier is required");
		}

		return AESUtil.encrypt(data.getIdentifier().trim());
	}

	@Override
	public String decrypt(UserDTO data) {

		User entity = modelMapper.map(data, User.class);

		if (entity.getUserName() != null) {
			entity.setUserName(AESUtil.decrypt(entity.getUserName()));

		}
		if (entity.getName() != null) {
			entity.setName(AESUtil.decrypt(entity.getName()));

		}
		if (entity.getEmail() != null) {
			entity.setEmail(AESUtil.decrypt(entity.getEmail()));

		}
		if (entity.getName() != null) {
			entity.setName(AESUtil.decrypt(entity.getName()));
		}

		return null;
	}

	public int updatePasswordByEmail(String email, UserDTO newData) {

		if (newData.getPassword() == null) {
			return 4;
		}

		Exceptions exception = new Exceptions();

		if (!exception.isValidPassword(newData.getPassword())) {
			return 4;
		}

		User user;
		try {
			user = findByEmail(email);
		} catch (RuntimeException e) {
			return 2;
		}

		user.setPassword(passwordEncoder.encode(newData.getPassword()));
		userRepo.save(user);

		return 0;
	}

	public int rememberPassword(UserDTO newData) {
		Optional<User> found = userRepo.findByUserName(AESUtil.encrypt(newData.getUserName()));
		Exceptions exception = new Exceptions();
		if (newData.getPassword() != null) {
			if (!exception.isValidPassword(newData.getPassword())) {
				return 4;
			}
		}
		if (found.isPresent()) {
			User temp = found.get();
			if (temp.getPassword() != null) {

				temp.setPassword(passwordEncoder.encode(newData.getPassword()));

			}

			userRepo.save(temp);
			return 0;
		}

		if (!found.isPresent()) {
			return 2;
		} else {
			return 3;
		}

	}

	public int updateGmail(Long id, UserDTO newData) {

		Optional<User> optionalUser = userRepo.findById(id);

		if (optionalUser.isEmpty()) {
			return 0;
		}

		User user = optionalUser.get();

		if (newData.getEmail() == null || newData.getEmail().isBlank()) {
			return 0;
		}

		user.setEmail(AESUtil.encrypt(newData.getEmail()));
		userRepo.save(user);

		return 1;
	}

	public int updateRol(Long id, UserDTO newData) {

		Optional<User> optionalUser = userRepo.findById(id);

		if (optionalUser.isEmpty()) {
			return 0;
		}

		User user = optionalUser.get();

		if (newData.getRole() == null) {
			return 2;
		}

		user.setRole(newData.getRole());
		userRepo.save(user);

		return 1;
	}

	public boolean isUserExist(String user) {
		user = AESUtil.encrypt(user);
		Optional<User> found = userRepo.findByUserName(user);
		if (found.isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	public List<UserDTO> getByUser(String user) {

		Optional<User> optionalUser = userRepo.findByUserName(user);
		List<UserDTO> dtoList = new ArrayList<>();

		optionalUser.ifPresent(entity -> {

			UserDTO dto = modelMapper.map(entity, UserDTO.class);

			if (dto.getUserName() != null) {
				dto.setUserName(AESUtil.decrypt(dto.getUserName()));
			}

			if (dto.getEmail() != null) {
				dto.setEmail(AESUtil.decrypt(dto.getEmail()));
			}

			if (dto.getName() != null) {
				dto.setName(AESUtil.decrypt(dto.getName()));
			}

			dtoList.add(dto);
		});

		return dtoList;
	}

	public int updatePassword(Long id, UserDTO newData) {
		Optional<User> found = userRepo.findById(id);
		Exceptions exception = new Exceptions();
		if (newData.getPassword() != null) {
			if (!exception.isValidPassword(newData.getPassword())) {

				return 4;
			}
		}
		if (found.isPresent()) {
			User temp = found.get();

			if (temp.getPassword() != null) {
				temp.setPassword(passwordEncoder.encode(newData.getPassword()));
			}
			userRepo.save(temp);
			return 0;
		}

		if (!found.isPresent()) {
			return 2;
		} else {
			return 3;
		}
	}

	public boolean existByUserName(String userName) {
		return userRepo.findByUserName(AESUtil.encrypt(userName)).isPresent();
	}

	public int updateVerificationCodeByUserName(String userName, String code, LocalDateTime expires) {

		User user = userRepo.findByUserName(AESUtil.encrypt(userName))
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (user.getCode() != null) {
			user.setCode(code);
			user.setVerificationExpires(expires);

			userRepo.save(user);
			return 0;
		} else {
			new RuntimeException("User already verified");
			return 1;
		}

	}

	/**
	 * Desencripta una cadena de texto que representa información de un usuario.
	 * Utiliza la utilidad {@link AESUtil} para realizar el proceso de
	 * desencriptación.
	 *
	 * @param user La cadena de texto encriptada que contiene información del
	 *             usuario.
	 * @return La cadena de texto desencriptada con la información del usuario.
	 */
	public String decryptUser(String user) {
		if (user != null) {
			user = AESUtil.decrypt(user);

		}
		return user;
	}

	/**
	 * Encripta una cadena de usuario utilizando el algoritmo AES.
	 *
	 * @param user La cadena de usuario que se va a encriptar.
	 * @return La cadena de usuario encriptada. Si la entrada {@code user} es nula,
	 *         retorna {@code null}.
	 */
	public String encryptUser(String user) {
		if (user != null) {
			user = AESUtil.encrypt(user);

		}
		return user;
	}

	@Override
	public int create(UserDTO data) {
		// TODO Auto-generated method stub
		return 0;
	}
	}


