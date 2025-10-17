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
import org.springframework.http.HttpMethod;
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
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    private static final String AUTH_COOKIE_NAME = "authToken";
    private final JwUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Lista de rutas públicas y métodos para una verificación más robusta
    private static final List<String> PUBLIC_PATHS_PREFIXES = Arrays.asList(
            "/api/firstuse/",
            "/api/PostCompany/",
            "/api/client/PostTicket", // Endpoint específico para permitir
            "/api/client/UpdateTicket/", // Asumiendo que necesita el /
            "/api/client/DeleteTicket",
            "/api/check-company-existence",
            "/api/users/request-password-reset",
            "/api/searchSolution",
            "/api/GetSolutions",
            "/api/GetSolutionsWeb/"
    );

    private static final List<String> PUBLIC_PATHS_EXACT = Arrays.asList(
            "/api/users/login",
            "/api/users/register",
            "/api/users/registerTech",
            "/api/PostCompany",
            "/api/users/request-password-reset",
            "/api/check-company-existence",
            "/api/companies"
    );


    @Autowired
    public JwtRequestFilter(JwUtil jwUtil , UserDetailsService userDetailsService){
        this.jwtUtil = jwUtil; this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Verificar si la ruta es pública
        if (isPublicEndpoint(request)){
            // Si es pública, saltamos toda la lógica de validación del token.
            // Spring Security se encargará de la autorización con permitAll().
            chain.doFilter(request, response);
            return;
        }

        // 2. Lógica de autenticación JWT (solo para rutas privadas)
        try {
            String token = extractTokenFromCookies(request);

            if (token ==null || token.isBlank() ){
                sendError(response, "Token no encontrado", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (!jwtUtil.validateToken(token)){
                // Si el token es inválido o expiró, esto se detecta aquí
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

        // 1. Manejo de OPTIONS (CORS pre-flight)
        if (method.equals("OPTIONS")) {
            return true;
        }

        // 2. Manejo de endpoints con prefijos (ej. /api/PostCompany/123)
        boolean isPrefixMatch = PUBLIC_PATHS_PREFIXES.stream()
                .anyMatch(path::startsWith);

        if (isPrefixMatch) {
            return true;
        }

        // 3. Manejo de endpoints exactos
        boolean isExactMatch = PUBLIC_PATHS_EXACT.stream()
                .anyMatch(publicPath -> publicPath.equalsIgnoreCase(path));

        if (isExactMatch) {
            return true;
        }

        // Manejo específico para peticiones que requieren POST y son exactas (aunque ya se cubren en 2 y 3)
        return (path.equals("/api/users/login") && "POST".equals(method)) ||
                (path.equals("/api/users/register") && "POST".equals(method)) ||
                (path.equals("/api/users/registerTech") && "POST".equals(method)) ||
                (path.equals("/api/PostCompany") && "POST".equals(method)) ||
                (path.equals("/api/request") && "POST".equals(method)) ||
                (path.equals("/api/verify") && "POST".equals(method)) ||
                (path.equals("/api/confirm") && "POST".equals(method)) ||
                (path.startsWith("/api/firstuse/")) ||
                (path.startsWith("/api/PostCompany/")) ||
                (path.equals("/api/companies") && "POST".equals(method));
    }


    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(String.format("{\"error\": \"%s\", \"status\": %d}", message, status));
    }

}