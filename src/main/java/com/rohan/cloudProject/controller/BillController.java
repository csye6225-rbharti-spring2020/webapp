package com.rohan.cloudProject.controller;

import com.rohan.cloudProject.model.Bill;
import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.security.BasicAuthentication;
import com.rohan.cloudProject.service.BillService;
import com.rohan.cloudProject.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
     * POST API to create a new bill. Bill is mapped to its respective User. Basic Auth is done before the bill is saved.
     *
     * @param authHeader
     * @param billToBeSaved
     * @return ResponseEntity
     */
    @PostMapping("/v1/bill/")
    @ApiOperation("Stores a new Bill")
    public ResponseEntity createBill(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader, @Valid @RequestBody Bill billToBeSaved) {
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            User user = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity("Authentication Error", HttpStatus.BAD_REQUEST);
            }

            user = userService.getUserDetails(userId);
            if (user == null) {
                return new ResponseEntity("User doesn't exist", HttpStatus.BAD_REQUEST);
            }

            billToBeSaved.setUser(user);
            billToBeSaved.setUserId(userId);
            try {
                Bill bill = billService.createNewBill(billToBeSaved);
                return new ResponseEntity(bill, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
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
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity("Authentication Error", HttpStatus.UNAUTHORIZED);
            }

            List<Bill> bills;
            try {
                bills = billService.getAllBillsByUserId(userId);
            } catch (Exception ex) {
                return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(bills, HttpStatus.OK);
        } else {
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
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity("Authentication Error", HttpStatus.UNAUTHORIZED);
            }

            try {
                billService.deleteById(billId);
            } catch (Exception ex) {
                return new ResponseEntity("The Bill for the ID provided doesn't exist", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
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
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity("Authentication Error", HttpStatus.UNAUTHORIZED);
            }

            Bill bill;
            try {
                bill = billService.getBillByBillId(billId);
            } catch (Exception ex) {
                return new ResponseEntity("The Bill for the ID provided doesn't exist", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity(bill, HttpStatus.OK);
        } else {
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
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity("Authentication Error", HttpStatus.UNAUTHORIZED);
            }

            Bill updatedBill;
            try {
                updatedBill = billService.updateBillByBillId(bill, billId);
            } catch (IllegalArgumentException illegalArgumentSection) {
                return new ResponseEntity("Please supply all the required fields to update the bill", HttpStatus.BAD_REQUEST);
            } catch (Exception ex) {
                return new ResponseEntity("The Bill for the ID provided doesn't exist", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity(updatedBill, HttpStatus.OK);
        } else {
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
        }
    }
}
