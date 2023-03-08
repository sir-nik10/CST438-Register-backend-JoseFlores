package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;
import com.cst438.service.GradebookService;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://registerf-cst438.herokuapp.com/"})
public class StudentController {
	
	@Autowired
	StudentRepository studentRepository;
	
	
	@PostMapping("/student")
	public void addStudent( @RequestParam("name") String student_name, @RequestParam("email") String student_email){
		
		Student email = studentRepository.findByEmail(student_email);
		if(email != null){
			System.out.println("/student already exists in system."+student_email);
			//throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student already exists " );
		}
		else {
			Student student = new Student();
			student.setEmail(student_email);
			student.setName(student_name);
			Student savedStudent = studentRepository.save(student);
		}
	}
	
	@PutMapping("/student")
	public void addHold( @RequestParam("email") String student_email, @RequestParam("status") String student_status){
		
		Student currentStudent = studentRepository.findByEmail(student_email);
		
		if(currentStudent==null) {
			System.out.println("Student does not exist in system. Could not complete status change.");
		}
		else {
			currentStudent.setStatus(student_status);
			studentRepository.save(currentStudent);
			
			System.out.println(student_email+" has status changed to "+ student_status);
		}
	}
	
	@PutMapping("/student/removehold")
	public void deleteHold( @RequestParam("email") String student_email){
		
		Student currentStudent = studentRepository.findByEmail(student_email);
		
		if(currentStudent==null) {
			System.out.println("Student does not exist in system. Could not complete status change.");
		}
		else if(currentStudent.getStatus()==null) {
			System.out.println("Student does not have a hold currently. Could not complete status change.");
		}
		else {
			currentStudent.setStatus(null);
			studentRepository.save(currentStudent);
			
			System.out.println(student_email+" student hold has been relased.");
		}
	}
}
