package com.rohan.cloudProject.component;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    public void getMessageFromQueue() throws IOException {

        while (true) {
            logger.info("Receiving messages from MyQueue");
            final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(amazonSqsUrl).withMaxNumberOfMessages(1).withWaitTimeSeconds(3);

            final List<Message> messages = amazonSqsClient.receiveMessage(receiveMessageRequest).getMessages();
            String billsDueUrlsEmailListString = "";

            for (final Message message : messages) {
                logger.debug("Message");
                logger.debug("MessageId: " + message.getMessageId());
                logger.debug("ReceiptHandle: " + message.getReceiptHandle());
                logger.debug("Body: " + message.getBody());
                if (!"".equals(message.getBody())) {
                    logger.info("POLLING: Fetching the Due Bills Json From the Message Received");

                    billsDueUrlsEmailListString = message.getBody();
                    if (!billsDueUrlsEmailListString.isEmpty()) {
                        logger.info("POLLING: User's email and due Bills URLs successfully fetched from the SQS Queue");
                    } else {
                        logger.error("POLLING: No email and Bills due URLs found in the SQS message");
                        return;
                    }

                    final String messageReceiptHandle = messages.get(0).getReceiptHandle();
                    amazonSqsClient.deleteMessage(new DeleteMessageRequest(amazonSqsUrl, messageReceiptHandle));
                }
            }

            if (!billsDueUrlsEmailListString.isEmpty()) {
                List<Topic> topics = amazonSNSClient.listTopics().getTopics();

                PublishRequest snsPublishRequest = new PublishRequest();
                snsPublishRequest.withMessage(billsDueUrlsEmailListString);

                for (Topic topic : topics) {
                    if (topic.getTopicArn().endsWith(amazonSnsTopic)) {
                        snsPublishRequest.withTopicArn(topic.getTopicArn());
                        amazonSNSClient.publish(snsPublishRequest);
                        logger.info("SNS: Published message successfully to the SNS Topic with the User's Email and the List of Due Bills");
                        break;
                    }
                }

            }
        }

    }

}
