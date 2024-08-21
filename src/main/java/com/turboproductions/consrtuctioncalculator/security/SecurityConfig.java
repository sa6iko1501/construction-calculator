/* Construction Calculator - Alexander Stoyanov! 2024 */
package com.turboproductions.consrtuctioncalculator.security;

import com.turboproductions.consrtuctioncalculator.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final UserService userService;

  @Autowired
  public SecurityConfig(UserService userService) {
    this.userService = userService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            (requests) ->
                requests
                    .requestMatchers("/register", "/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/home", true).permitAll())
        .sessionManagement(
            session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .invalidSessionUrl("/login?invalid-session=true"))
        .logout(LogoutConfigurer::permitAll);

    return http.build();
  }
}
