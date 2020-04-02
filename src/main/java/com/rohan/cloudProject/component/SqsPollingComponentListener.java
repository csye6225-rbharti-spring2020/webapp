package com.rohan.cloudProject.component;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.rohan.cloudProject.model.Bill;
import com.rohan.cloudProject.model.User;
import com.rohan.cloudProject.service.BillService;
import com.rohan.cloudProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQS Queue Component Listener
 *
 * @author rohan_bharti
 */
@Component
public class SqsPollingComponentListener {

    private final static Logger logger = LoggerFactory.getLogger(SqsPollingComponentListener.class);

    /**
     * Autowired amazonSqsClient
     */
    @Autowired(required = false)
    private AmazonSQS amazonSqsClient;

    /**
     * Autowired amazonSNSClient
     */
    @Autowired(required = false)
    private AmazonSNS amazonSNSClient;

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
     * Value of the SQS's Url
     */
    @Value("${amazon.sqs.url}")
    private String amazonSqsUrl;

    /**
     * Value of the SNS Topic
     */
    @Value("${amazon.sns.topic}")
    private String amazonSnsTopic;

    /**
     * Every second polls the SQS Queue to check if there's a message. Once it gets the message, it takes out the user's email and the List of
     * Due Bills, attach both of them to the SNS message and publishes on the SNS topic.
     *
     * @throws IOException
     */
    @Scheduled(fixedRate = 1000)
    public void getMessageFromQueue() throws IOException, ParseException {

        while (true) {
            final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(amazonSqsUrl).withMaxNumberOfMessages(1).withWaitTimeSeconds(3);

            final List<Message> messages = amazonSqsClient.receiveMessage(receiveMessageRequest).getMessages();
            String billsDueInfoString = "";
            String billsDueUrlsAndEmailString = "";

            for (final Message message : messages) {
                logger.debug("Message");
                logger.debug("MessageId: " + message.getMessageId());
                logger.debug("ReceiptHandle: " + message.getReceiptHandle());
                logger.debug("Body: " + message.getBody());
                if (!"".equals(message.getBody())) {
                    logger.info("POLLING: Fetching the Due Bills Json From the Message Received");

                    billsDueInfoString = message.getBody();
                    if (!billsDueInfoString.isEmpty()) {
                        logger.info("POLLING: User's ID and due Bills information successfully fetched from the SQS Queue");
                    } else {
                        logger.error("POLLING: No User Info and Bills due found in the SQS message");
                        return;
                    }

                    logger.info("AFTER POLLING: Started processing to fetch the bills due as per the user's request");

                    String[] info = billsDueInfoString.split(",");
                    String daysNum = info[0];
                    String userId = info[1];

                    List<Bill> billsDue;
                    String userEmail;

                    Long daysNumDue = Long.parseLong(daysNum);
                    billsDue = billService.getAllBillsDueByUserId(userId, daysNumDue);
                    User user = userService.getUserDetails(userId);
                    userEmail = user.getEmail();

                    logger.info("User email: " + userEmail);
                    List<String> billsDueEmailJsonList = new ArrayList<>();

                    billsDueEmailJsonList.add(userEmail);

                    for (Bill bill : billsDue) {
                        String billAccessUrl = billService.getAccessUrl(bill);
                        logger.info("Bill Url: " + billAccessUrl);
                        billsDueEmailJsonList.add(billAccessUrl);
                    }

                    billsDueUrlsAndEmailString = String.join(",", billsDueEmailJsonList);

                    final String messageReceiptHandle = messages.get(0).getReceiptHandle();
                    amazonSqsClient.deleteMessage(new DeleteMessageRequest(amazonSqsUrl, messageReceiptHandle));
                }
            }

            if (!billsDueUrlsAndEmailString.isEmpty()) {
                List<Topic> topics = amazonSNSClient.listTopics().getTopics();

                PublishRequest snsPublishRequest = new PublishRequest();
                snsPublishRequest.withMessage(billsDueUrlsAndEmailString);

                for (Topic topic : topics) {
                    if (topic.getTopicArn().endsWith(amazonSnsTopic)) {
                        logger.info("SNS Topic: " + amazonSnsTopic);
                        logger.info("SNS Topic ARN: " + topic.getTopicArn());
                        snsPublishRequest.withTopicArn(topic.getTopicArn());
                        PublishResult publishResponse = amazonSNSClient.publish(snsPublishRequest);
                        logger.info("SNS Message to be published: " + billsDueUrlsAndEmailString);
                        logger.info("SNS: Published message successfully to the SNS Topic with the User's Email and the List of Due Bills");
                        logger.info("Publish Response ID: " + publishResponse.getMessageId());
                        break;
                    }
                }

            }
        }

    }

}
