package com.rohan.cloudProject.service;

import com.rohan.cloudProject.model.Bill;
import com.rohan.cloudProject.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        logger.info("Bill has been successfully saved.");
        return billRepository.save(billToBeSaved);
    }

    /**
     * Takes in User Id, if everything is validated successfully, checks if there are any bills for that User, if not
     * throws an Exception.
     *
     * @param userId
     * @return List<Bill></Bill>
     */
    public List<Bill> getAllBillsByUserId(String userId) {
        List<Bill> userBills = new ArrayList<>();
        List<Bill> allBills = billRepository.findAll();

        for (Bill bill : allBills) {
            if (bill.getUser().getId().equals(userId)) {
                userBills.add(bill);
            }
        }

        if (userBills.size() == 0) {
            throw new IllegalStateException("No bills exist for this user yet!");
        }

        return userBills;
    }

    /**
     * Delete the bill by the billId supplied.
     *
     * @param billId
     * @throws Exception
     */
    public void deleteById(String billId, String userId) throws Exception {
        if (!billRepository.existsById(billId)) {
            throw new Exception("The Bill ID doesn't exist!");
        }

        if (!billRepository.findById(billId).get().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("The Bill ID doesn't belong to the User Credentials supplied.");
        }

        billRepository.deleteById(billId);
    }

    /**
     * Fetches the bill by its id.
     *
     * @param billId
     * @return bill
     * @throws Exception
     */
    public Bill getBillByBillId(String billId, String userId) throws Exception {
        if (!billRepository.existsById(billId)) {
            throw new Exception("The Bill ID doesn't exist!");
        }

        if (!billRepository.findById(billId).get().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("The Bill ID doesn't belong to the User Credentials supplied.");
        }

        Bill foundBill = billRepository.findById(billId).get();

        return foundBill;
    }

    /**
     * Updates the bill by its ID.
     *
     * @param bill
     * @param billId
     * @return Bill
     * @throws Exception
     */
    public Bill updateBillByBillId(Bill bill, String billId, String userId) throws Exception {
        if (!billRepository.existsById(billId)) {
            throw new Exception("The Bill ID doesn't exist!");
        }

        if (!billRepository.findById(billId).get().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("The Bill ID doesn't belong to the User Credentials supplied.");
        }

        Date currentDate = new Date();
        return billRepository.findById(billId).map(
                updatedBill -> {
                    updatedBill.setVendor(bill.getVendor());
                    updatedBill.setBillDate(bill.getBillDate());
                    updatedBill.setDueDate(bill.getDueDate());
                    updatedBill.setAmountDue(bill.getAmountDue());
                    updatedBill.setCategories(bill.getCategories());
                    updatedBill.setPayStatus(bill.getPayStatus());
                    updatedBill.setBillUpdated(currentDate);
                    return billRepository.save(updatedBill);
                }).orElseThrow(() ->
                new IllegalStateException()
        );
    }
}
