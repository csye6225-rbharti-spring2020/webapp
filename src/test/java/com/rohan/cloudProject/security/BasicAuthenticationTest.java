package com.rohan.cloudProject.security;

import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.model.exception.UserNotFoundException;
import com.rohan.cloudProject.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Test class for the Basic Authentication functionality.
 *
 * @author rohan_bharti
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BasicAuthenticationTest {

    private final String dummyUsername = "rohan.bharti@xyz.com";
    private final String dummyPassword = "QWerty2496@";
    private final String dummyBasicAuthToken = "Basic cm9oYW4uYmhhcnRpQHh5ei5jb206UVdlcnR5MjQ5NkA=";

    @InjectMocks
    @Autowired
    private BasicAuthentication basicAuthentication;

    @Mock
    private UserRepository mockUserRepository;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkAuthorizationProcess() throws UserNotFoundException {
        User dummyUser = new User();
        dummyUser.setId("123");
        dummyUser.setFirstName("Rohan");
        dummyUser.setLastName("Bharti");
        dummyUser.setEmail(dummyUsername);
        dummyUser.setPassword(encryptPassword(dummyPassword));

        List<User> users = new ArrayList<>();
        users.add(dummyUser);

        when(mockUserRepository.findAll()).thenReturn(users);

        String returnedId = basicAuthentication.authorize(dummyBasicAuthToken);

        Assert.assertEquals(returnedId, dummyUser.getId());
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
}
