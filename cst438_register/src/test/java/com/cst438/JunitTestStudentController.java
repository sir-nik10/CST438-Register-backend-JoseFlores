package com.cst438;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cst438.controller.ScheduleController;
import com.cst438.controller.StudentController;
import com.cst438.domain.Course;
import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;
import com.cst438.service.GradebookService;

@ContextConfiguration(classes = { StudentController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
class JunitTestStudentController {
	
	static final String URL = "http://localhost:8080";
	public static final int TEST_STUDENT_ID = 10;
	public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME  = "test";
	public static final String TEST_STATUS = "H";
	public static final int TEST_STATUS_CODE = 101;
	
	@MockBean
	StudentRepository studentRepository;
	
	@Autowired
	private MockMvc mvc;
	
	//Junit test to verify an addStudent method behaves properly in Student Controller
	@Test
	public void addStudent() throws Exception {
		
		//create a mock response to simulate a http response
		MockHttpServletResponse response;
		
		Student teststudent = new Student();
		
		teststudent.setStudent_id(TEST_STUDENT_ID);
		teststudent.setEmail(TEST_STUDENT_EMAIL);
		teststudent.setName(TEST_STUDENT_NAME);
		teststudent.setStatus(TEST_STATUS);
		teststudent.setStatusCode(TEST_STATUS_CODE);
		
		//test to see if find by email return the test student should return null because should not exist yet
	    given(studentRepository.findByEmail(TEST_STUDENT_EMAIL)).willReturn((null));
	    
	    //test to see if find by Id returns the test student should return null because new student
	    given(studentRepository.findById(TEST_STUDENT_ID)).willReturn(null);
	    
	    //http POST for adding a student    
	    response = mvc.perform(
				MockMvcRequestBuilders
			      .post("/student/?name="+TEST_STUDENT_NAME+"&email="+TEST_STUDENT_EMAIL)
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
	    
	    //test to see that http request returns success
	    assertEquals(200, response.getStatus());
	}
	
	
	//Junit Test Method to verify that addHold behaves properly 
	//basically when addHold called, is a hold indicator sent to the database
	@Test
	public void addHold() throws Exception {
		MockHttpServletResponse response;
		
		//This is making the student test entity 
		Student teststudent = new Student();
		
		teststudent.setStudent_id(TEST_STUDENT_ID);
		teststudent.setEmail(TEST_STUDENT_EMAIL);
		teststudent.setName(TEST_STUDENT_NAME);
		teststudent.setStatus(TEST_STATUS);
		teststudent.setStatusCode(TEST_STATUS_CODE);
		
		//This is vital because the mock database will return null by default - there is no database and no data to reference
		//In the student controller the addHold method uses findByEmail to make the put request
		//the 'given' test methods will verify findByEmail properly returns the correct data (int this case the test entity teststudent)
		given(studentRepository.findByEmail(TEST_STUDENT_EMAIL)).willReturn(teststudent);
		
		//given for student already tested above so do not duplicate test?
		
		//perform put request to database
		response = mvc.perform(
				MockMvcRequestBuilders
			      .put("/student?email="+TEST_STUDENT_EMAIL+"&status="+TEST_STATUS)
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		//test to see that http request returns success
	    assertEquals(200, response.getStatus());
	    
	    //test that http request does not return 400 
	    //test case where student DNE in student controller
	    assertNotEquals(400, response.getStatus());

	    
	    //test Student object -> setStatus() sets status properly
	    assertEquals("H", teststudent.getStatus());
	    
	    
	    //NEXT PART TESTS IF STUDENT DOES NOT EXIST IN DATABASE
	    //I AM NOT SURE IF IT IS NECESSARY 
	    
	    //test entity that tests addHold() method for a student that does not exist
	    //real world application: Someone types in an email or name wrong 
	    Student nullstudent = new Student();
	    
	    nullstudent.setStudent_id(100);
		nullstudent.setEmail(null);
		nullstudent.setName(null);
		nullstudent.setStatus(null);
		nullstudent.setStatusCode(100);
		
		//I am telling given that it will be null
		given(studentRepository.findByEmail("nullemail@incorrect.com")).willReturn(null);

		//perform put request to database 
		//simulates incorrect information the database will not find this email
		response = mvc.perform(
				MockMvcRequestBuilders
			      .put("/student?email="+"nullemail@incorrect.com"+"&status="+"H")
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		//We throw an exception in student controller that will return error 400
		assertEquals(400, response.getStatus());
		//We confirm that a request with incorrect information does not return success 200
		assertNotEquals(200, response.getStatus());
	}
	
	@Test
	public void deleteHold() throws Exception {
		MockHttpServletResponse response;
		
		//CASE 1: Student has a hold and it gets successfully cleared

		//This is making the student test entity 
		Student teststudent = new Student();
		
		teststudent.setStudent_id(TEST_STUDENT_ID);
		teststudent.setEmail(TEST_STUDENT_EMAIL);
		teststudent.setName(TEST_STUDENT_NAME);
		teststudent.setStatus(TEST_STATUS); //Does have a hold
		teststudent.setStatusCode(TEST_STATUS_CODE);
		
		given(studentRepository.findByEmail(TEST_STUDENT_EMAIL)).willReturn(teststudent);
		
	    given(studentRepository.findById(TEST_STUDENT_ID)).willReturn(null);

	    response = mvc.perform(
				MockMvcRequestBuilders
			      .put("/student/removehold?email="+TEST_STUDENT_EMAIL)
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
	    
	    //should return success because student does exist and student does have a hold
	    assertEquals(200, response.getStatus());
	    
	    //should not return fail error number 400
	    assertNotEquals(400, response.getStatus());
	    
	    //CASE 2: Student exists but does not have a hold to be deletetd
	    
	    //new testing entity to test case 2
	    Student studentnohold = new Student();
		
	    studentnohold.setStudent_id(TEST_STUDENT_ID);
	    studentnohold.setEmail("nohold@csumb.edu");
	    studentnohold.setName(TEST_STUDENT_NAME);
	    studentnohold.setStatus(null); //Does NOT have a hold
	    studentnohold.setStatusCode(TEST_STATUS_CODE);
	    
	    //find by email should return the testing entity
		given(studentRepository.findByEmail("nohold@csumb.edu")).willReturn(studentnohold);
		
		//make a put request for an existing student with no hold on account
		response = mvc.perform(
				MockMvcRequestBuilders
			      .put("/student/removehold?email="+"nohold@csumb.edu")
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		//should return fail error 400
		assertEquals(400, response.getStatus());
		
		//should not return success 
		assertNotEquals(200, response.getStatus());
		
	}
	
	//CASE 3: Student DNE 

}
