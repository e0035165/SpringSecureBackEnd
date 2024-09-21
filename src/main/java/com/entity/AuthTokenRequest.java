package com.entity;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenRequest {
	private String username;
	
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public AuthTokenRequest(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AuthTokenRequest() {
		super();
	}
	
	
}
