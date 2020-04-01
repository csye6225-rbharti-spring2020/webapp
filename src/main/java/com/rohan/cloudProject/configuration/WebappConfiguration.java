package com.rohan.cloudProject.configuration;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Configuration class for all the beans required by the Application
 *
 * @author rohan_bharti
 */
@Configuration
public class WebappConfiguration {

    @Bean
    public Docket swaggerConfig() {
        return new Docket(DocumentationType.SWAGGER_2).select().paths(PathSelectors.any()).
                apis(RequestHandlerSelectors.basePackage("com.rohan.cloudProject")).build();
    }

    @Bean
    @Profile("aws")
    public AmazonS3 amazonS3Client() {
        AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
        return amazonS3Client;
    }

    @Bean
    @Profile("aws")
    public AmazonSQS amazonSqsClient() {
        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        return sqs;
    }

    @Bean
    @Profile("aws")
    public AmazonSNS amazonSNSClient() {
        AmazonSNS snsClient = AmazonSNSAsyncClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .withRegion(Regions.US_EAST_1)
                .build();
        return snsClient;
    }
}
