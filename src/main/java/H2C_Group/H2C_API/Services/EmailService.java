package H2C_Group.H2C_API.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("fernadomiguelvelasquezperez@gmail.com"); //Correo emisor
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);
    }
}
