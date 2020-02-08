package com.rohan.cloudProject.repository;

import com.rohan.cloudProject.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * File Repository interface for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Repository
public interface FileRepository extends JpaRepository<File, String> {
}
