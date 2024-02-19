package com.prosto.indexer.library;

import com.prosto.indexer.library.repository.UserElasticRepository;
import com.prosto.indexer.library.repository.UserSqlRepository;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
@EnableTransactionManagement
@EnableJpaRepositories(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UserSqlRepository.class),
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UserElasticRepository.class))
@EnableElasticsearchRepositories(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UserElasticRepository.class),
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UserSqlRepository.class))
public class LibraryApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(LibraryApplication.class, args);
	}
	@Autowired JobLauncher jobLauncher;
	@Autowired
	private ListableJobLocator listableJobLocator;


	@Scheduled(fixedRate = 1000)
	public void run() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("d", System.nanoTime())
				.toJobParameters();
		for (String jobName : listableJobLocator.getJobNames()) {
			jobLauncher.run(listableJobLocator.getJob(jobName), jobParameters);
		}
	}
}
