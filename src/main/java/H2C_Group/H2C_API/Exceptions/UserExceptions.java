package H2C_Group.H2C_API.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UserExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND) //Excepcion de respuesta para usuarios no encontrados
    public static class UserNotFoundException extends RuntimeException {

        public UserNotFoundException(String message) {
            super(message);
        }

        public UserNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
