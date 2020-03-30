package com.rohan.cloudProject;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableJpaRepositories("com.rohan.cloudProject.repository")
public class CloudProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudProjectApplication.class, args);
    }

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
}
