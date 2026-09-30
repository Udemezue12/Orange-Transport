package com.astrotech.transport.config;


import lombok.RequiredArgsConstructor;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class JobRunrConfig {

    private final DeleteOnSuccessFilter deleteOnSuccessFilter;

    @Bean
    public StorageProvider databaseStorageProvider(
            DataSource dataSource) {

        return SqlStorageProviderFactory
                .using(dataSource);
    }


    @Bean
    public JobScheduler jobScheduler(StorageProvider storageProvider, ApplicationContext applicationContext) {
        return JobRunr.configure()
                .useStorageProvider(storageProvider)
                .useJobActivator(applicationContext::getBean)
                .withJobFilter(deleteOnSuccessFilter)
                .useBackgroundJobServer()
//                .useDashboard()
                .initialize()
                .getJobScheduler();
    }


    private BackgroundJobServerConfiguration getServerOptions() {
        var uniqueServerId = generateUniqueServerId();

        return BackgroundJobServerConfiguration
                .usingStandardBackgroundJobServerConfiguration()
                .andId(uniqueServerId)
                .andWorkerCount(5)
                .andPollIntervalInSeconds(15)
                .andDeleteSucceededJobsAfter(Duration.ZERO);
    }

    private UUID generateUniqueServerId() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            return UUID.nameUUIDFromBytes(host.getBytes());
        } catch (UnknownHostException e) {
            return UUID.randomUUID();
        }
    }
}
