package co.edu.unbosque.mundialhub.service;

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

//import jakarta.mail.MessagingException;

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

	public UserDTO createU(UserDTO newUserDto) {

		validateUser(newUserDto);
		findUsernameAlreadyTaken(newUserDto.getUserName());
		findUsernameAlreadyTaken(newUserDto.getEmail());

		try {

			User entity = modelMapper.map(newUserDto, User.class);
			entity.setId(null);

			entity.setPassword(passwordEncoder.encode(newUserDto.getPassword()));

			entity.setName(AESUtil.encrypt(entity.getName()));
			entity.setEmail(AESUtil.encrypt(entity.getEmail()));
			entity.setUserName(AESUtil.encrypt(entity.getUserName()));
			entity.setIdentification(AESUtil.encrypt(entity.getIdentification()));

			entity.setEnabled(false);

			String code = generateCode();

			entity.setCode(code);
			entity.setVerificationExpires(LocalDateTime.now().plusMinutes(10));

			entity.setCreatedAt(LocalDateTime.now());
			entity.setUpdatedAt(LocalDateTime.now());

			if (entity.getRole() == null) {
				entity.setRole(Role.USER);
			}

			entity.setAccountNonExpired(true);
			entity.setAccountNonLocked(true);
			entity.setCredentialsNonExpired(true);

			userRepo.save(entity);

			// sendConfirmationCode(newUserDto.getEmail(), code);

			UserDTO createdDto = modelMapper.map(entity, UserDTO.class);
			decryptSensitiveFields(createdDto);

			return createdDto;

		} catch (Exception e) {
			throw new IllegalArgumentException("Error al crear el usuario: " + e.getMessage());
		}
	}

	/*
	 * public void sendConfirmationCode(String destinationEmail, String code) throws
	 * IOException, MessagingException {
	 * 
	 * if (code == null || code.length() != 6) { throw new
	 * IllegalArgumentException("El código debe tener 6 dígitos"); }
	 * 
	 * String subject = "Código de verificación - Veltrix Pass";
	 * 
	 * char[] d = code.toCharArray();
	 * 
	 * String htmlTemplate = loadTemplate("/templates/code-email.html");
	 * 
	 * String body = htmlTemplate.replace("{{D1}}",
	 * String.valueOf(d[0])).replace("{{D2}}", String.valueOf(d[1]))
	 * .replace("{{D3}}", String.valueOf(d[2])).replace("{{D4}}",
	 * String.valueOf(d[3])) .replace("{{D5}}",
	 * String.valueOf(d[4])).replace("{{D6}}", String.valueOf(d[5]));
	 * 
	 * }
	 */

	/*
	 * private String loadTemplate(String path) throws IOException { return new
	 * String(getClass().getResourceAsStream(path).readAllBytes(),
	 * StandardCharsets.UTF_8); }
	 */
	private void validateUser(UserDTO userDto) {

		if (userDto == null) {
			throw new IllegalArgumentException("User data cannot be null");
		}

		if (userDto.getUserName() == null || userDto.getUserName().isBlank()) {
			throw new IllegalArgumentException("Username is required");
		}

		if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}

		if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
			throw new IllegalArgumentException("Password is required");
		}

		if (findUsernameAlreadyTaken(userDto.getUserName())) {
			throw new IllegalArgumentException("Username already taken");
		}

		if (findEmailAlreadyTaken(userDto.getEmail())) {
			throw new IllegalArgumentException("Email already registered");
		}
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
	public UserDTO findById(Integer id) {

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
	public UserDTO update(UserDTO data) {

		if (data == null || data.getId() == null) {
			throw new IllegalArgumentException("User ID is required for update");
		}

		User existingUser = userRepo.findById(data.getId()).orElseThrow(
				() -> new IllegalArgumentException("Usuario no encontrado para actualizar con ID: " + data.getId()));

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

	public void deleteById(Integer id) {

		if (id == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

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

			if (dto.getIdentification() != null) {
				dto.setIdentification(AESUtil.decrypt(dto.getIdentification()));
			}

		} catch (Exception e) {
			throw new IllegalArgumentException("Error decrypting user fields", e);
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

		if (username == null) {
			throw new IllegalArgumentException("Username cannot be null");
		}

		String encryptedUsername = AESUtil.encrypt(username);

		Optional<User> found = userRepo.findByUserName(encryptedUsername);
		if (found.isPresent()) {
			throw new IllegalArgumentException("Username already exist");
		}

		return found.isPresent();
	}

	/**
	 * Verifica si un correo electrónico ya está registrado (busca el valor
	 * encriptado).
	 */
	public boolean findEmailAlreadyTaken(String email) {
		if (email == null) {
			throw new IllegalArgumentException("Email cannot be null");
		}

		String encryptedEmail = AESUtil.encrypt(email);
		Optional<User> found = userRepo.findByEmail(encryptedEmail);
		if (found.isPresent()) {
			throw new IllegalArgumentException("Email already exist");
		}

		return found.isPresent();
	}

	/**
	 * Valida las credenciales de un usuario.
	 */
	public int validateCredentials(String username, String password) {
		Optional<User> userOptional = userRepo.findByUserName(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (passwordEncoder.matches(password, user.getPassword())) {
				return 0; 
			}
		}
		return 1;
	}



	@Override
	public int createWithRol(UserDTO data, String rol) {
		try {
			data.setRole(Role.valueOf(rol));
			createU(data);
			return 0;
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			return 1;
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


	public String decryptUser(String user) {
		if (user != null) {
			user = AESUtil.decrypt(user);

		}
		return user;
	}


	public String encryptUser(String user) {
		if (user != null) {
			user = AESUtil.encrypt(user);

		}
		return user;
	}

	@Override
	public int create(UserDTO data) {
		return 0;
	}


	public User encrypt(UserDTO data) {

		return null;
	}
}
