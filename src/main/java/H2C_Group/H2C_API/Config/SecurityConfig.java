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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/api/PostCompany/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/PostCompany").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/companies").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/companies/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").permitAll()
                        .requestMatchers("/api/firstuse/**").permitAll()
                        .requestMatchers("/api/check-company-existence").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/users/login", "/api/users/register", "api/users/registerTech").permitAll()
                        .requestMatchers("/api/client/PostTicket").permitAll()
                        .requestMatchers("/api/client/UpdateTicket/**").permitAll()
                        .requestMatchers("/api/client/DeleteTicket").permitAll()
                        .requestMatchers("/api/searchSolution").permitAll()
                        .requestMatchers("/api/GetSolutions").permitAll()
                        .requestMatchers("api/GetSolutionsWeb/**").permitAll() //ENDPOINT PARA APP WEB

                        // Endpoints que requieren cualquier autenticación, pero antes de las reglas específicas de rol
                        .requestMatchers("/api/GetUserByUsername/{username}").authenticated()
                        .requestMatchers("/api/image/upload-to-folder").authenticated()
                        .requestMatchers("/api/users/change-password").authenticated()
                        .requestMatchers("/api/users/logoutWeb").authenticated()


                        // =========================================================================
                        // ✅ COMIENZO DE REGLAS DE ACCESO POR ROL (Ordenadas de más específico a general)
                        // =========================================================================

                        // 1. REGLA ESPECÍFICA (Corregida a hasRole() y movida al principio)
                        // Este endpoint es el que fallaba, lo dejamos con hasRole.
                        .requestMatchers("/api/tech/getAssignedTicketsByTechnicianIdPage/**").hasRole("TECNICO")

                        // 2. OTRAS REGLAS DE TÉCNICOS
                        .requestMatchers("/api/GetAssignedTicketsByTech/**").hasRole("TECNICO") // Usando hasRole
                        .requestMatchers(HttpMethod.GET, "/api/tech/GetTicketsEnEspera").hasRole("TECNICO") // Usando hasRole
                        .requestMatchers(HttpMethod.GET, "/api/tech/available-tickets").hasRole("TECNICO") // Usando hasRole
                        .requestMatchers(HttpMethod.POST, "/api/tech/decline-ticket/**").hasRole("TECNICO") // Usando hasRole


                        // 3. REGLAS GENERALES QUE INVOLUCRAN /api/tech o /api/admin

                        // Endpoint para acceder al listado de tecnicos
                        .requestMatchers(HttpMethod.GET, "/api/GetTech").hasAnyRole("CLIENTE", "TECNICO", "ADMINISTRADOR") // Usando hasAnyRole

                        // Endpoints para clientes (usando hasRole)
                        .requestMatchers("/api/client/**").hasRole("CLIENTE")

                        // Endpoints para técnicos Y administradores (usando hasAnyRole)
                        // Todos los endpoints restantes de /api/tech que no se definieron arriba
                        .requestMatchers("/api/tech/**").hasAnyRole("TECNICO", "ADMINISTRADOR")

                        // ENDPOINTS PARA TICKETS (usando hasAnyRole)
                        .requestMatchers("/api/admin/GetTicketCounts").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        .requestMatchers("/api/admin/GetTickets").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        // ENDPOINTS PARA SOLUCIONES (usando hasAnyRole)
                        .requestMatchers("/api/PostSolution").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        .requestMatchers("/api/UpdateSolution/**").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        .requestMatchers("/api/DeleteSolution/**").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        // ENDPOINTS PARA ACTIVIDADES (usando hasAnyRole)
                        .requestMatchers("/api/GetActivities").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        .requestMatchers("/api/PostActivity").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        .requestMatchers("/api/UpdateActivity/**").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        .requestMatchers("/api/DeleteActivity/**").hasAnyRole("TECNICO", "ADMINISTRADOR")
                        // ENDPOINTS PARA ANALITICA (usando hasAnyRole)
                        .requestMatchers("/api/users/counts-by-month").hasAnyRole("TECNICO", "ADMINISTRADOR")

                        // ENDPOINTS PARA ADMINISTRADOR (usando hasRole)
                        .requestMatchers("/api/UpdateUser/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/DeleteUser/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/PostUser").hasRole("ADMINISTRADOR")

                        // =========================================================================
                        // ✅ FIN DE REGLAS DE ACCESO POR ROL
                        // =========================================================================

                        // Cualquier otra petición debe estar autenticada
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
        //Ip de origen que pueden ACCEDER A LA API AGREGAR TODAS LAS IP DEL EQUIPO (JORGE, DANIELA, FERNANDO, ASTRID, HERBERT)
        configuration.setAllowedOrigins(Arrays.asList(
                // Localhost y 127.0.0.1 con puertos comunes de desarrollo
                "http://localhost:5500",
                "http://localhost:5501",
                "http://127.0.0.1:5500",
                "http://127.0.0.1:5501",
                "http://localhost:8080",
                "http://127.0.0.2:5501",


                // Orígenes sin puerto (vercel y localhost)
                "http://localhost",
                "https://localhost",
                "https://*.herokuapp.com",
                "https://*.vercel.app",
                "https://ptc-2025-app-web.vercel.app",
                "https://h2c-helpdesk-web.vercel.app/",
                "http://127.0.0.1",

                // Tu IP local con puerto de desarrollo (192.168.0.183)
                "http://192.168.0.183:5500",
                "http://192.168.0.183:5501"


        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}