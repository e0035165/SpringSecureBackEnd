package com.controllerz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entity.AuthRequest;
import com.entity.AuthTokenRequest;
import com.entity.CustomUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.service.JwtService;
import com.service.UserInfoService;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/authorization")
public class AuthorizationController {
	
	@Autowired
	private AuthenticationManager manager;
	
	
	@Autowired
	private AuthenticationProvider provider;
	
	@Autowired
	private JwtService service;
	
	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private UserInfoService user_service;
	
	
	
	//private Logger authLogger = 
	
	@PostConstruct
	private void postConstruct() {
		System.out.println("Entrance to postConstruct...");
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		CustomUser admin = new CustomUser("admin","admin","admin@gmail.com");
		Authentication adminAuth = new UsernamePasswordAuthenticationToken(admin,null,admin.getAuthorities());
		System.out.println(service.jweEncrypt(admin.getUsername()));
		//provider.authenticate(adminAuth);
	}
	
	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody AuthRequest request) throws JsonProcessingException
	{
		CustomUser user = new CustomUser(request.getUsername()
				,encoder.encode(request.getPassword()),request.getEmail());
		user_service.addCustomUser(user);
		return new ResponseEntity<String>(service.jweEncrypt(request.getUsername()), HttpStatus.CREATED);
		
	}
	
	@GetMapping("/getToken")
	public ResponseEntity<String> getToken(@RequestBody AuthTokenRequest req) {
		CustomUser user = user_service.loadUserByUsername(req.getUsername());
		if(user==null)
			return new ResponseEntity<>("User is not found", HttpStatus.BAD_REQUEST);
		else if(!encoder.matches(req.getPassword(), user.getPassword()))
			return new ResponseEntity<>("User has wrong password. Please recheck passsword", HttpStatus.UNAUTHORIZED);
		else
			return new ResponseEntity<>(service.jweEncrypt(req.getUsername()), HttpStatus.ACCEPTED);
	}
}
