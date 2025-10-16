package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.PasswordResetToken;
import H2C_Group.H2C_API.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUser_Email(String token, String email);
    void deleteByUser(UserEntity user);
}


