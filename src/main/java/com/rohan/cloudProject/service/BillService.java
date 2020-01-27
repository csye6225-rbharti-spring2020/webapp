package com.rohan.cloudProject.service;

import com.rohan.cloudProject.model.Bill;
import com.rohan.cloudProject.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Bill Service Layer Class for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Service
public class BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    @Autowired
    private BillRepository billRepository;

    /**
     * Takes in the Bill object, if everything is validated successfully, the user is saved
     * to the database with having Many to One mapping to its User.
     *
     * @param billToBeSaved
     * @return
     */
    public Bill createNewBill(Bill billToBeSaved) {

        Date currentDate = new Date();
        billToBeSaved.setBillCreated(currentDate);
        billToBeSaved.setBillUpdated(currentDate);

        logger.info("Bill has been successfully saved.");
        return billRepository.save(billToBeSaved);
    }
}

