package H2C_Group.H2C_API.Exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class SurveyExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND) //Excepcion de respuesta para usuarios no encontrados
    public static class SurveyNotFoundException extends RuntimeException {

        public SurveyNotFoundException(String message) {
            super(message);
        }

        public SurveyNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
