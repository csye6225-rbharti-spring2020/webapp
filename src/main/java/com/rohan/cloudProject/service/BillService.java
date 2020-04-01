package com.rohan.cloudProject.service;

import com.google.common.base.Stopwatch;
import com.rohan.cloudProject.configuration.MetricsConstants;
import com.rohan.cloudProject.model.Bill;
import com.rohan.cloudProject.model.File;
import com.rohan.cloudProject.model.exception.StorageException;
import com.rohan.cloudProject.repository.BillRepository;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Bill Service Layer Class for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Service
public class BillService {

    private final static Logger logger = LoggerFactory.getLogger(BillService.class);

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private FileService fileService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${domain.name}")
    private String domainName;

    /**
     * Autowired statsDClient.
     */
    @Autowired
    private StatsDClient statsDClient;

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
        Stopwatch stopwatch = Stopwatch.createStarted();
        Bill bill = billRepository.save(billToBeSaved);
        stopwatch.stop();
        statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_BILL_SAVE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return bill;
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

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Bill> allBills = billRepository.findAll();
        stopwatch.stop();
        statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_BILLS_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));

        for (Bill bill : allBills) {
            if (bill.getUser().getId().equals(userId)) {
                userBills.add(bill);
            }
        }

        if (userBills.size() == 0) {
            throw new IllegalStateException("No bills exist for this user yet!");
        }

        logger.info("Bills have been successfully retrieved for the User");
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

        Bill bill = getBillByBillId(billId, userId);
        if (bill.getBillFile() != null) {
            String fileId = bill.getBillFile().getFileId();
            File file = fileService.getFileById(fileId);
            if (activeProfile.equals("dev")) {
                Files.deleteIfExists(Paths.get(file.getStorageUrl() + "-" + billId));
                logger.info("File has been successfully deleted physically from the system!");
            }

            if (activeProfile.equals("aws")) {
                boolean isDeleted = fileService.deleteFileFromS3Bucket(file.getFileName());
                if (isDeleted) {
                    logger.info("File has been successfully deleted physically from the S3 Bucket!");
                }
            }
        }

        logger.info("Bill " + billId + " has been successfully deleted with its attached file");

        Stopwatch stopwatch = Stopwatch.createStarted();
        billRepository.deleteById(billId);
        stopwatch.stop();
        statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_BILL_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
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

        Stopwatch stopwatch = Stopwatch.createStarted();
        Bill foundBill = billRepository.findById(billId).get();
        stopwatch.stop();
        statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_BILL_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));

        logger.info("Retrieved Bill for the User: " + userId);

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
                    logger.info("Successfully updated the Bill with the new information supplied for Bill: " + billId);
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    Bill newBill = billRepository.save(updatedBill);
                    stopwatch.stop();
                    statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_BILL_SAVE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    return newBill;
                }).orElseThrow(() ->
                new IllegalStateException()
        );
    }

    /**
     * After authenticating the user and checking if the bill exists, it creates a File Model object for the Multipart file and
     * returns it on successful persistence.
     *
     * @param billId
     * @param userId
     * @param file
     * @return File
     * @throws Exception
     */
    public File createFileForBill(String billId, String userId, MultipartFile file) throws Exception {

        Bill bill = getBillByBillId(billId, userId);

        if (bill.getBillFile() != null) {
            throw new StorageException("The Bill already has a File attached!");
        }

        File storedFile = fileService.createNewFile(file, billId);

        //adding the UserId of the file being stored
        storedFile.setUserId(userId);
        storedFile.setBillId(billId);

        bill.setBillFile(storedFile);

        Stopwatch stopwatch = Stopwatch.createStarted();
        billRepository.save(bill);
        stopwatch.stop();
        statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_FILE_SAVE, stopwatch.elapsed(TimeUnit.MILLISECONDS));

        //Fetching the File object after persisting it in the database
        storedFile = bill.getBillFile();

        logger.info("Created a new File successfully for the user: " + userId + " and the bill: " + billId);

        return storedFile;
    }

    /**
     * After authenticating the user and checking if the bill exists, it checks if the fileId supplied matches the ID of the file
     * associated with the Bill object. If yes, returns the file details.
     *
     * @param billId
     * @param userId
     * @param fileId
     * @return
     * @throws Exception
     */
    public File getFileForBill(String billId, String userId, String fileId) throws Exception {

        Bill bill = getBillByBillId(billId, userId);

        if (bill.getBillFile() == null) {
            throw new Exception("This Bill has no file attached to it");
        }

        if (fileService.getFileById(fileId) == null) {
            throw new Exception("The file doesn't exist.");
        }

        if (!bill.getBillFile().getFileId().equals(fileId)) {
            throw new IllegalArgumentException("The File ID doesn't belong to the Bill details provided.");
        }

        logger.info("Fetched the file for the Bill: " + billId + " successfully");

        return fileService.getFileById(fileId);
    }

    /**
     * After authenticating the user and checking if the bill exists, it checks if the fileId supplied matches the ID of the file
     * associated with the Bill object. If yes, deletes it.
     *
     * @param billId
     * @param userId
     * @param fileId
     */
    public void deleteFileForBill(String billId, String userId, String fileId) throws Exception {

        Bill bill = getBillByBillId(billId, userId);

        if (bill.getBillFile() == null) {
            throw new Exception("This Bill has no file attached to it");
        }

        if (!bill.getBillFile().getFileId().equals(fileId)) {
            throw new IllegalArgumentException("The File ID doesn't belong to the Bill details provided.");
        }

        File file = fileService.getFileById(fileId);

        if (activeProfile.equals("dev")) {
            Files.deleteIfExists(Paths.get(file.getStorageUrl() + "-" + billId));
            logger.info("File has been successfully deleted physically from the system!");
        }

        if (activeProfile.equals("aws")) {
            boolean isDeleted = fileService.deleteFileFromS3Bucket(file.getFileName());
            if (isDeleted) {
                logger.info("File has been successfully deleted physically from the S3 Bucket!");
            }
        }

        //Setting the File for the bill to null, due to cascading deletes it from the table
        bill.setBillFile(null);

        logger.info("Deleted the Bill: " + billId + " successfully");

        Stopwatch stopwatch = Stopwatch.createStarted();
        billRepository.save(bill);
        stopwatch.stop();
        statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_FILE_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    /**
     * After authenticating the user, supplies a List of Bills for the user which are due in the next 'daysNumDue' days.
     *
     * @param userId
     * @param daysNumDue
     */
    public List<Bill> getAllBillsDueByUserId(String userId, Long daysNumDue) throws ParseException {
        List<Bill> userBills = new ArrayList<>();
        List<Bill> billsDue = new ArrayList<>();
        Date currentDate = new Date();

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Bill> allBills = billRepository.findAll();
        stopwatch.stop();
        statsDClient.recordExecutionTime(MetricsConstants.TIMER_DATABASE_BILLS_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));

        for (Bill bill : allBills) {
            if (bill.getUser().getId().equals(userId)) {
                userBills.add(bill);
            }
        }

        if (userBills.size() == 0) {
            throw new IllegalStateException("No bills exist for this user yet!");
        }

        Long daysDiff;

        for (Bill bill : userBills) {
            String billDueDate = bill.getDueDate().toString();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dueDate = formatter.parse(billDueDate);
            if (currentDate.compareTo(dueDate) <= 0) {
                long diff = dueDate.getTime() - currentDate.getTime();
                daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                logger.info(String.valueOf(daysDiff));
                if (daysDiff.intValue() >= 0 && daysDiff.compareTo(daysNumDue) <= 0) {
                    billsDue.add(bill);
                }
            }
        }

        logger.info(billsDue.size() + " Due Bills have been successfully retrieved for the User");
        return billsDue;
    }

    /**
     * Creates a Url String for the Bill Object
     *
     * @return
     */
    public String getAccessUrl(Bill bill) {
        StringBuilder sb = new StringBuilder("http://");
        if (!domainName.equals("notAvailable")) {
            sb.append(domainName);
        } else {
            logger.error("Domain Name wasn't successfully received by the application");
        }
        sb.append("/v1/bill/");
        sb.append(bill.getBillId());
        return sb.toString();
    }
}

