package H2C_Group.H2C_API.Services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {

        // EL BLOQUE TRY-CATCH DEBE ENVOLVER TODA LA LÓGICA DEL MESSAGEHELPER
        try {
            // 1. Crear el MimeMessage
            MimeMessage message = mailSender.createMimeMessage();

            // 2. Usar MimeMessageHelper (el 'true' habilita multiparte/HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 3. Configurar el correo (setFrom, setTo, setSubject, setText)
            helper.setFrom("fernadomiguelvelasquezperez@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // El segundo 'true' para indicar que 'body' es HTML

            // 4. Enviar el mensaje
            mailSender.send(message);

        } catch (jakarta.mail.MessagingException e) {
            // 5. Manejo del error
            System.err.println("Error al configurar o enviar correo: " + e.getMessage());
            // Si quieres que el servicio falle y lance una excepción de tiempo de ejecución:
            throw new RuntimeException("Fallo al enviar el correo electrónico.", e);
        }
    }
}
