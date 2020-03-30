package com.rohan.cloudProject.component;

import com.amazonaws.internal.SdkInternalMap;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * SQS Queue Componnet Listener
 *
 * @author rohan_bharti
 */
@Component
public class SqsPollingComponentListener {

    private final static Logger logger = LoggerFactory.getLogger(SqsPollingComponentListener.class);

    /**
     * Autowired amazonSqsClient
     */
    @Autowired
    private AmazonSQS amazonSqsClient;

    /**
     * Autowired amazonSNSClient
     */
    @Autowired
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
            List<String> billsDueList = new ArrayList<>();
            String userEmail = "";

            for (final Message message : messages) {
                logger.debug("Message");
                logger.debug("MessageId: " + message.getMessageId());
                logger.debug("ReceiptHandle: " + message.getReceiptHandle());
                logger.debug("Body: " + message.getBody());
                if (!"".equals(message.getBody())) {
                    logger.info("Fetching the Due Bills Json From the Message Received");

                    billsDueList = message.getMessageAttributes().get("billsDue").getStringListValues();
                    userEmail = message.getMessageAttributes().get("email").getStringValue();
                    if (!userEmail.isEmpty()) {
                        logger.info("User's email successfully fetched from the SQS Queue");
                    } else {
                        logger.error("No email found in the SQS message");
                        return;
                    }
                    if (billsDueList.size() > 0) {
                        logger.info("Bills successfully fetched from the SQS Queue");
                    } else {
                        logger.error("Bills were NOT fetched successfully from the SQS Queue OR no due bills Exist for the User");
                        return;
                    }

                    final String messageReceiptHandle = messages.get(0).getReceiptHandle();
                    amazonSqsClient.deleteMessage(new DeleteMessageRequest(amazonSqsUrl, messageReceiptHandle));
                }
            }

            if (billsDueList.size() > 0) {
                List<Topic> topics = amazonSNSClient.listTopics().getTopics();
                SdkInternalMap<String, MessageAttributeValue> messageAttributes = new SdkInternalMap<>();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(baos);
                for (String billStringJson : billsDueList) {
                    out.writeUTF(billStringJson);
                }
                byte[] bytes = baos.toByteArray();

                MessageAttributeValue billsMessageAttributeValue = new MessageAttributeValue();
                billsMessageAttributeValue.setBinaryValue(ByteBuffer.wrap(bytes));
                MessageAttributeValue emailMessageAttributeValue = new MessageAttributeValue();
                if (!userEmail.isEmpty()) {
                    emailMessageAttributeValue.setStringValue(userEmail);
                }

                messageAttributes.put("billsDueByteArray", billsMessageAttributeValue);
                messageAttributes.put("userEmail", emailMessageAttributeValue);

                PublishRequest snsPublishRequest = new PublishRequest();
                snsPublishRequest.withMessageAttributes(messageAttributes);

                for (Topic topic : topics) {
                    if (topic.getTopicArn().endsWith(amazonSnsTopic)) {
                        snsPublishRequest.withTopicArn(topic.getTopicArn());
                        amazonSNSClient.publish(snsPublishRequest);
                        logger.info("Published successfully to the SNS Topic with the User's Email and the List of Due Bills");
                        break;
                    }
                }

            }
        }

    }

}