package H2C_Group.H2C_API.Models.DTO;

import java.io.Serializable;

public class JwtResponse implements Serializable {

    private final String token;
    private final String username;
    private final String rolId;
    private final boolean passwordExpired;

    public JwtResponse(String token, String username, String rolId, boolean passwordExpired) {
        this.token = token;
        this.username = username;
        this.rolId = rolId;
        this.passwordExpired = passwordExpired;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public String getRolId() {
        return rolId;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
