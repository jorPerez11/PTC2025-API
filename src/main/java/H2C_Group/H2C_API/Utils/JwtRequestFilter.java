package H2C_Group.H2C_API.Utils;

import H2C_Group.H2C_API.Entities.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        final String requestUri = request.getRequestURI();

        // Paso 1: Ignorar rutas públicas. Si la URI de la petición es para login o register, el filtro se salta la validación del token y deja pasar la petición al siguiente filtro en la cadena. Esto es crucial porque estas rutas  no tienen un token JWT
        if (requestUri.startsWith("/api/users/login") || requestUri.startsWith("/api/users/register")) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Paso 2: Extraer el token del encabezado 'Authorization'.
        // Comprueba si el header de autorización existe y tiene el formato 'Bearer TOKEN'
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                // Intenta extraer el nombre de usuario del token.
                username = jwtUtil.getUsernameFromToken(jwt);
            } catch (ExpiredJwtException e) {
                // 2. Manejo de la excepción de token expirado para evitar errores 500
                // Aunque no detiene el flujo aquí, el token inválido no pasará la validación más adelante
                System.out.println("JWT Token ha expirado.");

            }
        }

        // Paso 3: Validar el token y autenticar al usuario.
        // Se ejecuta solo si se encontró un nombre de usuario en el token y no hay un usuario autenticado en el contexto de seguridad actual
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Verifica si el token es válido (no expirado y con firma correcta)
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // Comprueba si el objeto userDetails es una instancia de tu UserEntity
                if(userDetails instanceof UserEntity){
                    UserEntity userEntity = (UserEntity) userDetails;
                    // Si la contraseña ha expirado Y la petición NO es para el endpoint de cambio de contraseña, se detiene el flujo
                    if (userEntity.isPasswordExpired()&& !requestUri.startsWith("/api/users/change-password")){
                        //Si la contraseña ha expirado, responde con un 403 Forbidde y un mensaje claro
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("error: La contraseña ha expirado, porfavor cambiala en el endpoint /api/users/change-password");
                        response.setContentType("application/json");
                        return;
                    }
                }
                //Sitodo es válido (el token y la contraseña), autentica al usuario
                // Crea un objeto de autenticación y lo establece en el contexto de seguridad de Spring.
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        // Paso 4: Dejar que la petición continúe, pasa la petición al siguiente filtro en la cadena de seguridad o, si no hay más, al controlador
        chain.doFilter(request, response);
    }

}
