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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
                user = userService.getUserDetails(userId);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
            }

            billToBeSaved.setUser(user);
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

}
