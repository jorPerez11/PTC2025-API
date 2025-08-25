package H2C_Group.H2C_API.Config;


import H2C_Group.H2C_API.Services.UserService;
import H2C_Group.H2C_API.Utils.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // Le dice a Spring que use el Bean 'corsConfigurationSource' de abajo
                .authorizeHttpRequests(auth -> auth
                        // 1. REGLAS ESPECÍFICAS DE PERMISOS (permitAll)

                        // Permite las peticiones OPTIONS (para pre-vuelo de CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Permite el acceso al endpoint de login y registro
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll()

                        // Permite las reglas de "primer uso"
                        .requestMatchers("/api/firstuse/**").permitAll()

                        // Permite explícitamente el acceso a la gestión inicial de compañía
                        // Nota: '/api/companies' se deja abierto para POST/PATCH para que el administrador inicial pueda crearla.
                        .requestMatchers(HttpMethod.POST, "/api/PostCompany", "/api/companies").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/companies/**").permitAll()

                        // NOTA: '/api/users/**' con PATCH es demasiado amplio. Si es solo para el cambio
                        // de contraseña, usa la regla más específica abajo. Si es para el CRUD de usuarios,
                        // podría dejarse así, pero recomiendo un rol específico (ej. hasAuthority("ROLE_ADMINISTRADOR")).
                        // De momento, dejo esta abierta como estaba en tu código:
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").permitAll()


                        // 2. REGLAS DE AUTORIDAD ESPECÍFICAS (hasAnyAuthority)

                        // Permite el acceso a la Base de Conocimiento solo a usuarios con roles específicos
                        .requestMatchers("/api/GetSolutions").hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_TECNICO", "ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/UpdateSolution/**").hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_TECNICO")


                        // 3. REGLAS DE AUTENTICACIÓN (authenticated)

                        // Permite que cualquier usuario autenticado cambie su contraseña
                        .requestMatchers("/api/users/change-password").authenticated()

                        // 4. REGLA DE CAPTURA GENERAL (Debe ser la última)
                        // Todos los demás endpoints requieren al menos estar autenticado
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        // AQUÍ ES DONDE SE APLICA TU VERDADERA SEGURIDAD DE ORIGEN
        // Esta lista es la única fuente de verdad para saber qué clientes (IPs/dominios) pueden hablar con tu API.
        configuration.setAllowedOrigins(Arrays.asList(
                "http://127.0.0.1:5501",
                "http://localhost",
                "http://127.0.0.2:5501",
                "http://IPJORGE:5501",
                "http://192.168.1.42:5501"
        ));
        // Define los métodos HTTP permitidos (GET, POST, etc.).
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Define los encabezados permitidos. El * permite todos los encabezados.
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Permite que las credenciales (como cookies o tokens) se incluyan en las peticiones.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a todas las rutas de tu API.
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}