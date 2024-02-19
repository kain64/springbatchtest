package com.prosto.indexer.library.config;

import com.prosto.indexer.library.writers.ElasticSearchWriter;
import com.prosto.indexer.library.writers.SqlItemWriter;
import com.prosto.indexer.library.entity.User;
import com.prosto.indexer.library.processors.UsersItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.concurrent.Future;

@Configuration
public class BatchConfig {
    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);
    @Value("${application.input.file:C:/tmp/Users.csv}")
    private String inputFile;
    @Value("${application.input.fields:firstName,lastName,street,city,zip,email}")
    private String[] names;
    @Value("${application.chunk.size:1000}")
    private int chunkSize;
    final
    ElasticSearchWriter elasticSearchWriter;
    final
    TaskExecutor manualTaskExecutor;
    final
    SqlItemWriter sqlItemWriter;
    final
    DataSource dataSource;

    public BatchConfig(ElasticSearchWriter elasticSearchWriter, TaskExecutor manualTaskExecutor, SqlItemWriter sqlItemWriter, DataSource dataSource) {
        this.elasticSearchWriter = elasticSearchWriter;
        this.manualTaskExecutor = manualTaskExecutor;
        this.sqlItemWriter = sqlItemWriter;
        this.dataSource = dataSource;
    }

    @Bean
    public ItemReader<User> sqlReader() {
        JdbcPagingItemReader<User> reader = new JdbcPagingItemReader<>();
        final SqlPagingQueryProviderFactoryBean sqlPagingQueryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        sqlPagingQueryProviderFactoryBean.setDataSource(dataSource);
        sqlPagingQueryProviderFactoryBean.setSelectClause("select *");
        sqlPagingQueryProviderFactoryBean.setFromClause("from users");
        sqlPagingQueryProviderFactoryBean.setSortKey("id");
        try {
            reader.setQueryProvider(Objects.requireNonNull(sqlPagingQueryProviderFactoryBean.getObject()));
        } catch (Exception e) {
            log.error("sqlReader init failed", e);
            throw new RuntimeException(e);
        }
        reader.setDataSource(dataSource);
        reader.setPageSize(chunkSize);
        reader.setRowMapper(new BeanPropertyRowMapper<>(User.class));
        return reader;
    }
    @Bean
    public FlatFileItemReader<User> fileReader() throws MalformedURLException {
        log.info("reader init");
        return new FlatFileItemReaderBuilder<User>()
                .name("UserItemReader")
                .resource(new FileUrlResource(inputFile))
                .delimited()
                .names(names)
                .targetType(User.class)
                .build();
    }
    @Bean
    public UsersItemProcessor processor() {
        log.info("processor init");
        return new UsersItemProcessor();
    }
    @Bean
    public ItemProcessor<User, Future<User>> asyncProcessor() {
        log.info("processor init");
        AsyncItemProcessor<User, User> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(processor());
        asyncItemProcessor.setTaskExecutor(manualTaskExecutor);
        return asyncItemProcessor;
    }
    @Bean
    public UsersItemProcessor elasticProcessor() {
        log.info("processor init");
        return new UsersItemProcessor();
    }
    @Bean
    public ItemProcessor<User, Future<User>> asyncElasticProcessor() {
        log.info("async processor init");
        AsyncItemProcessor<User, User> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(elasticProcessor());
        asyncItemProcessor.setTaskExecutor(manualTaskExecutor);
        return asyncItemProcessor;
    }



    @Bean
    public ItemWriter<User> itemSqlWriter(){
        return sqlItemWriter;
    }
    @Bean
    public ItemWriter<Future<User>> asyncSqlItemWriter(){
        AsyncItemWriter<User> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(itemSqlWriter());
        return asyncItemWriter;
    }
    @Bean
    public ItemWriter<User> itemElasticWriter(){
        return elasticSearchWriter;
    }
    @Bean
    public ItemWriter<Future<User>> asyncElasticItemWriter(){
        AsyncItemWriter<User> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(itemElasticWriter());
        return asyncItemWriter;
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws MalformedURLException {
        log.info("step1 init");
        return new StepBuilder("step1: from file to sql", jobRepository)
                .<User, Future<User>>chunk(chunkSize, transactionManager)
                .reader(fileReader())
                .processor(asyncProcessor())
                .writer(asyncSqlItemWriter())
                .build();
    }
    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("step2 init");
        return new StepBuilder("step2: from sql to elasticsearch", jobRepository)
                .<User, Future<User>>chunk(chunkSize, transactionManager)
                .reader(sqlReader())
                .processor(asyncElasticProcessor())
                .writer(asyncElasticItemWriter())
                .build();
    }
    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1, Step step2, JobCompletionNotificationListener listener) {
        log.info("importUserJob init");
        return new JobBuilder("importUserJob", jobRepository)
                .listener(listener)
                .start(step1)
                .next(step2)
                .build();
    }


}
