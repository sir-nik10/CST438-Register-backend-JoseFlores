package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;

/*
 * This example shows how to use selenium testing using the web driver 
 * with Chrome browser.
 * 
 *  - Buttons, input, and anchor elements are located using XPATH expression.
 *  - onClick( ) method is used with buttons and anchor tags.
 *  - Input fields are located and sendKeys( ) method is used to enter test data.
 *  - Spring Boot JPA is used to initialize, verify and reset the database before
 *      and after testing.
 *      
 *    Make sure that TEST_COURSE_ID is a valid course for TEST_SEMESTER.
 *    
 *    URL is the server on which Node.js is running.
 */

@SpringBootTest
public class EndToEndScheduleTest {

	public static final String CHROME_DRIVER_FILE_LOCATION = "/Users/nickflores/chromedriver_mac64/chromedriver";

	public static final String URL = "http://localhost:3000";

	public static final String TEST_USER_EMAIL = "test@csumb.edu";

	public static final int TEST_COURSE_ID = 40443; 

	public static final String TEST_SEMESTER = "2021 Fall";

	public static final int SLEEP_DURATION = 1000; // 1 second.
	
	/*
	 * add student name string for testing addStudent
	 */
	
	public static final String TEST_STUDENT_NAME = "test";
	
	public static final String TEST_STUDENT_EMAIL = "csumbtest@csumb.edu";

	/*
	 * When running in @SpringBootTest environment, database repositories can be used
	 * with the actual database.
	 */
	
	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	StudentRepository studentRepository;

	/*
	 * Student add course TEST_COURSE_ID to schedule for 2021 Fall semester.
	 */
	
	@Test
	public void addCourseTest() throws Exception {

		/*
		 * if student is already enrolled, then delete the enrollment.
		 */
		
		Enrollment x = null;
		do {
			x = enrollmentRepository.findByEmailAndCourseId(TEST_USER_EMAIL, TEST_COURSE_ID);
			if (x != null)
				enrollmentRepository.delete(x);
		} while (x != null);

		// set the driver location and start driver
		//@formatter:off
		// browser	property name 				Java Driver Class
		// edge 	webdriver.edge.driver 		EdgeDriver
		// FireFox 	webdriver.firefox.driver 	FirefoxDriver
		// IE 		webdriver.ie.driver 		InternetExplorerDriver
		//@formatter:on

		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		ChromeOptions ops = new ChromeOptions();
		ops.addArguments("--remote-allow-origins=*");	


           WebDriver driver = new ChromeDriver(ops);
	
		// Puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		try {

			driver.get(URL);
			Thread.sleep(SLEEP_DURATION);

			// select the last of the radio buttons on the list of semesters page.
			
			WebElement we = driver.findElement(By.xpath("(//input[@type='radio'])[last()]"));
			we.click();

			// Locate and click "Get Schedule" button
			
			driver.findElement(By.xpath("//a")).click();
			Thread.sleep(SLEEP_DURATION);

			// Locate and click "Add Course" button which is the first and only button on the page.
			driver.findElement(By.xpath("//button")).click();
			Thread.sleep(SLEEP_DURATION);

			// enter course no and click Add button
			
			driver.findElement(By.xpath("//input[@name='course_id']")).sendKeys(Integer.toString(TEST_COURSE_ID));
			driver.findElement(By.xpath("//button[@id='Add']")).click();
			Thread.sleep(SLEEP_DURATION);

			/*
			* verify that new course shows in schedule.
			* get the title of all courses listed in schedule
			*/ 
		
			Course course = courseRepository.findById(TEST_COURSE_ID).get();
			
			List<WebElement> elements  = driver.findElements(By.xpath("//div[@data-field='title']/div[@class='MuiDataGrid-cellContent']"));
			boolean found = false;
			for (WebElement e : elements) {
				System.out.println(e.getText()); // for debug
				if (e.getText().equals(course.getTitle())) {
					found=true;
					break;
				}
			}
			assertTrue( found, "Course added but not listed in schedule.");
			
			// verify that enrollment row has been inserted to database.
			
			Enrollment e = enrollmentRepository.findByEmailAndCourseId(TEST_USER_EMAIL, TEST_COURSE_ID);
			assertNotNull(e, "Course enrollment not found in database.");

		} catch (Exception ex) {
			throw ex;
		} finally {

			// clean up database.
			
			Enrollment e = enrollmentRepository.findByEmailAndCourseId(TEST_USER_EMAIL, TEST_COURSE_ID);
			if (e != null)
				enrollmentRepository.delete(e);

			driver.quit();
		}	

	}
	@Test
	public void addStudentTest() throws Exception {
		/*
		 * if student already exists, then delete the student.
		 */

		Student x = null;
		do {
			x = studentRepository.findByEmail(TEST_STUDENT_EMAIL);
			if (x != null)
				studentRepository.delete(x);
		} while (x != null);
		
		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		ChromeOptions ops = new ChromeOptions();
		ops.addArguments("--remote-allow-origins=*");	


           WebDriver driver = new ChromeDriver(ops);
	
		// Puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		try {

			driver.get(URL);
			Thread.sleep(SLEEP_DURATION);


			// Locate and click "Get Schedule" button
			
			driver.findElement(By.id("Add Student")).click();
			Thread.sleep(SLEEP_DURATION);
			
			//fill in new student information: name and email
			driver.findElement(By.id("Student Name")).sendKeys(TEST_STUDENT_NAME);
			driver.findElement(By.id("Student Email")).sendKeys(TEST_STUDENT_EMAIL);
			Thread.sleep(SLEEP_DURATION);
			
			//submit new student information
			driver.findElement(By.id("Add")).click();
			Thread.sleep(SLEEP_DURATION);

			/*
			* verify that new Student added to database.
			* get the Student of all Students listed in repository
			*/ 
		
			Student student = null;
			
			student = studentRepository.findByEmail(TEST_STUDENT_EMAIL);
			
			boolean works;
			
			if(student==null) {
				 works = false;
			}else {
				works = true;
			}
				
			assertTrue( works, "Student added successfully into system");
			
			// verify that enrollment row has been inserted to database.
			
			assertNotNull(student, "Student not found in database.");

		} catch (Exception ex) {
			throw ex;
		} finally {

			// clean up database.
			
			Student s = studentRepository.findByEmail(TEST_STUDENT_EMAIL);
			if (s != null)
				studentRepository.delete(s);

			driver.quit();
		}
	}
}
