package com.smart.controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepostiory;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Path;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.razorpay.*;


@Controller
@RequestMapping("/user")

public class UserController {
	@Autowired
	private BCryptPasswordEncoder bcryptpasswordencoder;
	@Autowired
	
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactrepository;
	@Autowired
	private MyOrderRepostiory myorderrepo;
	
	
	
	
	
	
	@ModelAttribute
	//method for adding common data to response
	public void addCommonData(Model model,Principal principal) {
		String userName=principal.getName();
		System.out.println("USERNAME"+userName);
		
		com.smart.entities.User user=userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		model.addAttribute("user",user);
		
	}
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
		
		//get the user using username(Email)
		return "normal/user_dashboard";
	}
	//open add form handler
	@GetMapping("/add-contact")
	public String OpenAddContactForm(Model model){
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,//for getting contact details of user
			@RequestParam("Profileimage")MultipartFile file,//for getting the image uploaded by user
			Principal principal,HttpSession session) {//for fetching user details by name
		try {
		
		String name=principal.getName();
		com.smart.entities.User user=this.userRepository.getUserByUserName(name);
		contact.setUser(user);//contact ko user dena hai
		//processing and uploading file
		if(file.isEmpty()) {
			System.out.println("file is empty");
			contact.setImage("google-contacts.png");
		}
		else {
			//put the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			//for finding out the path where to upload the image
			File savefile=new ClassPathResource("static/image").getFile();
			java.nio.file.Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);//for getting the file,for giving the location where to upload file
			System.out.println("File Uploaded");
		}
		
		user.getContacts().add(contact);//list mai contact add krenge user ki
		this.userRepository.save(user);
		System.out.println("DATA"+contact);
		System.out.println("Added to data base");
		//message success
		session.setAttribute("message",new Message("Your contact is added !! Add more..","success"));
		}
		
		catch(Exception e) {
			
			e.printStackTrace();
			//error message
			session.setAttribute("message",new Message("Something went wrong !! Try again..","danger"));
		}
		return  "normal/add_contact_form";
	}
	
	//show contact handler
	
	@GetMapping("/show-contacts/{page}")
	public String showcontacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title","Show User Contacts");
		String userName=principal.getName();
		com.smart.entities.User user=this.userRepository.getUserByUserName(userName);
		//per page=5[n]
		//current page=0[page]
		Pageable pageable=PageRequest.of(page,5);
		Page<Contact> contacts=this.contactrepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		//contact ki list bhejni hai for this we create a contact repository
		return "normal/show_contacts";
		
		
		
	}
	// showing each contact detail
	@RequestMapping("/{cid}/contact")
	public String showcontactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		System.out.println("CID" +cid)	;
		
		Optional<Contact> contactoptional=this.contactrepository.findById(cid);
		Contact contact=contactoptional.get();
		
		//
		String username=principal.getName();
		com.smart.entities.User user=this.userRepository.getUserByUserName(username);
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			
		}
		
		
		
		return "normal/contact_detail";
		}
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String DeleteContact(@PathVariable("cid") Integer cid,Model model,HttpSession session,Principal principal) {
		System.out.println("CID" +cid)	;
		
		Optional<Contact> contactoptional=this.contactrepository.findById(cid);
		Contact contact=contactoptional.get();
		//check
		System.out.println("Contact"+contact.getCid());
		contact.setUser(null);
		//this.contactrepository.delete(contact);
		com.smart.entities.User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);

		
		
		session.setAttribute("message",new Message("Contact Deleted Successfully...","success"));
		return "redirect:/user/show-contacts/0";
	}
	//update form handler
	
	@PostMapping("/update-contact/{cid}")
	public String UpdateForm(@PathVariable("cid") Integer cid,Model m) {
		m.addAttribute("title","Update Contact");
		Contact contact=this.contactrepository.findById(cid).get();
		m.addAttribute("contact", contact);
			return "normal/update_form";
		
	
	}
	//update contact handler
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updatehandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		
		//processing and uploading file
		//image
		try {
			Contact oldcontactdetails=this.contactrepository.findById(contact.getCid()).get();
			//processing and uploading file
			if(!file.isEmpty())
			{
				//file work
				//rewrite
				
				//delete old photo
				File deleteFile=new ClassPathResource("static/image").getFile();
				File file1=new File(deleteFile,oldcontactdetails.getImage());
				file1.delete();
				
				//upload new photo
				File saveFile=new ClassPathResource("static/image").getFile();
				java.nio.file.Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
				
				
				
			else
			{
				contact.setImage(oldcontactdetails.getImage());
				
				
			}
			com.smart.entities.User user=this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);

			
			this.contactrepository.save(contact);

			

			//message success
			session.setAttribute("message", new Message("Your contact is updated","success"));
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			//error message
			
		}
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	
	
//your profile handler
	@GetMapping("/profile")
	public String Yourprofile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	//change password handler
	@PostMapping("/change-password")
	public String changepassword(@RequestParam("oldpassword") String oldpassword,@RequestParam("newpassword") String newpassword,Principal principal,HttpSession session) {
		System.out.println("OLD PASSWORD:"+oldpassword);
		System.out.println("NEW PASSWORD:"+newpassword);
		String userName=principal.getName();
		com.smart.entities.User currentuser=this.userRepository.getUserByUserName(userName);
		System.out.println(currentuser.getPassword());
		if(this.bcryptpasswordencoder.matches(oldpassword, currentuser.getPassword()))
		{
			//change the password
			currentuser.setPassword(this.bcryptpasswordencoder.encode(newpassword));
			this.userRepository.save(currentuser);
			session.setAttribute("message",new Message("Your password is successfully changed..","success"));
		}
		else {
			session.setAttribute("message",new Message("Please Enter correct old password!!","danger"));
			return "redirect:/user/settings";
		}
		
		
		
		
		
		return "redirect:/user/index";
		
	}
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object>data,Principal principal) throws Exception {
		System.out.println(data);
		int amt=Integer.parseInt(data.get("amount").toString());
		var client=new RazorpayClient("rzp_test_qyXpyHLysIAKKq","Me9iCInYyBAAJqwTjkbcckcm");
		
		
		JSONObject obj=new JSONObject();
		obj.put("amount",amt*100);
		obj.put("currency","INR");
		obj.put("receipt", "txn_235425");
		
		//creating order
		Order order=client.orders.create(obj);
		
		System.out.println(order);
		//save the order in database
		MyOrder myorder=new MyOrder();
		myorder.setAmount(order.get("amount")+"");
		myorder.setOrderId(order.get("id"));
		myorder.setPaymentId(null);
		myorder.setStatus("created");
		myorder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myorder.setReceipt(order.get("receipt"));
		this.myorderrepo.save(myorder);
		
		
		
		
		
		//if you want you can save this to your database
		
		return order.toString();
	}
	@PostMapping("/update_order")
	
	public ResponseEntity<?> updateorder(@RequestBody Map<String,Object>data) {
		MyOrder myorder=this.myorderrepo.findByOrderId(data.get("order_id").toString());
		myorder.setPaymentId(data.get("payment_id").toString());
		
		myorder.setStatus(data.get("status").toString());
		this.myorderrepo.save(myorder);
		
	
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
}
