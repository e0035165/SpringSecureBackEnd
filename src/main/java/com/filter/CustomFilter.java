package com.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.entity.CustomUser;
import com.service.JwtService;
import com.service.UserInfoService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class CustomFilter extends OncePerRequestFilter{
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserInfoService userInfoService;
	
	

	@Override
	protected void doFilterInternal(HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain)
			throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String username;
		if(authHeader!=null && authHeader.startsWith("Bearer ")) {
			jwt = authHeader.substring(7);
			username = jwtService.jweDecrypt(jwt);
		} else {
			username = null;
			jwt=null;
		}
		System.out.println(username);
		if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
			CustomUser user = userInfoService.loadUserByUsername(username);
			Boolean isValid = jwtService.isJWTtokenExpired(jwt);
			if(isValid) {
				System.out.println("Token validated and passsed");
				UsernamePasswordAuthenticationToken token = 
						new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
				token.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(token);
			} else {
				throw new RuntimeException("Token validity failed");
			}
		}
		filterChain.doFilter(request, response);
		
	}

}
