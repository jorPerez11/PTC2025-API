package H2C_Group.H2C_API.Models.DTO;

import java.io.Serializable;

public class JwtResponse implements Serializable {

    private final String token;
    private final String username;
    private final String rolId;
    private final boolean passwordExpired;
    private Long userId;

    public JwtResponse(String token, String username, String rolId, boolean passwordExpired, Long userId) {
        this.token = token;
        this.username = username;
        this.rolId = rolId;
        this.passwordExpired = passwordExpired;
        this.userId = userId;
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

    public Long getUserId(){
        return userId;
    }

    public void setUserId(){
        this.userId = userId;
    }
}
