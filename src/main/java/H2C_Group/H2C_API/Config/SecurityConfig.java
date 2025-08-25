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
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Permite explícitamente el acceso a la creación de la compañía.
                        // Permite explícitamente el acceso a la creación de la compañía.
                        .requestMatchers(HttpMethod.POST, "/api/PostCompany/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/PostCompany").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/companies").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/companies/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").permitAll()

                        // Permite el acceso a todos los endpoints del "primer uso"
                        .requestMatchers("/api/firstuse/**").permitAll()

                        // Permite las peticiones OPTIONS (para pre-vuelo de CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Permite acceso público a los endpoints de login y registro
                        .requestMatchers("/api/users/login", "/api/users/register", "api/users/registerTech").permitAll()
                        //Permite el acceso a este endpoint solo si el usuario esta autenticado
                        .requestMatchers("/api/users/change-password").authenticated()
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
                "http://127.0.0.1:5501", //
                "http://localhost:5501", //
                "http://127.0.0.1",     //
                "http://localhost",     //
                "http://127.0.0.2:5501",
                "http://179.5.94.204:5501",
                "http://192.168.1.42:5501",
                "http://IPDANIELA:5501",
                "http://IPHERBERT:5501"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

