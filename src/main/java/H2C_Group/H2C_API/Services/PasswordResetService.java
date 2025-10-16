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

        String subject = "Recuperación de contraseña - Help Desk H2C";
        String body = "Hola " + user.getFullName() + ",\n\nTu código de recuperación es:\n\n" +
                token + "\n\nEste código expirará en 15 minutos.";

        emailService.sendEmail(user.getEmail(), subject, body);
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


