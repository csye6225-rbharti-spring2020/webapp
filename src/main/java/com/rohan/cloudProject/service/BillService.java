package com.rohan.cloudProject.service;

import com.rohan.cloudProject.model.Bill;
import com.rohan.cloudProject.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        if (billToBeSaved.getCategories().size() == 0) {
            throw new IllegalArgumentException("Please add a category!");
        }

        if (!(verifyDateFormat(billToBeSaved.getBillDate()) && verifyDateFormat(billToBeSaved.getDueDate()))) {
            throw new IllegalArgumentException("Date has to be in YYYY-MM-DD!");
        }

        logger.info("Bill has been successfully saved.");
        return billRepository.save(billToBeSaved);
    }

    private boolean verifyDateFormat(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = date.toString();
        if (date != null) {
            try {
                Date ret = simpleDateFormat.parse(dateString.trim());
                if (simpleDateFormat.format(ret).equals(dateString.trim())) {
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

