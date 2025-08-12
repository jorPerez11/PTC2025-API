package H2C_Group.H2C_API.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwUtil {
    // Genera una clave secreta segura en tiempo de ejecución.
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long serialVersionUID = -2550185165626007488L;
    public static final long JWT_TOKEN_VALIDITY_SHORT = 5 * 60 * 60 * 1000; // 5 horas
    public static final long JWT_TOKEN_VALIDITY_LONG = 7 * 24 * 60 * 60 * 1000; // 7 días

    // Obtener el nombre de usuario de un token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Obtener la fecha de expiración de un token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Para obtener cualquier información del token necesitamos la clave secreta
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Verificar si el token ha expirado
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    // Generar el token con un tiempo de validez por defecto (sesión corta)
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, JWT_TOKEN_VALIDITY_SHORT);
    }

    // Nuevo metodo para generar el token con un tiempo de validez personalizado (sesión larga)
    public String generateToken(UserDetails userDetails, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), expirationTime);
    }

    // Creación del token
    private String doGenerateToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // Validar el token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
