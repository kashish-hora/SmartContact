package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepository;

public class UserDetailsServiceimp implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//fetching user from database
		com.smart.entities.User user=userRepository.getUserByUserName(username);
		if(user==null) {
			throw new UsernameNotFoundException("could not found user!!");
		}
		CustomUserDetails customuserdetails=new CustomUserDetails (user) ;
		return  customuserdetails;
		
		
		
		
		
	}
	
	
	

}
