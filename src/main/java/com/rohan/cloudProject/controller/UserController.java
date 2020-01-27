package com.rohan.cloudProject.controller;

import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.security.BasicAuthentication;
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
import java.util.Map;

/**
 * Controller class for the Spring Boot Application. Defines all the REST APIs.
 *
 * @author rohan_bharti
 */
@RestController
@RequestMapping(path = "/v1/user")
@Api(value = "UserControllerAPI", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

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
     * GET API to fetch the User's information. The User is authenticated by self created Basic Authentication.
     * Returns the respective status code as well on the basis of User's authentication.
     *
     * @param authHeader
     * @return User
     */
    @RequestMapping(path = "/self", method = RequestMethod.GET)
    @ApiOperation("Gets the User's information provided the User has been successfully authenticated")
    public ResponseEntity<User> getUserDetails(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
            }

            if (userId == null) {
                return new ResponseEntity("The credentials are incorrect!", HttpStatus.FORBIDDEN);
            }
            User user = userService.getUserDetails(userId);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * POST API to create a new User.
     *
     * @param userToBeSaved
     * @return User
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Stores a new User")
    public ResponseEntity<User> createUser(@Valid @RequestBody User userToBeSaved) {
        User newUser;
        try {
            newUser = userService.createNewUser(userToBeSaved);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        if (newUser == null) {
            return new ResponseEntity("The User wasn't stored", HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity(newUser, HttpStatus.CREATED);
        }
    }

    /**
     * PUT API to Update an already existing User.
     *
     * @param fieldsToBeUpdated
     * @return User
     */
    @RequestMapping(value = "/self", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Updates an already existing User")
    public ResponseEntity<User> updateUser(@RequestBody Map<String, Object> fieldsToBeUpdated, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
            }

            User user;
            try {
                user = userService.updateUser(fieldsToBeUpdated, userId);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (NullPointerException nullPointerException) {
                return new ResponseEntity("Enter all the three fields - first_name, last_name and password!", HttpStatus.BAD_REQUEST);
            }

            if (user == null) {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity("Please provide Email and Password (Basic Authentication", HttpStatus.FORBIDDEN);
        }
    }

}
