package com.rohan.cloudProject.repository;

import com.rohan.cloudProject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
