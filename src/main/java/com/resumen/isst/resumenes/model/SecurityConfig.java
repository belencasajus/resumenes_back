package com.resumen.isst.resumenes.model;


import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource; 
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.*;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
          .cors(withDefaults())
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(HttpMethod.OPTIONS,  "/**").permitAll()
              .requestMatchers(HttpMethod.POST, "/login", "/usuarios", "/resumenes", "/suscripciones").permitAll()
              .requestMatchers(HttpMethod.GET,  "/usuarios", "/resumenes", "/usuarios/me", "/categorias").permitAll()
              .requestMatchers(HttpMethod.GET,  "/cover/**", "/audio/**").permitAll() 
              .requestMatchers(HttpMethod.GET, "/resumenes/**").authenticated()
              .requestMatchers("/").permitAll()
              .anyRequest().authenticated()
          )
          .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
          .build();
    }

@Bean
    public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path rootPath = Paths.get("").toAbsolutePath().getParent().resolve("public");

        registry.addResourceHandler("/cover/**")
                .addResourceLocations("file:" + rootPath.resolve("cover").toString() + "/");

        registry.addResourceHandler("/audio/**")
                .addResourceLocations("file:" + rootPath.resolve("audio").toString() + "/");
                
        registry.addResourceHandler("/user_imgs/**")
                .addResourceLocations("file:" + rootPath.resolve("user_imgs").toString() + "/");
    }
}
}
