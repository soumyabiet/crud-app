package com.javaprojects.crud.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/crud")
public class CrudController {
	
	@GetMapping(value = "/user/list")
	public ResponseEntity<String> info() {
		return new ResponseEntity<String>("User List", HttpStatus.OK);
	}
	@GetMapping(value = "/home")
	public ResponseEntity<String> home() {
		return new ResponseEntity<String>("Landing Page", HttpStatus.OK);
	}

}
