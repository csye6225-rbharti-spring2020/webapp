package com.rohan.cloudProject.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.rohan.cloudProject.model.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SQS Amazon Service Layer Class for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Service
@Profile("aws")
public class SqsService {

    private final static Logger logger = LoggerFactory.getLogger(SqsService.class);

    /**
     * Autowired amazonSqsClient
     */
    @Autowired(required = false)
    private AmazonSQS amazonSqsClient;

    /**
     * Autowired Bill Service
     */
    @Autowired
    private BillService billService;

    /**
     * Value of the SQS's Url
     */
    @Value("${amazon.sqs.url}")
    private String amazonSqsUrl;

    public boolean enqueueBillsDueOnSqs(List<Bill> billsDue, String userEmail) {

        try {
            logger.info("Sending a message to MyQueue, adding BillsDue to the message!");

            List<String> billsDueEmailJsonList = new ArrayList<>();

            billsDueEmailJsonList.add(userEmail);

            for (Bill bill : billsDue) {
                String billAccessUrl = billService.getAccessUrl(bill);
                logger.info("Bill Url: " + billAccessUrl);
                billsDueEmailJsonList.add(billAccessUrl);
            }

            String billsDueUrlsEmailListString = String.join(", ", billsDueEmailJsonList);

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(amazonSqsUrl);
            sendMessageRequest.setMessageBody(billsDueUrlsEmailListString);

            amazonSqsClient.sendMessage(sendMessageRequest);
            logger.info("Message to SQS: " + billsDueUrlsEmailListString);
            logger.info("Message Sent to the SQS Queue: " + amazonSqsUrl);
            return true;

        } catch (final AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException, which means " +
                    "your request made it to Amazon SQS, but was " +
                    "rejected with an error response for some reason.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());
            return false;
        } catch (final AmazonClientException ace) {
            logger.error("Caught an AmazonClientException, which means " +
                    "the client encountered a serious internal problem while " +
                    "trying to communicate with Amazon SQS, such as not " +
                    "being able to access the network.");
            logger.error("Error Message: " + ace.getMessage());
            return false;
        }
    }
}

