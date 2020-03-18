package com.rohan.cloudProject.configuration;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Class which sets up the StatsD Client Bean
 *
 * @author rohan_bharti
 */
@Configuration
public class MetricsClientBean {

    @Value("${publish.metrics}")
    private boolean publishMetrics;

    @Value("${metrics.server.hostname}")
    private String metricsServerHost;

    @Value("${metrics.server.port}")
    private int metricsServerPort;

    @Value("csye6225")
    private String prefix;

    /**
     * Creates the Bean for the StatsDClient by taking in the properties from the application.properties file
     *
     * @return StatsDClient
     */
    @Bean
    public StatsDClient statsDClient() {
        if (publishMetrics) {
            return new NonBlockingStatsDClient(prefix, metricsServerHost, metricsServerPort);
        }
        return new NoOpStatsDClient();
    }

}
