package com.rohan.cloudProject.controller;

import com.google.common.base.Stopwatch;
import com.rohan.cloudProject.configuration.MetricsConstants;
import com.rohan.cloudProject.model.Bill;
import com.rohan.cloudProject.model.File;
import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.model.exception.StorageException;
import com.rohan.cloudProject.security.BasicAuthentication;
import com.rohan.cloudProject.service.BillService;
import com.rohan.cloudProject.service.SqsService;
import com.rohan.cloudProject.service.UserService;
import com.timgroup.statsd.StatsDClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Bill Controller class for the Spring Boot Application. Defines all the REST APIs.
 *
 * @author rohan_bharti
 */
@RestController
@Api(value = "BillControllerAPI", produces = MediaType.APPLICATION_JSON_VALUE)
public class BillController {

    /**
     * Autowired UserService.
     */
    @Autowired
    private BillService billService;
    /**
     * Autowired UserService.
     */
    @Autowired
    private UserService userService;
    /**
     * Autowired BasicAuthentication.
     */
    @Autowired
    private BasicAuthentication basicAuthentication;

    /**
     * Autowired statsDClient.
     */
    @Autowired
    private StatsDClient statsDClient;

    /**
     * Autowired sqsService.
     */
    @Autowired(required = false)
    private SqsService sqsService;

    /**
     * Current Profile in use
     */
    @Value("${spring.profiles.active}")
    private String activeProfile;

    /**
     * POST API to create a new bill. Bill is mapped to its respective User. Basic Auth is done before the bill is saved.
     *
     * @param authHeader
     * @param billToBeSaved
     * @return ResponseEntity
     */
    @PostMapping("/v1/bill/")
    @ApiOperation("Stores a new Bill")
    public ResponseEntity createBill(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader, @Valid @RequestBody Bill billToBeSaved) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_BILL_HTTP_POST);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            User user = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
            }

            user = userService.getUserDetails(userId);
            if (user == null) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity("User doesn't exist", HttpStatus.BAD_REQUEST);
            }

            billToBeSaved.setUser(user);
            try {
                Bill bill = billService.createNewBill(billToBeSaved);
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(bill, HttpStatus.CREATED);
            } catch (Exception e) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * GET API to fetch all the bills for the supplied User Information.
     * Each Bill is mapped to its respective User. Basic Auth is done before the bills are fetched for that user.
     *
     * @param authHeader
     * @return ResponseEntity
     */
    @GetMapping("/v1/bills")
    @ApiOperation("Gets all the bills for the user")
    public ResponseEntity getBillsByUserId(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_BILLS_HTTP_GET);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            List<Bill> bills;
            try {
                bills = billService.getAllBillsByUserId(userId);
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(bills, HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * DELETE API to delete the bill for the billId supplied for. Each Bill is mapped to its respective User.
     * Basic Auth is done before the bills are fetched for that user.
     *
     * @param authHeader
     * @param billId
     * @return ResponseEntity
     */
    @DeleteMapping("/v1/bill/{id}")
    @ApiOperation("Deletes the bill for the User once he/she is authenticated")
    public ResponseEntity deleteBillById(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable(value = "id") String billId) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_BILL_HTTP_DELETE);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            try {
                billService.deleteById(billId, userId);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity("The Bill for the ID provided doesn't exist", HttpStatus.NOT_FOUND);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * GET API to fetch the bill by its id. Each Bill is mapped to its respective User.
     * Basic Auth is done before the bills are fetched for that user.
     *
     * @param authHeader
     * @param billId
     * @return ResponseEntity
     */
    @GetMapping("/v1/bill/{id}")
    @ApiOperation("Fetches the Bill by its bill id.")
    public ResponseEntity getBillById(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable(value = "id") String billId) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_BILL_HTTP_GET);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            Bill bill;
            try {
                bill = billService.getBillByBillId(billId, userId);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity("The Bill for the ID provided doesn't exist", HttpStatus.NOT_FOUND);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(bill, HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * PUT API to update the bill by its id. Each Bill is mapped to its respective User.
     * Basic Auth is done before the bills are fetched for that user.
     *
     * @param authHeader
     * @param billId
     * @param bill
     * @return ResponseEntity
     */
    @PutMapping("/v1/bill/{id}")
    @ApiOperation("Updates the Bill by its bill id.")
    public ResponseEntity updateBillById(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
                                         @PathVariable(value = "id") String billId, @Valid @RequestBody Bill bill) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_BILL_HTTP_PUT);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            Bill updatedBill;
            try {
                updatedBill = billService.updateBillByBillId(bill, billId, userId);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (IllegalStateException illegalState) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalState.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(updatedBill, HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILL_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Upon successful authentication, takes in the Multipart File supplied and stores it with its respective bill.
     *
     * @param authHeader
     * @param billId
     * @param file
     * @return
     */
    @PostMapping("/v1/bill/{id}/file")
    @ApiOperation("Stores a new File for the Bill information supplied")
    public ResponseEntity storeNewFile(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
                                       @PathVariable(value = "id") String billId, @RequestParam("file") MultipartFile file) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_FILE_HTTP_POST);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            File toBeSavedFile;
            try {
                toBeSavedFile = billService.createFileForBill(billId, userId, file);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (StorageException storageException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(storageException.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(toBeSavedFile, HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Upon successful authentication, takes in the FileId supplied and fetches its information from the database.
     *
     * @param authHeader
     * @param billId
     * @param fileId
     * @return
     */
    @GetMapping("/v1/bill/{billId}/file/{fileId}")
    @ApiOperation("Gets the File details for the Bill stored")
    public ResponseEntity getFileByFileId(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
                                          @PathVariable(value = "billId") String billId, @PathVariable(value = "fileId") String fileId) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_FILE_HTTP_GET);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            File file;
            try {
                file = billService.getFileForBill(billId, userId, fileId);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(file, HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Upon successful authentication, takes in the FileId supplied and deletes it from the database ensuring the bill
     * attached to it is also updated accordingly.
     *
     * @param authHeader
     * @param billId
     * @param fileId
     * @return
     */
    @DeleteMapping("/v1/bill/{billId}/file/{fileId}")
    @ApiOperation("Gets the File details for the Bill stored")
    public ResponseEntity deleteFileByFileId(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader,
                                             @PathVariable(value = "billId") String billId, @PathVariable(value = "fileId") String fileId) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_FILE_HTTP_DELETE);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            try {
                billService.deleteFileForBill(billId, userId, fileId);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity("The File for provided Bill ID doesn't exist", HttpStatus.NOT_FOUND);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_FILE_HTTP_DELETE, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * GET API to fetch all the bills due in the next N days for the supplied User Information.
     * Each Bill is mapped to its respective User. Basic Auth is done before the bills are fetched for that user.
     *
     * @param authHeader
     * @return ResponseEntity
     */
    @GetMapping("/v1/bills/due/{daysNum}")
    @ApiOperation("Gets all the bills for the user")
    public ResponseEntity getBillsDueByUserId(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable String daysNum) {
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_BILLS_DUE_HTTP_GET);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_DUE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            List<Bill> bills;
            String userEmail;
            try {
                Long daysNumDue = Long.parseLong(daysNum);
                bills = billService.getAllBillsDueByUserId(userId, daysNumDue);
                User user = userService.getUserDetails(userId);
                userEmail = user.getEmail();
            } catch (Exception ex) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_DUE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_DUE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));

            //Put the List of bills on the SQS Queue
            if (activeProfile.equals("aws")) {
                sqsService.enqueueBillsDueOnSqs(bills, userEmail);
            }

            return new ResponseEntity(HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_BILLS_DUE_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }
}
