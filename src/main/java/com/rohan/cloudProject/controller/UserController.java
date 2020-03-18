package com.rohan.cloudProject.controller;

import com.google.common.base.Stopwatch;
import com.rohan.cloudProject.configuration.MetricsConstants;
import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.security.BasicAuthentication;
import com.rohan.cloudProject.service.UserService;
import com.timgroup.statsd.StatsDClient;
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
import java.util.concurrent.TimeUnit;

/**
 * User Controller class for the Spring Boot Application. Defines all the REST APIs.
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
     * Autowired statsDClient.
     */
    @Autowired
    private StatsDClient statsDClient;

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
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_USER_HTTP_GET);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
            }

            if (userId == null) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity("The credentials are incorrect!", HttpStatus.FORBIDDEN);
            }
            User user = userService.getUserDetails(userId);
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_GET, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide a valid username and password for authentication!", HttpStatus.UNAUTHORIZED);
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
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_USER_HTTP_POST);
        Stopwatch stopwatch = Stopwatch.createStarted();
        User newUser;
        try {
            newUser = userService.createNewUser(userToBeSaved);
        } catch (IllegalArgumentException ex) {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        if (newUser == null) {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("The User wasn't stored", HttpStatus.BAD_REQUEST);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_POST, stopwatch.elapsed(TimeUnit.MILLISECONDS));
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
        statsDClient.incrementCounter(MetricsConstants.ENDPOINT_USER_HTTP_PUT);
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic")) {
            String userId = null;
            try {
                userId = basicAuthentication.authorize(authHeader);
            } catch (IllegalArgumentException illegalArgumentException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
            }

            User user;
            try {
                user = userService.updateUser(fieldsToBeUpdated, userId);
            } catch (IllegalArgumentException e) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (NullPointerException nullPointerException) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity("Enter all the three fields - first_name, last_name and password!", HttpStatus.BAD_REQUEST);
            }

            if (user == null) {
                stopwatch.stop();
                statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new ResponseEntity("Enter all the three fields - first_name, last_name and password!", HttpStatus.BAD_REQUEST);
            }
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            stopwatch.stop();
            statsDClient.recordExecutionTime(MetricsConstants.TIMER_USER_HTTP_PUT, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ResponseEntity("Please provide Email and Password for Basic Authentication", HttpStatus.UNAUTHORIZED);
        }
    }

}
