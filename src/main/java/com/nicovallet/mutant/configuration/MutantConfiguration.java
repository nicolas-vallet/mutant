package com.nicovallet.mutant.configuration;

import com.nicovallet.mutant.controller.ControllerPkg;
import com.nicovallet.mutant.entity.EntityPkg;
import com.nicovallet.mutant.repository.RepositoryPkg;
import com.nicovallet.mutant.service.ServicePkg;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {ControllerPkg.class, ServicePkg.class,
        RepositoryPkg.class, EntityPkg.class})
public class MutantConfiguration {

    public static final String STATS_CACHE_NAME = "STATS";
}