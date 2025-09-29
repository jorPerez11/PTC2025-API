package H2C_Group.H2C_API.Utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwUtil jwtUtil;

    @Autowired
    public JwtHandshakeInterceptor(JwUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        log.info("🛠️ JwtHandshakeInterceptor inicializado");
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            Cookie[] cookies = httpRequest.getCookies();

            System.out.println("Interceptando handshake WebSocket...");
            log.info("Interceptando handshake WebSocket...");
            log.info("Cookies recibidas: {}", Arrays.toString(httpRequest.getCookies()));

            if (cookies != null) {
                boolean tokenEncontrado = false;

                for (Cookie cookie : cookies) {
                    if ("authToken".equals(cookie.getName())) {
                        tokenEncontrado = true;
                        String token = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);

                        if (jwtUtil.validateToken(token)) {
                            String username = jwtUtil.getUsernameFromToken(token);
                            attributes.put("username", username);
                            attributes.put("jwt", token);
                            log.info("✅ Token JWT válido. Usuario: {}", username);
                            return true;
                        } else {
                            log.warn("❌ Token JWT inválido o expirado");
                        }
                    }
                }

                if (!tokenEncontrado) {
                    log.warn("🚫 No se encontró la cookie authToken en el handshake");
                }
            }
        }

        response.setStatusCode(HttpStatus.FORBIDDEN);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // No hace falta implementar nada aquí
    }
}

