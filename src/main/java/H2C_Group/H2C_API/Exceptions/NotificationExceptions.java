package H2C_Group.H2C_API.Exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class NotificationExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND) //Excepcion de respuesta para usuarios no encontrados
    public static class NotificationNotFoundException extends RuntimeException {

        public NotificationNotFoundException(String message) {
            super(message);
        }

        public NotificationNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
