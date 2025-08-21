package H2C_Group.H2C_API.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    //Constante que define el tamaño máximo permitido para los archivos (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    //Constante para definir los tipos de archivo admitidos
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png"};
    //Cliente de Cloudinary inyectado como dependencia
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     *
     * @param file
     * @return String URL de la imagen
     * @throws IOException
     */
    public String uploadImage(MultipartFile file) throws IOException {
        //1. Validamos el archivo
        validateImage(file);

        //Sube el archivo a Cloudinary con configuraciones básicas
        //Configura el tipo de recurso auto-detectado
        //Calidad automática con niver "good"
        Map<?, ?> uploadResult = cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap(
                        "resource_type", "auto",
                        "quality", "auto:good"
                ));

        //Retorna la URL segura de la imagen
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Sube una imagen a una carpeta en específico
     * @param file
     * @param folder carpeta destino
     * @return URL segura (HTTPS) de la imagen subida
     * @throws IOException Si ocurre un error durante la subida
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateImage(file);

        //Generar un nombre único para el archivo
        //Conservar la extensión original
        //Agregar un prefijo y un UUID para evitar colisiones

        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = "img_" + UUID.randomUUID() + fileExtension;

        //Configuración para subir imagen
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder, //Carpeta de destino
                "public_id", uniqueFileName, //Nombre único para el archivo
                "use_filename", false, //No usar el nombre original
                "unique_filename", false, //No generar nombre único (proceso hecho anteriormente)
                "overwrite", false, //No sobreescribir archivos
                "resource_type", "auto", //Auto-detectar tipo de recurso
                "quality", "auto:good" //Optimización de calidad automática
        );

        //Subir el archivo
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        //Retornamos la URL segura
        return (String) uploadResult.get("secure_url");
    }

    /**
     *
     * @param file
     */
    private void validateImage(MultipartFile file) {
        //1. Verificar si el archivo está vacío
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }

        //2. Verificar el tamaño de la imagen
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo no puede ser mayor a 5MB");
        }

        //3. Obtener y validar el nombre original del archivo
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("Nombre del archivo inválido");
        }

        //4. Extraer y validar la extensión
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)) {
            throw new IllegalArgumentException("Solo se permiten archivos JPG, JPEG Y PNG");
        }

        //5. Verificar que el tipo de MIME sea una imagen
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen válida");
        }
    }
}