package com.spring.boot.carro.reservas.configuration.app;

import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {
    private final DataSource dataSource;
    private final ApplicationContext applicationContext;

    public QuartzConfig(DataSource dataSource, ApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.applicationContext = applicationContext;
    }

    @Bean
    public JobFactory jobFactory() {
        // Esta pieza es la que permite que Quartz entienda @Autowired
        AutocompleteJobFactory jobFactory = new AutocompleteJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, QuartzProperties quartzProperties) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory); // IMPORTANTE: Le asignamos la fábrica inteligente

        // Al declarar manualmente este SchedulerFactoryBean, la autoconfiguracion de Quartz se desactiva
        // y las propiedades spring.quartz.properties.* NO se aplican solas. Las cargamos aqui para que
        // SI se use el PostgreSQLDelegate (driverDelegateClass) y no el StdJDBCDelegate por defecto,
        // que no sabe leer los BYTEA de las tablas QRTZ_.
        Properties props = new Properties();
        props.putAll(quartzProperties.getProperties());
        factory.setQuartzProperties(props);

        factory.setSchedulerName("ReservaScheduler");
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);
        return factory;
    }

    // Clase interna para dar superpoderes a Quartz
    public final class AutocompleteJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
        private transient AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) {
            beanFactory = applicationContext.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job); // Inyecta las dependencias de Spring en el Job
            return job;
        }
    }
}
