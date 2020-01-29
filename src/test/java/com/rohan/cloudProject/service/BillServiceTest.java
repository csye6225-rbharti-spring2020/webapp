package com.rohan.cloudProject.service;

import com.rohan.cloudProject.repository.BillRepository;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test class for the Bill Service Layer functions.
 *
 * @author rohan_bharti
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BillServiceTest {

    @InjectMocks
    @Autowired
    private BillService billService;

    @Mock
    private BillRepository billRepository;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
}
