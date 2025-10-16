package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.PasswordResetToken;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Repositories.PasswordResetTokenRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void requestReset(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) return; // No revelar si el correo existe

        UserEntity user = userOpt.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiration(expiry);

        tokenRepository.deleteByUser(user); // Elimina tokens anteriores
        tokenRepository.save(resetToken);

        // Variables para el correo
        String nombre = user.getFullName();
        String expirationTime = "15 minutos"; // O formatear 'expiry' si lo necesitas exacto

        String subject = "Código de Recuperación de Contraseña - Help Desk H2C";

        // Construir el cuerpo HTML (foco en el código)
        String bodyHTML = "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head>"
                + "    <meta charset='UTF-8'>"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "    <title>Recuperación de Contraseña</title>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f8f9fa;'>"

                + "    <table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f8f9fa; padding: 20px;'>"
                + "        <tr>"
                + "            <td align='center'>"
                + "                <table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width: 600px; background-color: #ffffff; border-radius: 10px; border: 1px solid #e9ecef; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>"
                + "                    "
                + "                    <tr>"
                + "                        <td align='center' style='padding: 20px 30px; background-color: #ffffff; border-top-left-radius: 10px; border-top-right-radius: 10px;'>"
                + "                            <img src='https://i.ibb.co/5Xxq0WTx/logoH2C.png' alt='Logo H2C Help Desk' width='160' style='display: block; border: 0;' />"
                + "                        </td>"
                + "                    </tr>"

                + "                    <tr>"
                + "                        <td height='5' style='background-color: #9e0918;'></td>" // Color de marca (Rojo/Vino)
                + "                    </tr>"

                + "                    <tr>"
                + "                        <td style='padding: 30px; color: #343a40; font-size: 16px; line-height: 1.7;'>"
                + "                            <h1 style='color: #9e0918; font-size: 24px; margin-top: 0; margin-bottom: 20px;'>Código de Recuperación de Contraseña</h1>"
                + "                            "
                + "                            <p>Hola <strong>" + nombre + "</strong>,</p>"
                + "                            <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta. Por favor, utiliza el siguiente código en la página de recuperación de tu sistema para continuar:</p>"
                + "                            "
                + "                            "
                + "                            <div style='background-color: #fff0f5; /* Rosa muy claro */ padding: 20px; border-left: 5px solid #9e0918; border-radius: 5px; margin: 30px 0;'>"
                + "                                <p style='margin: 0; font-size: 17px; text-align: center;'>"
                + "                                    <strong><span style='color: #9e0918;'>&#10148;</span> TU CÓDIGO DE RECUPERACIÓN:</strong> "
                + "                                    <br>"
                + "                                    <span style='color: #9e0918; font-weight: bold; font-size: 1.5em; letter-spacing: 1px; word-break: break-all; display: block; padding-top: 10px;'>" + token + "</span>"
                + "                                </p>"
                + "                            </div>"
                + "                            "
                + "                            <p><strong>IMPORTANTE:</strong> Este código es de un solo uso y expirará en " + expirationTime + ".</p>"
                + "                            <p>Si no solicitaste un cambio de contraseña, ignora este correo. Tu contraseña no será modificada.</p>"
                + "                            "
                + "                        </td>"
                + "                    </tr>"

                + "                    <tr>"
                + "                        <td align='center' style='padding: 20px 30px; border-top: 1px solid #e9ecef; background-color: #f8f9fa; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; font-size: 12px; color: #6c757d;'>"
                + "                            <p style='margin: 0;'>Este es un correo electrónico automatizado de H2C. Por favor, no respondas a este mensaje.</p>"
                + "                        </td>"
                + "                    </tr>"

                + "                </table>"
                + "            </td>"
                + "        </tr>"
                + "    </table>"
                + "</body>"
                + "</html>";

        emailService.sendEmail(user.getEmail(), subject, bodyHTML);
    }

    public boolean verifyToken(String email, String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUser_Email(token, email);
        return tokenOpt.isPresent() && tokenOpt.get().getExpiration().isAfter(LocalDateTime.now());
    }

    public void resetPassword(String email, String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUser_Email(token, email);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token inválido o expirado.");
        }

        UserEntity user = tokenOpt.get().getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordExpired(false);
        userRepository.save(user);

        tokenRepository.delete(tokenOpt.get());
    }
}


