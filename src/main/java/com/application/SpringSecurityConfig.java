package com.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.filter.CustomFilter;
import com.service.UserInfoService;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
	@Autowired
	private CustomFilter filter;
	
	@Autowired
	private UserInfoService user_info_service;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests(req->req.requestMatchers(HttpMethod.POST, "/authorization/**").authenticated()
											.requestMatchers(HttpMethod.PUT, "/authorization/**").authenticated()
											.requestMatchers(HttpMethod.DELETE,"/authorization/**").authenticated()
											.requestMatchers(HttpMethod.GET,"/authorization/**").permitAll()
											.requestMatchers("/api/v1/management/**").permitAll()
											.requestMatchers("/api/v1/admin/**").authenticated()
				);
		http.headers(headers->headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
		http.csrf(csrf->csrf.disable());
		http.httpBasic(Customizer.withDefaults());
		http.sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authenticationProvider(authenticationProvider()).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	
	
	
	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
	
	@Bean
	public UserDetailsService userDetailsService() {
		System.out.println(passwordEncoder().encode("Vignesh$@1995g"));
		return user_info_service;
	}
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
    	System.out.println("test DAO");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

}
