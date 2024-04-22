package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random=new Random(1000);
	//email id form open handler
	@Autowired
	private EmailService emailservice;
	
	@Autowired
	private UserRepository userrepository;
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	@RequestMapping("/forgot")
	public String openemailform() {
		return "Forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendotp(@RequestParam("email")String email,HttpSession session) {
		System.out.println("EMAIL"+email);
		//generating otp of 4 digits
		
		int otp=random.nextInt(9999);
		System.out.println("OTP"+otp);
		
		//write code for sending otp to email
		String subject="OTP from SCM";
				String message=""
				+"<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+"OTP is"
				+"<b>"+otp
				+"</n>"
				+"</h1>"
				+"</div>";
						
								
				String to=email;
				
				boolean flag=this.emailservice.sendEmail(subject, message, to);
				if(flag)
				{
					session.setAttribute("myotp", otp);
					session.setAttribute("email", email);
					
					
					return "verify-otp";
				}
				else {
					session.setAttribute("message", "Check your emailid!!");
					
				return  "Forgot_email_form";
					
				}
	}
				
				//verify otp
				@PostMapping("/verify-otp")
				public String verifyOtp(@RequestParam("otp")int otp,HttpSession session) {
					int myOtp=(int)session.getAttribute("myotp") ;
					String email=(String)session.getAttribute("email") ;
					if(myOtp==otp)
					{
						//password change form
						User user=this.userrepository.getUserByUserName(email);
						if(user==null) {
							//send error message
							session.setAttribute("message",new Message("User does not exist with this emailid!!","danger"));
							return "Forgot_email_form";
						}
						else {
							
							//send change password form
							
						}
						
						return "password_change_form";
					
				}
					else {
						session.setAttribute("message", new Message("You have entered wrong otp!!","danger"));
						return "verify-otp";
						
					}
				}
		
		//change password
				@PostMapping("/change-password")
				public String Changepassword(@RequestParam("newpassword")String newpassword,HttpSession session) {
					String email=(String)session.getAttribute("email") ;
					User user=this.userrepository.getUserByUserName(email);
					user.setPassword(this.bcrypt.encode(newpassword));
					
					this.userrepository.save(user);
					return "redirect:/signin?change=password changed successfully..";
					
				}
		
		
		
	}

