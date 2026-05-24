package co.edu.unbosque.mundialhub.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.mundialhub.dto.UserStickerDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.UserSticker;
import co.edu.unbosque.mundialhub.repository.StickerRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.repository.UserStickerRepository;

/**
 * Servicio encargado de gestionar los álbumes e inventarios individuales de los usuarios,
 * controlando la compra de sobres, apertura de paquetes con algoritmos de probabilidad
 * y el intercambio de láminas/cromos repetidos entre coleccionistas.
 * * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
@Service
public class UserStickerService implements CRUDOperation<UserStickerDTO, UserSticker> {

	private static final SecureRandom secureRandom = new SecureRandom();

	@Autowired
	private StickerRepository stickerRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserStickerRepository userStickerRepo;

	@Autowired
	private ModelMapper modelMapper;

	/**
	 * Crea un registro o asignación directa de un cromo a un usuario.
	 *
	 * @param dto DTO con los datos de asignación del cromo.
	 * @return El DTO con el registro creado y su timestamp.
	 */
	@Override
	@Transactional
	public UserStickerDTO create(UserStickerDTO dto) {
		UserSticker entity = modelMapper.map(dto, UserSticker.class);
		entity.setId(null);
		entity.setObtainedAt(LocalDateTime.now());

		if (entity.getQuantity() == null) {
			entity.setQuantity(1);
		}

		userStickerRepo.save(entity);
		return modelMapper.map(entity, UserStickerDTO.class);
	}

	/**
	 * Recupera la colección completa de láminas (inventario) que le pertenecen a un usuario.
	 *
	 * @param userId Identificador único del usuario coleccionista.
	 * @return Lista de DTOs con los stickers del usuario y sus respectivas cantidades.
	 */
	@Transactional(readOnly = true)
	public List<UserStickerDTO> findByUser(Long userId) {
		return userStickerRepo.findByUserId(userId).stream()
				.map(entity -> modelMapper.map(entity, UserStickerDTO.class))
				.collect(Collectors.toList());
	}

	/**
	 * Actualiza la cantidad de copias que posee un usuario sobre una lámina determinada.
	 *
	 * @param id  ID único de la relación intermedia UserSticker.
	 * @param dto DTO con la nueva cantidad a asignar.
	 * @return El DTO con las modificaciones aplicadas.
	 * @throws Exceptions.ResourceNotFoundException Si la relación intermedia no existe en la base de datos.
	 */
	@Override
	@Transactional
	public UserStickerDTO updateById(Long id, UserStickerDTO dto) {
		UserSticker existing = userStickerRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("Relación Usuario-Sticker no encontrada con ID: " + id));

		if (dto.getQuantity() != null) {
			existing.setQuantity(dto.getQuantity());
		}

		userStickerRepo.save(existing);
		return modelMapper.map(existing, UserStickerDTO.class);
	}

	/**
	 * Elimina la asignación o pertenencia de un cromo del inventario de un usuario.
	 *
	 * @param id ID de la relación a eliminar.
	 * @throws Exceptions.ResourceNotFoundException Si no existe el registro indicado.
	 */
	@Override
	@Transactional
	public void deleteById(Long id) {
		UserSticker entity = userStickerRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("No se encontró el registro de asignación para eliminar con ID: " + id));
		userStickerRepo.delete(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserStickerDTO> findAll() {
		// No se requiere listado global sin segmentar por usuario
		return new ArrayList<>();
	}

	@Override
	@Transactional(readOnly = true)
	public long count() {
		return userStickerRepo.count();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean exist(Long id) {
		return userStickerRepo.existsById(id);
	}

	/**
	 * Ejecuta el desempaque de un sobre virtual de cromos. Consume un sobre del inventario
	 * del usuario y genera 7 láminas aleatorias usando un algoritmo de probabilidad
	 * basado en los puntos del usuario, actualizando su inventario personal.
	 *
	 * @param userId Identificador del usuario que abre el sobre.
	 * @return Lista de los 7 DTOs de las láminas obtenidas en el sobre de regalo.
	 * @throws Exceptions.ResourceNotFoundException Si el usuario no existe en el sistema.
	 * @throws Exceptions.ValidationException       Si no hay cromos cargados en el sistema o si el usuario no tiene sobres.
	 */
	@Transactional
	public List<UserStickerDTO> openPack(Long userId) {
	    User user = userRepo.findById(userId)
	            .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

	    List<Sticker> allStickers = stickerRepo.findAll();
	    if (allStickers.isEmpty()) {
	        throw new Exceptions.ValidationException("No hay stickers configurados en el sistema global.");
	    }

	    if (user.getPackets() <= 0) {
	        throw new Exceptions.ValidationException("No tienes sobres disponibles para abrir.");
	    }

	    user.setPackets(user.getPackets() - 1);
	    userRepo.save(user);

	    int userCoins = user.getTotalPoints() != null ? user.getTotalPoints() : 0;
	    List<UserStickerDTO> packResult = new ArrayList<>();

	    for (int i = 0; i < 7; i++) {
	        int index;
	        if (userCoins > 1000 && secureRandom.nextDouble() > 0.5) {
	            index = secureRandom.nextInt(allStickers.size() / 2) + (allStickers.size() / 2);
	        } else {
	            index = secureRandom.nextInt(allStickers.size());
	        }

	        Sticker selectedSticker = allStickers.get(index);

	        UserSticker entity = userStickerRepo.findByUserIdAndStickerId(userId, selectedSticker.getId())
	                .map(existing -> {
	                    existing.setQuantity(existing.getQuantity() + 1);
	                    return existing;
	                })
	                .orElseGet(() -> {
	                    UserSticker newSticker = new UserSticker();
	                    newSticker.setUser(user);
	                    newSticker.setSticker(selectedSticker);
	                    newSticker.setTeam(selectedSticker.getTeam());
	                    newSticker.setQuantity(1);
	                    newSticker.setObtainedAt(LocalDateTime.now());
	                    return newSticker;
	                });

	        UserSticker savedEntity = userStickerRepo.save(entity);
	        packResult.add(modelMapper.map(savedEntity, UserStickerDTO.class));
	    }

	    return packResult;
	}

	/**
	 * Permite comprar un sobre de láminas en la tienda virtual. Deduce de manera fija
	 * 500 puntos/monedas del saldo del usuario y le suma un sobre a su inventario.
	 *
	 * @param userId ID único del usuario comprador.
	 * @throws Exceptions.ResourceNotFoundException Si el usuario no existe.
	 * @throws Exceptions.ValidationException       Si el balance de monedas es inferior a 500 puntos.
	 */
	@Transactional
	public void buyPack(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
		
		int tokensActuales = user.getTotalPoints() != null ? user.getTotalPoints() : 0;
		
		// Corrección de bug crítico: Antes permitía comprar con saldo menor a 500 si era diferente de cero
		if (tokensActuales < 500) {
			throw new Exceptions.ValidationException("No posees suficientes monedas. Cada sobre cuesta 500 tokens y tu saldo actual es de: " + tokensActuales);
		}

		user.setPackets(user.getPackets() + 1);
		user.setTotalPoints(tokensActuales - 500);

		userRepo.save(user);
	}

	/**
	 * Realiza un proceso transaccional seguro de intercambio (Trade) de láminas entre dos usuarios.
	 * Verifica la existencia de repetidos en el origen del cromo antes de cederlo al receptor.
	 *
	 * @param fromUserId ID del usuario que entrega y cede el cromo.
	 * @param toUserId   ID del usuario destino que recibe el cromo.
	 * @param stickerId  ID del cromo en cuestión objeto de la transacción.
	 * @return true si el proceso de intercambio se consolidó exitosamente.
	 * @throws Exceptions.ValidationException       Si intentan operar con el mismo usuario o no hay copias repetidas.
	 * @throws Exceptions.ResourceNotFoundException Si los usuarios o el sticker especificado no se encuentran registrados.
	 */
	@Transactional
	public boolean exchangeSticker(Long fromUserId, Long toUserId, Long stickerId) {
	    if (fromUserId.equals(toUserId)) {
	        throw new Exceptions.ValidationException("Operación inválida: No es posible realizar intercambios de láminas contigo mismo.");
	    }

	    UserSticker fromUserSticker = userStickerRepo.findByUserIdAndStickerId(fromUserId, stickerId)
	            .orElseThrow(() -> new Exceptions.ResourceNotFoundException("El usuario origen no posee esta lámina en su inventario."));

	    // Validación reglamentaria: Solo se puede cambiar si te queda por lo menos 1 de respaldo para tu álbum personal
	    if (fromUserSticker.getQuantity() <= 1) {
	        throw new Exceptions.ValidationException("No posees copias repetidas disponibles para intercambio de este cromo.");
	    }

	    fromUserSticker.setQuantity(fromUserSticker.getQuantity() - 1);
	    userStickerRepo.save(fromUserSticker);

	    Optional<UserSticker> toUserStickerOpt = userStickerRepo.findByUserIdAndStickerId(toUserId, stickerId);
	    UserSticker toUserSticker;

	    if (toUserStickerOpt.isPresent()) {
	        toUserSticker = toUserStickerOpt.get();
	        toUserSticker.setQuantity(toUserSticker.getQuantity() + 1);
	    } else {
	        User toUser = userRepo.findById(toUserId)
	                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario destino no encontrado con ID: " + toUserId));
	        Sticker sticker = stickerRepo.findById(stickerId)
	                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Sticker no encontrado con ID: " + stickerId));

	        toUserSticker = new UserSticker();
	        toUserSticker.setUser(toUser);
	        toUserSticker.setSticker(sticker);
	        toUserSticker.setQuantity(1);
	        toUserSticker.setObtainedAt(LocalDateTime.now());
	    }

	    userStickerRepo.save(toUserSticker);
	    return true;
	}
	@Transactional(readOnly = true)
	public List<UserStickerDTO> findByUserAndTeam(Long userId, Long teamId) {
	    return userStickerRepo.findByUserIdAndTeamId(userId,teamId).stream()
				.map(entity -> modelMapper.map(entity, UserStickerDTO.class))
				.collect(Collectors.toList());
	}

}