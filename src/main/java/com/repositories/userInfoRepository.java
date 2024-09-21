package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.CustomUser;

@Repository
public interface userInfoRepository extends JpaRepository<CustomUser,Integer> {
	
}
