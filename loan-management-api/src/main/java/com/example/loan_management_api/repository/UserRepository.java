package com.example.loan_management_api.repository;

import com.example.loan_management_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}