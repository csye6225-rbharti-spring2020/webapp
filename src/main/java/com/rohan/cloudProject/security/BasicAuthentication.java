package com.rohan.cloudProject.security;

import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.repository.UserRepository;
import com.rohan.cloudProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for Custom Basic Authentication.
 *
 * @author rohan_bharti
 */
@Service
public class BasicAuthentication {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Autowired userRepository.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Checks if the Username and Password values passed in the Authorization header
     * exist in the database or not.
     *
     * @param authHeader
     * @return userId
     */
    public String authorize(String authHeader) {
        Map<String, String> credentials = getCredentials(authHeader);
        String email = credentials.get("email");
        String password = credentials.get("password");

        List<User> users = userRepository.findAll();

        boolean userExists = false;
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                userExists = true;
            }
        }

        if (!userExists) {
            return null;
        }

        for (User user : users) {
            if (user.getEmail().equals(email) && passwordMatch(password, user.getPassword())) {
                return user.getId();
            } else {
                continue;
            }
        }
        return null;
    }

    /**
     * Helper function to check if the plainPassword and encryptedPassword supplied match.
     *
     * @param plainPassword
     * @param encryptedPassword
     * @return Boolean
     */
    private boolean passwordMatch(String plainPassword, String encryptedPassword) {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return bcrypt.matches(plainPassword, encryptedPassword);
    }

    /**
     * Takes in the Authorization Header and strips it to get a Map of Username and Password.
     *
     * @param authHeader
     * @return Map<String, String>
     */
    private Map<String, String> getCredentials(String authHeader) {
        Map<String, String> credentials = new HashMap<>();
        String base64Token = authHeader.substring("Basic".length()).trim();
        byte[] decodedCredentials = Base64.getDecoder().decode(base64Token);
        String combinedCredentials = new String(decodedCredentials, StandardCharsets.UTF_8);
        final String[] decodedValues = combinedCredentials.split(":", 2);
        credentials.put("email", decodedValues[0].trim());
        credentials.put("password", decodedValues[1].trim());
        return credentials;
    }
}
