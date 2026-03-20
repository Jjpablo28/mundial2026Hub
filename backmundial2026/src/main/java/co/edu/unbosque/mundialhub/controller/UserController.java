package co.edu.unbosque.mundialhub.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.mundialhub.dto.UserDTO;
import co.edu.unbosque.mundialhub.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082" })
@Tag(name = "Users", description = "CRUD Users")
public class UserController {

	@Autowired
	private UserService userServ;

	@PutMapping(path = "/updatePasswordById")
	ResponseEntity<?> updateNewById(@RequestParam Long id, @RequestParam String newPassword) {

		UserDTO newUser = new UserDTO(null, null, null, null, null, null, newPassword, null, null, null, null, null);

		int status = userServ.updatePassword(id, newUser);

		if (status == 0) {

			return ResponseEntity.status(HttpStatus.ACCEPTED)
					.body(Map.of("message", "User registered successfully", "success", true));
		} else if (status == 2) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "User not found", "success", false));
		} else if (status == 4) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "Password not valid", "success", false));
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "Error on update", "success", false));
		}
	}
	
	@GetMapping("/count")
	ResponseEntity<Long> countAll() {
		Long count = userServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	@DeleteMapping("/deleteById/{id}")

	public ResponseEntity<String> deleteById(@PathVariable Long id) {

		int status = userServ.deleteById(id);
		if (status == 0) {
			return new ResponseEntity<>("Usuario eliminado con exito", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar el usuario", HttpStatus.NOT_FOUND);
		}
	}
	
	@DeleteMapping("/deletebyuser")
	ResponseEntity<String> deleteByUser(@RequestParam String name) {
		int status = userServ.deleteByUsername(name);
		if (status == 0) {
			return new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error on delete", HttpStatus.NOT_FOUND);
		}
	}
}
