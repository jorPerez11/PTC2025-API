package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
@CrossOrigin
public class CloudinaryController {
    @Autowired
    private final CloudinaryService cloudinaryService;

    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("image")MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Map.of(
                    "message", "Imagen subida",
                    "url", imageUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir la imagen");
        }
    }

    @PostMapping("/upload-to-folder")
    public ResponseEntity<?> uploadImageToFolder(
            @RequestParam("image") MultipartFile file,
            @RequestParam String folder
    ) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(Map.of(
                    "message", "Imagen subida exitosamente",
                    "url", imageUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir la imagen");
        }
    }
}