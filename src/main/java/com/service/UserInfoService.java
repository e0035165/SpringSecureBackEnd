package com.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.entity.CustomUser;
import com.repositories.userInfoRepository;

@Service
public class UserInfoService implements UserDetailsService{
	@Autowired
	private userInfoRepository userinforepo;

	@Override
	public CustomUser loadUserByUsername(String username) throws UsernameNotFoundException {
		List<CustomUser>userDetailsList = userinforepo.findAll();
		System.out.println(username);
		System.out.println(userDetailsList.size());
		userDetailsList = userDetailsList.stream().filter(x->x.getUsername().equals(username)).collect(Collectors.toList());
		if(userDetailsList.isEmpty()) {
			return null;
		} else {
			return userDetailsList.get(0);
		}
	}
	
	
	public void addCustomUser(CustomUser user) {
		userinforepo.save(user);
	}

}
