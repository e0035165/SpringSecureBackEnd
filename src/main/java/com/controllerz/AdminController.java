package com.controllerz;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
	
	@GetMapping("/getController")
	public String get() {
		return "GET:: admin controller";
	}
	
	
	@PostMapping("/postController")
	public String post() {
		return "POST:: admin controller";
	}
	
	@PutMapping("/putController")
	public String put() {
		return "PUT:: admin controller";
	}
	
	
	@DeleteMapping("/deleteController")
	public String delete() {
		return "DELETE:: admin controller";
	}

}
