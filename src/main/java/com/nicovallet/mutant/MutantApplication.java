package com.nicovallet.mutant;

import com.nicovallet.mutant.configuration.MutantConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackageClasses = MutantConfiguration.class)
@EnableCaching
@EnableTransactionManagement
public class MutantApplication {
    public static void main(String[] args) {
        SpringApplication.run(MutantApplication.class, args);
    }
}
