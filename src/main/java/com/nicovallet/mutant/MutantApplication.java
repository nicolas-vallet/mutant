package com.nicovallet.mutant;

import com.nicovallet.mutant.configuration.MutantConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = MutantConfiguration.class)
@EnableCaching
public class MutantApplication {
    public static void main(String[] args) {
        SpringApplication.run(MutantApplication.class, args);
    }
}