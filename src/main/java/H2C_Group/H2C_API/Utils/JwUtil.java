package H2C_Group.H2C_API.Utils;

import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Repositories.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwUtil {

    @Value("${security.jwt.secret}")
    public String jwtSecreto;

    @Value("${jwt.token.validity.short}")
    public long JWT_TOKEN_VALIDITY_SHORT;

    @Value("${jwt.token.validity.long}")
    public long JWT_TOKEN_VALIDITY_LONG;

    @Autowired
    private UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger(JwUtil.class);

    private SecretKey getSingningKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecreto));
    }

    // Obtener el nombre de usuario de un token
    public String getUsernameFromToken(String token) {

        return getClaimFromToken(token, Claims::getSubject);
    }

    // Obtener la fecha de expiración de un token
    public Date getExpirationDateFromToken(String token) {

        return getClaimFromToken(token, Claims::getExpiration);
    }

    public Long getUserIdFromToken(String token){
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    public Boolean getIsPasswordExpiredFromToken(String token){
        return getClaimFromToken(token, claims -> claims.get("passwordExpired", Boolean.class));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Para obtener cualquier información del token necesitamos la clave secreta
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSingningKey()).build().parseClaimsJws(token).getBody();
    }

    // Verificar si el token ha expirado
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Claims parseToken(String token) throws ExpiredJwtException, MalformedJwtException{
        try {
            return getAllClaimsFromToken(token);
        } catch (ExpiredJwtException e){
            log.warn("Token expirado: ", e.getMessage());
            throw e;
        } catch (MalformedJwtException e){
            log.warn("Token malformado: ", e.getMessage());
            throw e;
        } catch (Exception e){
            log.warn("Token invalido", e.getMessage());
            throw  new MalformedJwtException("Token invalido");
        }
    }

    public  boolean validateToken(String token){
        try {
            parseToken(token); //Valida la firma el formato y la expiracion
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public String extractRol(String token){
        try {
            Claims claims = parseToken(token);
            Object rolClaims = claims.get("rol");
            return (rolClaims != null)? String.valueOf(rolClaims): null;
        } catch (Exception e){
            return null;
        }
    }


    // Generar el token con un tiempo de validez por defecto (sesión corta)
    public String generateToken(UserDetails userDetails) {

        return generateToken(userDetails, JWT_TOKEN_VALIDITY_SHORT);
    }

    // Nuevo metodo para generar el token con un tiempo de validez personalizado (sesión larga)
    public String generateToken(UserDetails userDetails, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();

        String rolId = userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> {
                    String authorityName = authority.getAuthority();
                    // Si el formato es "ROLE_X", extrae "X"
                    if (authorityName.startsWith("ROLE_")) {
                        return authorityName.substring("ROLE_".length());
                    }
                    return authorityName;
                })
                .orElse(null);

        UserEntity userEntity = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado:"+ userDetails.getUsername()));

        claims.put("rol", rolId);
        claims.put("username", userDetails.getUsername());
        claims.put("userId", userEntity.getUserId());
        claims.put("passwordExpired", userEntity.isPasswordExpired());

        return doGenerateToken(claims, userDetails.getUsername(), expirationTime);
    }

    // Creación del token
    private String doGenerateToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSingningKey(), SignatureAlgorithm.HS256).compact();

    }

}
