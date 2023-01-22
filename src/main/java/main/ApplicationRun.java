package main;

import main.config.ConfigData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import service.ParseSiteOrPage;


@SpringBootApplication
@ComponentScan(basePackages = {"main","service"})
@EntityScan("model")
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories("repositories")

public class ApplicationRun {



    public static void main(String[] args)  {

        SpringApplication.run(ApplicationRun.class, args);


    }


}
