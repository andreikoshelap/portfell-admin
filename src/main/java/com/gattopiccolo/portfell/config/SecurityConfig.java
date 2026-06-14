package com.gattopiccolo.portfell.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties({AdminProperties.class, UploadProperties.class})
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/login", "/css/**").permitAll()
						.anyRequest().hasRole("ADMIN"))
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/admin", true)
						.permitAll())
				.logout(logout -> logout.logoutSuccessUrl("/login?logout"))
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(AdminProperties properties, PasswordEncoder passwordEncoder) {
		var admin = User.withUsername(properties.username())
				.password(passwordEncoder.encode(properties.password()))
				.roles("ADMIN")
				.build();
		return new InMemoryUserDetailsManager(admin);
	}
}
