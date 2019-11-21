package com.serjer.freeter.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.serjer.freeter.domain.Role;
import com.serjer.freeter.domain.User;
import com.serjer.freeter.repos.UserRepo;

@Service
public class UserService implements UserDetailsService {
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private MailSender mailSender;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return userRepo.findByUsername(username);
	}
	
	public boolean addUser(User user) {
		User userFromDb = userRepo.findByUsername(user.getUsername());
	      if (userFromDb != null) {
	          return false;
	        }
		user.setActive(true);
        user.setRoles(Collections.singleton(Role.ADMIN));
        user.setActivationCode(UUID.randomUUID().toString());
        userRepo.save(user);
        
        if(!StringUtils.isEmpty(user.getEmail())) {
        	String message = String.format(
        			"Hello, %s! \n"
        			+ "Welcome to Freeter. Please comfirm your email http://localhost:8080/activate/%s",
        			user.getUsername(),
        			user.getActivationCode());
        	mailSender.send(user.getEmail(), "Activation code", message);
        }
        return true;
	}

	public boolean activateUser(String code) {
		User user = userRepo.findByActivationCode(code);
		if (user == null) {
			return false;
		}
		user.setActivationCode(null);
		userRepo.save(user);
		return true;
	}
}
