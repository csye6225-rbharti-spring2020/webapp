package com.rohan.cloudProject.repository;

import com.rohan.cloudProject.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Bill Repository interface for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, String> {

}
