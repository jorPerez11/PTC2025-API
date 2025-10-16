package H2C_Group.H2C_API.Utils;

import H2C_Group.H2C_API.Entities.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    private static final String AUTH_COOKIE_NAME = "authToken";
    private final JwUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtRequestFilter(JwUtil jwUtil , UserDetailsService userDetailsService){
        this.jwtUtil = jwUtil; this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (isPublicEndpoint(request)){
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = extractTokenFromCookies(request);

            if (token ==null || token.isBlank() ){
                sendError(response, "Token no encontrado", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (!jwtUtil.validateToken(token)){
                sendError(response, "Token invalido o expirado", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

                //Obtiene el nombre de usuario y los claims
                String userName = jwtUtil.getUsernameFromToken(token);
                Claims claims = jwtUtil.parseToken(token);

                if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null){
                   //Extraer el rol del token

                    UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

                    String rol = jwtUtil.extractRol(token);

                   String authority = (rol != null && !rol.startsWith("ROLE_")) ? "ROLE_" + rol: rol;

                   Collection<? extends GrantedAuthority> authorities = authority != null ? Collections.singletonList(new SimpleGrantedAuthority(authority)): userDetails.getAuthorities();

                   UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                           userDetails,
                           null,
                           authorities
                   );
                   authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                   log.debug("Usuario autenticado:{} con los roles:{} " , userName, authorities);
                }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.error("DEBUG ROLES: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            }

            chain.doFilter(request, response);

        }catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            sendError(response, "Token expirado", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (MalformedJwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            sendError(response, "Token inválido", HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            log.error("Error de autenticación", e);
            sendError(response, "Error de autenticación", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null){
            return null;
        }

        for (Cookie cookie: cookies){
            if (AUTH_COOKIE_NAME.equals(cookie.getName())){
                try {
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                } catch (Exception e){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return (path.equals("/api/users/login") && "POST".equals(method)) ||
                (path.equals("/api/users/register") && "POST".equals(method)) ||
                (path.equals("/api/users/registerTech") && "POST".equals(method)) ||
                (path.equals("/api/request") && "POST".equals(method)) ||
                (path.equals("/api/verify") && "POST".equals(method)) ||
                (path.equals("/api/confirm") && "POST".equals(method)) ||
                (path.startsWith("/api/firstuse/")) ||
                (path.startsWith("/api/PostCompany/")) ||
                (path.equals("/api/companies") && "POST".equals(method)) ||
                (method.equals("OPTIONS"));
    }


    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(String.format("{\"error\": \"%s\", \"status\": %d}", message, status));
    }

}
