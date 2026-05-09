package co.edu.unbosque.mundialhub.configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.User.Role;
import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.model.GroupName;
import co.edu.unbosque.mundialhub.model.Rarity;
import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.service.ExternalHTTPRequestHandler;
import co.edu.unbosque.mundialhub.repository.StickerRepository;
import co.edu.unbosque.mundialhub.repository.TeamRepository;
import co.edu.unbosque.mundialhub.util.AESUtil;

@Service
public class DatabaseInitializer {
	@Value("${api-football}")
	private String apiKey;

	private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final TeamRepository teamRepo;
	private final StickerRepository stickerRepo;
	private final ModelMapper modelMapper;

	public DatabaseInitializer(UserRepository userRepo, PasswordEncoder passwordEncoder, TeamRepository teamRepo,
			StickerRepository stickerRepo, ModelMapper modelMapper) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.teamRepo = teamRepo;
		this.stickerRepo = stickerRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public void runDataInitialization() {
		log.info("Iniciando la carga de datos de configuración...");

		createDefaultUser("admin", Role.ADMIN);
		createDefaultUser("normaluser", Role.USER);
		createDefaultUser("temporalUser", Role.TEMPORAL);
		createDefaultUser("supportUser", Role.SUPPORT);
		createDefaultUser("operatorUser", Role.OPERATOR);

		createPlayers();

		loadTeams();

		log.info("Proceso de inicialización finalizado con éxito.");
	}

	private void createPlayers() {
		if (stickerRepo.count() == 513) {
			System.out.println("Los jugadores ya están precargados en la base de datos. Hay " + stickerRepo.count() + " Jugadores");
			return;
		}

		List<Integer> teams = Arrays.asList(12, 31, 1569, 15, 13, 5529, 9, 27, 8, 26, 25, 16, 2384, 6, 2, 10, 7, 3, 1, 22);
		String url = "https://v3.football.api-sports.io/players/squads?team=";

		for (Integer teamId : teams) {
			String fullUrl = url + teamId;
			long team = 0;

			switch (teamId) {
				case 9:    team = 29; break;
				case 27:   team = 41; break;
				case 8:    team = 44; break;
				case 26:   team = 37; break;
				case 25:   team = 17; break;
				case 16:   team = 1;  break;
				case 2384: team = 13; break;
				case 6:    team = 9;  break;
				case 2:    team = 33; break;
				case 10:   team = 45; break;
				case 7:    team = 32; break;
				case 3:    team = 46; break;
				case 1:    team = 25; break;
				case 22:   team = 27; break;
				case 12:   team = 22; break;
				case 31:   team = 10; break;
				case 1569: team = 7;  break;
				case 15:   team = 8;  break;
				case 13:   team = 34; break;
				case 5529: team = 5;  break;
				default:   team = 0;  break;
			}

			System.out.println("Consultando equipo ID: " + teamId + " -> URL: " + fullUrl);

			boolean success = false;
			int retries = 0;
			int maxRetries = 3; // Intentará hasta 3 veces por equipo antes de rendirse

			while (!success && retries < maxRetries) {
				try {
					String responseBody = ExternalHTTPRequestHandler.doGetWithAuthTokenApiFootball(fullUrl, apiKey);

					if (responseBody != null && !responseBody.contains("requests limit")) {
						
						JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
						JsonArray responseArray = jsonObject.getAsJsonArray("response");

						for (JsonElement element : responseArray) {
							JsonObject item = element.getAsJsonObject();
							JsonArray playersArray = item.getAsJsonArray("players");

							int countGoalkeeper = 0;
							int countDefender = 0;
							int countMidfielder = 0;
							int countAttacker = 0;

							for (JsonElement playerEl : playersArray) {
								JsonObject playerObj = playerEl.getAsJsonObject();
								String position = playerObj.get("position").getAsString();

								if ("Goalkeeper".equals(position)) {
									if (countGoalkeeper >= 3) continue;
									countGoalkeeper++;
								} else if ("Defender".equals(position)) {
									if (countDefender >= 8) continue;
									countDefender++;
								} else if ("Midfielder".equals(position)) {
									if (countMidfielder >= 9) continue;
									countMidfielder++;
								} else if ("Attacker".equals(position)) {
									if (countAttacker >= 6) continue;
									countAttacker++;
								} else {
									continue;
								}

								String playerName = playerObj.get("name").getAsString();
								String imgUrl = playerObj.get("photo").getAsString();

								System.out.println("Jugador guardado: " + playerName + " - " + position);

								Sticker sticker = new Sticker();
								sticker.setId(null);
								sticker.setImageUrl(imgUrl);
								sticker.setPlayerName(playerName);
								sticker.setPosition(position);

								Team teamf = teamRepo.findById(team).orElse(null);
								sticker.setTeam(teamf);

								if ("Goalkeeper".equals(position)) {
									sticker.setRarity(Rarity.RARE);
									sticker.setCost(30);
								} else if ("Defender".equals(position)) {
									sticker.setRarity(Rarity.COMMON);
									sticker.setCost(10);
								} else if ("Midfielder".equals(position)) {
									sticker.setRarity(Rarity.EPIC);
									sticker.setCost(40);
								} else if ("Attacker".equals(position)) {
									sticker.setRarity(Rarity.LEGENDARY);
									sticker.setCost(50);
								}

								sticker.setCreatedAt(LocalDateTime.now());
								stickerRepo.save(sticker);
							}
						}
						
						success = true; 

					} else {
						System.out.println("⚠️ Límite de peticiones de la API alcanzado. Esperando 60 segundos...");
						Thread.sleep(60000); 
						retries++;
					}

				} catch (Exception e) {
					if (e.getMessage() != null && e.getMessage().contains("429")) {
						System.out.println("⚠️ Error HTTP 429 detectado. Esperando 60 segundos antes de reintentar...");
						try {
							Thread.sleep(60000);
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
						}
						retries++;
					} else {
						System.out.println("❌ Ocurrió un error inesperado consultando el equipo " + teamId + ": " + e.getMessage());
						break; 
					}
				}
			}
		}
	}
	private void createDefaultUser(String username, Role role) {
		String encryptedUsername = AESUtil.encrypt(username);
		Optional<User> found = userRepo.findByUserName(encryptedUsername);

		if (found.isPresent()) {
			log.info("El usuario '{}' ya existe, omitiendo creación.", username);
		} else {
			User newUser = new User(null, encryptedUsername, null, null, passwordEncoder.encode("1234567890"), null,
					null, null, null, null, null, null, null, null, null, null, null, true, true, true, true);
			newUser.setRole(role);
			newUser.setEnabled(true);
			newUser.setAccountNonExpired(true);
			newUser.setAccountNonLocked(true);
			newUser.setCredentialsNonExpired(true);
			userRepo.save(newUser);
			log.info("Precargando usuario: {}", username);
		}
	}

	// ====================================================================
	// SECCIÓN DE INICIALIZACIÓN DE EQUIPOS (DEFINITIVA)
	// ====================================================================

	private void loadTeams() {
		if (teamRepo.count() > 0) {
			log.info("Los equipos ya están precargados en la base de datos. Omitiendo...");
			return;
		}

		log.info("Iniciando la carga de los 48 equipos");

		// GRUPO A: México, Sudáfrica, Corea del Sur, Chequia
		createTeam("México", "MEX", "https://crests.football-data.org/769.svg", "México", GroupName.A, 15, 1681);
		createTeam("Sudáfrica", "RSA", "https://crests.football-data.org/9396.svg", "Sudáfrica", GroupName.A, 60, 1429);
		createTeam("Corea del Sur", "KOR", "https://crests.football-data.org/772.png", "Corea del Sur", GroupName.A, 25,
				1588);
		createTeam("Chequia", "CZE", "https://crests.football-data.org/798.svg", "Chequia", GroupName.A, 41, 1501);

		// GRUPO B: Canadá, Bosnia y Herzegovina, Catar, Suiza
		createTeam("Canadá", "CAN", "https://crests.football-data.org/canada.svg", "Canadá", GroupName.B, 30, 1556);
		createTeam("Bosnia y Herzegovina", "BIH", "https://crests.football-data.org/bosnia.svg", "Bosnia y Herzegovina",
				GroupName.B, 65, 1385);
		createTeam("Catar", "QAT", "https://crests.football-data.org/8030.svg", "Catar", GroupName.B, 55, 1454);
		createTeam("Suiza", "SUI", "https://crests.football-data.org/788.svg", "Suiza", GroupName.B, 19, 1649);

		// GRUPO C: Brasil, Marruecos, Haití, Escocia
		createTeam("Brasil", "BRA", "https://crests.football-data.org/764.svg", "Brasil", GroupName.C, 6, 1761);
		createTeam("Marruecos", "MAR", "https://crests.football-data.org/morocco.svg", "Marruecos", GroupName.C, 8,
				1755);
		createTeam("Haití", "HAI", "https://crests.football-data.org/haiti.svg", "Haití", GroupName.C, 83, 1291);
		createTeam("Escocia", "SCO", "https://crests.football-data.org/814.svg", "Escocia", GroupName.C, 43, 1498);

		// GRUPO D: Estados Unidos, Paraguay, Australia, Turquía
		createTeam("Estados Unidos", "USA", "https://crests.football-data.org/usa.svg", "Estados Unidos", GroupName.D,
				16, 1673);
		createTeam("Paraguay", "PAR", "https://crests.football-data.org/761.svg", "Paraguay", GroupName.D, 40, 1503);
		createTeam("Australia", "AUS", "https://crests.football-data.org/779.svg", "Australia", GroupName.D, 27, 1580);
		createTeam("Turquía", "TUR", "https://crests.football-data.org/803.svg", "Turquía", GroupName.D, 22, 1599);

		// GRUPO E: Alemania, Curazao, Costa de Marfil, Ecuador
		createTeam("Alemania", "GER", "https://crests.football-data.org/759.svg", "Alemania", GroupName.E, 10, 1730);
		createTeam("Curazao", "CUW", "https://crests.football-data.org/curacao.svg", "Curazao", GroupName.E, 82, 1294);
		createTeam("Costa de Marfil", "CIV", "https://crests.football-data.org/787.svg", "Costa de Marfil", GroupName.E,
				34, 1532);
		createTeam("Ecuador", "ECU", "https://crests.football-data.org/791.svg", "Ecuador", GroupName.E, 23, 1594);

		// GRUPO F: Países Bajos, Japón, Suecia, Túnez
		createTeam("Países Bajos", "NED", "https://crests.football-data.org/8601.svg", "Países Bajos", GroupName.F, 7,
				1757);
		createTeam("Japón", "JPN", "https://crests.football-data.org/766.svg", "Japón", GroupName.F, 18, 1660);
		createTeam("Suecia", "SWE", "https://crests.football-data.org/792.svg", "Suecia", GroupName.F, 38, 1514);
		createTeam("Túnez", "TUN", "https://crests.football-data.org/tunisia.svg", "Túnez", GroupName.F, 44, 1483);

		// GRUPO G: Bélgica, Egipto, Irán, Nueva Zelanda
		createTeam("Bélgica", "BEL", "https://crests.football-data.org/805.svg", "Bélgica", GroupName.G, 9, 1734);
		createTeam("Egipto", "EGY", "https://crests.football-data.org/825.svg", "Egipto", GroupName.G, 29, 1563);
		createTeam("Irán", "IRN", "https://crests.football-data.org/iran.svg", "Irán", GroupName.G, 21, 1615);
		createTeam("Nueva Zelanda", "NZL", "https://crests.football-data.org/783.svg", "Nueva Zelanda", GroupName.G, 85,
				1281);

		// GRUPO H: España, Cabo Verde, Arabia Saudita, Uruguay
		createTeam("España", "ESP", "https://crests.football-data.org/760.svg", "España", GroupName.H, 2, 1876);
		createTeam("Cabo Verde", "CPV", "https://crests.football-data.org/cape_verde.svg", "Cabo Verde", GroupName.H,
				69, 1366);
		createTeam("Arabia Saudita", "KSA", "https://crests.football-data.org/saudi_arabia.svg", "Arabia Saudita",
				GroupName.H, 61, 1421);
		createTeam("Uruguay", "URU", "https://crests.football-data.org/758.svg", "Uruguay", GroupName.H, 17, 1673);

		// GRUPO I: Francia, Senegal, Irak, Noruega
		createTeam("Francia", "FRA", "https://crests.football-data.org/773.svg", "Francia", GroupName.I, 1, 1877);
		createTeam("Senegal", "SEN", "https://crests.football-data.org/senegal.svg", "Senegal", GroupName.I, 14, 1688);
		createTeam("Irak", "IRQ", "https://crests.football-data.org/iraq.svg", "Irak", GroupName.I, 57, 1447);
		createTeam("Noruega", "NOR", "https://crests.football-data.org/813.svg", "Noruega", GroupName.I, 31, 1550);

		// GRUPO J: Argentina, Argelia, Austria, Jordania
		createTeam("Argentina", "ARG", "https://crests.football-data.org/762.png", "Argentina", GroupName.J, 3, 1874);
		createTeam("Argelia", "ALG", "https://crests.football-data.org/algeria.svg", "Argelia", GroupName.J, 28, 1564);
		createTeam("Austria", "AUT", "https://crests.football-data.org/816.svg", "Austria", GroupName.J, 24, 1593);
		createTeam("Jordania", "JOR", "https://crests.football-data.org/8049.png", "Jordania", GroupName.J, 63, 1391);

		// GRUPO K: Portugal, RD Congo, Uzbekistán, Colombia
		createTeam("Portugal", "POR", "https://crests.football-data.org/765.svg", "Portugal", GroupName.K, 5, 1763);
		createTeam("RD Congo", "COD", "https://crests.football-data.org/congo_dr.svg", "RD Congo", GroupName.K, 46,
				1478);
		createTeam("Uzbekistán", "UZB", "https://crests.football-data.org/8070.png", "Uzbekistán", GroupName.K, 50,
				1465);
		createTeam("Colombia", "COL", "https://crests.football-data.org/818.svg", "Colombia", GroupName.K, 13, 1693);

		// GRUPO L: Inglaterra, Croacia, Ghana, Panamá
		createTeam("Inglaterra", "ENG", "https://crests.football-data.org/770.svg", "Inglaterra", GroupName.L, 4, 1825);
		createTeam("Croacia", "CRO", "https://crests.football-data.org/799.svg", "Croacia", GroupName.L, 11, 1717);
		createTeam("Ghana", "GHA", "https://crests.football-data.org/ghana.svg", "Ghana", GroupName.L, 74, 1346);
		createTeam("Panamá", "PAN", "https://crests.football-data.org/panama.svg", "Panamá", GroupName.L, 33, 1540);

		log.info("48 equipos insertados exitosamente");
	}

	private void createTeam(String name, String shortName, String crest, String country, GroupName groupName,
			Integer ranking, Integer points) {
		Team newTeam = new Team();
		newTeam.setName(name);
		newTeam.setShortName(shortName);
		newTeam.setCrest(crest);
		newTeam.setCountry(country);
		newTeam.setGroupName(groupName);
		newTeam.setFifaRanking(ranking);
		newTeam.setPoints(points);
		newTeam.setCreatedAt(LocalDateTime.now());
		teamRepo.save(newTeam);
	}

	public ModelMapper getModelMapper() {
		return modelMapper;
	}
}