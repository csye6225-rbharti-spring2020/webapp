package com.rohan.cloudProject.service;

import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.repository.UserRepository;
import org.passay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service Layer Class for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Autowired userRepository.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Takes the newly passed User Object, adds account created and updated information and stores in the database
     * with the encrypted password.
     *
     * @param newUser
     * @return User
     */
    public User createNewUser(User newUser) throws IllegalArgumentException {

        User toBeSavedUser = new User();

        boolean duplicateEmail = checkDuplicateEmail(newUser.getEmail());
        if (duplicateEmail) {
            throw new IllegalArgumentException(("The Email Already Exists!"));
        }

        boolean isValidEmail = checkEmailValidity(newUser.getEmail());
        if (!isValidEmail) {
            throw new IllegalArgumentException("Please Enter a valid Email Address");
        }

        boolean isValidPassword = checkPasswordValidity(newUser.getPassword());
        if (!isValidPassword) {
            throw new IllegalArgumentException("Please enter a strong Password: length between 8 and 16 characters, " +
                    "at least one upper-case character, one lower-case character, one digit character, one symbol (special character)" +
                    " and no whitespace.");
        }

        toBeSavedUser.setFirstName(newUser.getFirstName());
        toBeSavedUser.setLastName(newUser.getLastName());
        toBeSavedUser.setEmail(newUser.getEmail());

        String hashedPassword = encryptPassword(newUser.getPassword());
        toBeSavedUser.setPassword(hashedPassword);

        Date currentDate = new Date();
        toBeSavedUser.setAccountCreated(currentDate);
        toBeSavedUser.setAccountUpdated(currentDate);

        try {
            userRepository.save(toBeSavedUser);
            logger.info("A new User saved successfully!");
        } catch (Exception e) {
            logger.error("New User couldn't be saved: " + e.getMessage());
        }

        return toBeSavedUser;
    }

    /**
     * Takes in the new/updated User object and its id and updates the database with the new information
     * supplied.
     *
     * @param fieldsToBeUpdated
     * @param id
     * @return User
     */
    public User updateUser(Map<String, Object> fieldsToBeUpdated, String id) throws IllegalArgumentException, NullPointerException {

        List<String> fieldsNotToBeUpdated = Arrays.asList(new String[]{
                "id", "email_address", "account_created", "account_updated"
        });

        for (String fieldNotToBeUpdated : fieldsNotToBeUpdated) {
            if (fieldsToBeUpdated.containsKey(fieldNotToBeUpdated)) {
                throw new IllegalArgumentException("Only first_name, last_name and password can be updated!");
            }
        }

        User user = userRepository.findById(id).get();

        user.setFirstName((String) fieldsToBeUpdated.get("first_name"));
        user.setLastName((String) fieldsToBeUpdated.get("last_name"));

        String passwordSupplied = (String) fieldsToBeUpdated.get("password");
        boolean isValidPassword = checkPasswordValidity(passwordSupplied);
        if (!isValidPassword) {
            throw new IllegalArgumentException("Please enter a strong Password: length between 8 and 16 characters, " +
                    "at least one upper-case character, one lower-case character, one digit character, one symbol (special character)" +
                    " and no whitespace.");
        }

        user.setPassword(encryptPassword(passwordSupplied));
        user.setAccountUpdated(new Date());
        logger.info("User information updated successfully!");

        try {
            userRepository.save(user);
        } catch (Exception e) {
            return null;
        }

        return user;
    }

    /**
     * Fetches the User Object based on the userId supplied.
     *
     * @param id
     * @return User
     */
    public User getUserDetails(String id) {
        try {
            return userRepository.findById(id).get();
        } catch (Exception e) {
            logger.error("The User couldn't be fetched from the Database successfully!");
            return null;
        }
    }

    /**
     * Helper Method. Takes in the plain password and encrypts it using BCryptPasswordEncoder with BCrypt Salt.
     *
     * @param plainPassword
     * @return encryptedPassword
     */
    private String encryptPassword(String plainPassword) {
        String strongSalt = BCrypt.gensalt(10);
        String encryptedPassword = BCrypt.hashpw(plainPassword, strongSalt);
        return encryptedPassword;
    }

    /**
     * Helper function to check id the email supplied by the user is valid or not.
     *
     * @param email
     * @return boolean
     */
    public boolean checkEmailValidity(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper function to ensure that the password entered is a strong password.
     *
     * @param password
     * @return boolean
     */
    public boolean checkPasswordValidity(String password) {
        List<Rule> passwordRules = Arrays.asList(
                new LengthRule(8, 16),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        );

        PasswordValidator passwordValidator = new PasswordValidator(passwordRules);
        PasswordData passwordData = new PasswordData(password);

        return passwordValidator.validate(passwordData).isValid();
    }

    /**
     * Helper function to convert the given String into CamelCase String
     *
     * @param str
     * @return
     */
    private String toCamelCase(String str) {
        String[] individualStrings = str.split("_");
        String camelCaseString = "";
        boolean firstIndividualString = true;
        for (String string : individualStrings) {
            if (firstIndividualString) {
                camelCaseString = string;
                firstIndividualString = false;
            } else {
                camelCaseString += toProperCase(string);
            }
        }
        return camelCaseString;
    }

    /**
     * Helper function to handle the Case for toCamelCase function
     *
     * @param str
     * @return
     */
    private String toProperCase(String str) {
        return str.substring(0, 1).toUpperCase() +
                str.substring(1).toLowerCase();
    }

    /**
     * Helper function to check if the email supplied during the POST API is duplicate or not
     *
     * @param email
     * @return
     */
    private boolean checkDuplicateEmail(String email) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Takes in the user object and deletes it from the database.
     *
     * @param user
     */
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}
