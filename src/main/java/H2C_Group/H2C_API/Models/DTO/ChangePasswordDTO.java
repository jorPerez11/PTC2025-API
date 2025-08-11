package H2C_Group.H2C_API.Models.DTO;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String username;
    private String currentPassword;
    private String newPassword;
}
