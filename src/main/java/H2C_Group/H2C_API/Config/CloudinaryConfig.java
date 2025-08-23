package H2C_Group.H2C_API.Config;

import com.cloudinary.Cloudinary;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    //Variables para almacenar las credenciales de Cloudinary

    private String cloudName;
    private String apiKey;
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        //Crear un objeto de tipo Dotenv
        Dotenv dotenv = Dotenv.load();

        //Crear un Map para guardar la clave valor del archivo .env
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", dotenv.get("CLOUDINARY_CLOUD_NAME"));
        config.put("api_key", dotenv.get("CLOUDINARY_API_KEY"));
        config.put("api_secret", dotenv.get("CLOUDINARY_API_SECRET"));

        //Retorna una nueva instancia de Cloudinary con la configuración cargada
        return new Cloudinary(config);
    }
}