package groupId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Profile("E2Etest") 
public class TestController {
    
    @Autowired
    private ImageController imageController;

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetDatabase() {
        try {
            imageController.resetDatabase();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 