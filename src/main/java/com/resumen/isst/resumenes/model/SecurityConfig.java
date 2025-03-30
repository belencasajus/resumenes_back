package com.resumen.isst.resumenes.model;

import java.beans.Customizer;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource; 
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .cors().and()
      .csrf().disable()
      .authorizeHttpRequests(auth -> auth
          .requestMatchers(HttpMethod.POST, "/login").permitAll()
          .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
          .anyRequest().authenticated()
      );
    return http.build();
}


@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    // Esto es clave para enviar y recibir cookies
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
}
