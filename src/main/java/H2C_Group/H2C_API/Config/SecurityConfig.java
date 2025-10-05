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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/users/login", "/api/users/register", "api/users/registerTech").permitAll()
                        .requestMatchers("/api/client/PostTicket").permitAll()
                        .requestMatchers("/api/client/UpdateTicket/**").permitAll()
                        .requestMatchers("/api/client/DeleteTicket").permitAll()
                        .requestMatchers("/api/searchSolution").permitAll()
                        .requestMatchers("/api/GetSolutions").permitAll()
                        .requestMatchers("api/GetSolutionsWeb/**").permitAll() //ENDPOINT PARA APP WEB
                        .requestMatchers("/api/GetUserByUsername/{username}").authenticated()

                        // Endpoints autenticados
                        .requestMatchers("/api/users/change-password").authenticated()

                        // ✅ CORREGIDO: Endpoints para clientes
                        .requestMatchers("/api/client/**").hasAuthority("ROLE_CLIENTE")

                                //Logout
                                .requestMatchers("/api/users/logoutWeb").authenticated()

                                // ✅ CORREGIDO: Endpoints para técnicos Y administradores
                                .requestMatchers("/api/tech/**").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                // ENDPOINTS PARA TICKETS
                                .requestMatchers("/api/admin/GetTicketCounts").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/admin/GetTickets").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                // ENDPOINTS PARA SOLUCIONES
                                .requestMatchers("/api/PostSolution").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/UpdateSolution/**").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/DeleteSolution/**").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                // ENDPOINTS PARA ACTIVIDADES
                                .requestMatchers("/api/GetActivities").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/PostActivity").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/UpdateActivity/**").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/DeleteActivity/**").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                // ENDPOINTS PARA ANALITICA
                                .requestMatchers("/api/users/counts-by-month").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMINISTRADOR")
                                //ENDPOINTS PARA TECNICO VISTA ADMIN
                                .requestMatchers("/api/UpdateUser/**").hasAnyAuthority( "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/DeleteUser/**").hasAnyAuthority( "ROLE_ADMINISTRADOR")
                                .requestMatchers("/api/PostUser").hasAnyAuthority( "ROLE_ADMINISTRADOR")



                        //Endpoints para acceder al listado de tecnicos
                                .requestMatchers(HttpMethod.GET, "/api/GetTech").hasAnyAuthority("ROLE_CLIENTE", "ROLE_TECNICO", "ROLE_ADMINISTRADOR")

                                //Enpoint para que los tecnicos puedan obtener los tickets en espera
                                .requestMatchers(HttpMethod.GET, "/api/tech/GetTicketsEnEspera").hasAuthority("ROLE_TECNICO")

                                //Enpoint para poder obtener los tickets disponibles
                                .requestMatchers(HttpMethod.GET, "/api/tech/available-tickets").hasAuthority("ROLE_TECNICO")

                                //Endpoint para declinar un ticket
                                .requestMatchers(HttpMethod.POST, "/api/tech/decline-ticket/**").hasAuthority("ROLE_TECNICO")






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

                // Orígenes sin puerto (si accedes a la página directamente por localhost)
                "http://localhost",
                "https://localhost",
                "https://*.herokuapp.com",
                "https://*.vercel.app",
                "https://ptc-2025-app-web.vercel.app",
                "http://127.0.0.1",

                // Tu IP local con puerto de desarrollo (192.168.0.183)
                "http://192.168.0.183:5500",
                "http://192.168.0.183:5501"

                // Agrega aquí las IPs de tus compañeros si son estáticas y necesarias
                // "http://IPDANIELA:5501",
                // "http://IPHERBERT:5501"
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