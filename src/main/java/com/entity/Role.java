package com.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
	
	ADMIN(
			Set.of(Permission.ADMIN_READ,
					Permission.ADMIN_UPDATE,
					Permission.ADMIN_DELETE,
					Permission.ADMIN_CREATE,
					Permission.MANAGER_READ,
					Permission.MANAGER_UPDATE,
					Permission.MANAGER_DELETE,
					Permission.MANAGER_CREATE
				)	
			),
	MANAGER(
			Set.of(Permission.MANAGER_READ,
					Permission.MANAGER_UPDATE,
					Permission.MANAGER_DELETE,
					Permission.MANAGER_CREATE)	
			),
	USER(Collections.EMPTY_SET);
	
	
	private final Set<Permission>permissions;
	
	
	private Role(Set emptySet) {
		this.permissions=emptySet;
	}
	
	private Set<Permission>getPermissions() {
		return this.permissions;
	}

	public List<SimpleGrantedAuthority> getAuthorities() {
		var authorities = getPermissions().stream().map(permission-> new SimpleGrantedAuthority(permission.name()))
		.collect(Collectors.toList());
		authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
		return authorities;
	}
}
