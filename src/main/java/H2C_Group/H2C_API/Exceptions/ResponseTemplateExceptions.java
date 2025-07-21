package H2C_Group.H2C_API.Exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ResponseTemplateExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND) //Excepcion de respuesta para soluciones no encontrados
    public static class ResponseTemplateNotFoundException extends RuntimeException {

        public ResponseTemplateNotFoundException(String message) {
            super(message);
        }

        public ResponseTemplateNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
