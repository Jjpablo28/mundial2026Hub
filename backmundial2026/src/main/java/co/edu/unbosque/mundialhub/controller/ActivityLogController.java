package co.edu.unbosque.mundialhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.mundialhub.dto.UserActivityLogDTO;
import co.edu.unbosque.mundialhub.service.ActivityLogService;

@RestController
@RequestMapping("/activity-logs")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082/register",
"https://mundial2026hub.netlify.app" })
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserActivityLogDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(activityLogService.getLogsByUser(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserActivityLogDTO>> getAll() {
        return ResponseEntity.ok(activityLogService.getAllLogs());
    }


    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<UserActivityLogDTO>> getByUserPaged(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(activityLogService.getLogsByUserPaged(userId, page, size));
    }


    @GetMapping("/all/paged")
    public ResponseEntity<Page<UserActivityLogDTO>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(activityLogService.getAllLogsPaged(page, size));
    }
}