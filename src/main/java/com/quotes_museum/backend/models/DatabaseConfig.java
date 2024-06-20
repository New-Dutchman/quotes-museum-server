package com.quotes_museum.backend.models;

import com.quotes_museum.backend.models.quotes.QuotesRepository;
import com.quotes_museum.backend.models.users.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

@PropertySource("application.properties")
@Configuration
@ComponentScan
public class DatabaseConfig {

    @Value("${datasource.url}")
    private String datasourceUrl;

    @Value("${datasource.username}")
    private String databaseUser;

    @Value("${datasource.password}")
    private String databasePassword;

    @Bean
    public QuotesRepository quotesRepository(){
        return new QuotesRepository(dataSource());
    }

    @Bean
    public UsersRepository usersRepository() {
        return new UsersRepository(dataSource());
    }

    @Bean
    @Scope("singleton")
    public DataSource dataSource(){
        PGSimpleDataSource ds = new PGSimpleDataSource();

        ds.setUrl(datasourceUrl);
        ds.setUser(databaseUser);
        ds.setPassword(databasePassword);

        return ds;
    }

}
