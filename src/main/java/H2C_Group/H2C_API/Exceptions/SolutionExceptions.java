package H2C_Group.H2C_API.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class SolutionExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND) //Excepcion de respuesta para soluciones no encontrados
    public static class SolutionNotFoundException extends RuntimeException {

        public SolutionNotFoundException(String message) {
            super(message);
        }

        public SolutionNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
