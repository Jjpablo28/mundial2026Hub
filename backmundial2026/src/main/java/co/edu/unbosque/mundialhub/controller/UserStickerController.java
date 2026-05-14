package co.edu.unbosque.mundialhub.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.mundialhub.dto.UserStickerDTO;
import co.edu.unbosque.mundialhub.service.UserStickerService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user-stickers")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082" })
@Tag(name = "user-stickers", description = "Gestión del inventario de láminas de usuarios")
public class UserStickerController {

    @Autowired
    private UserStickerService userStickerServ;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserStickerDTO dto) {
        try {
            userStickerServ.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Lámina añadida al inventario", "success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userStickerServ.findByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserStickerDTO dto) {
        try {
            userStickerServ.updateById(id, dto);
            return ResponseEntity.ok(Map.of("message", "Cantidad actualizada", "success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            userStickerServ.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Lámina removida del inventario", "success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }
    
    @PostMapping("/open-pack/{id}")
    public ResponseEntity<?> createPack(@PathVariable Long id) {
        try {
            userStickerServ.openPack(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Lámina añadida al inventario", "success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
        }
    }
    @PostMapping("/buy-pack/{id}")
    public ResponseEntity<?> buyPack(@PathVariable Long id) {
    	try {
    		userStickerServ.buyPack(id);
    		return ResponseEntity.status(HttpStatus.CREATED)
    				.body(Map.of("message", "Lámina añadida al inventario", "success", true));
    	} catch (Exception e) {
    		return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
    	}
    }

}