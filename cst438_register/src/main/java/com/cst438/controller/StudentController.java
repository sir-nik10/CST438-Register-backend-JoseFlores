package com.cst438.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {
	
	@PostMapping("/student")
	public String createStudent() 
	{
		return "student_id = 12398";
	}
	@PostMapping("/student")
	public String createStatus()
	{
		return "status = H";
	}
	@PostMapping("/student")
	public String releaseHold() 
	{
		return "status = NULL";
	}
	//The story is :   As an administrator, I can add a student to the system.  I input the student email and name.  The student email must not already exists in the system.
	//As an administrator, I can put a HOLD on a student's registration.
	//As an administrator, I can release the HOLD on a student's registration.

}
