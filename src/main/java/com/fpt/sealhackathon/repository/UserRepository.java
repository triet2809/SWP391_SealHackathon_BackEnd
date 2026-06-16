package com.fpt.sealhackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpt.sealhackathon.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer>{
    Users findByEmail(String email);
}
