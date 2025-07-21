package H2C_Group.H2C_API.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TicketExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND) //Excepcion de respuesta para tickets no encontrados
    public static class TicketNotFoundException extends RuntimeException {

        public TicketNotFoundException(String message) {
            super(message);
        }

        public TicketNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
