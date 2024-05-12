package com.example.quartz.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

public class SchedulerJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
    private AutowireCapableBeanFactory beanFactory;
    @Override
    public void setApplicationContext(final ApplicationContext context){
        beanFactory= context.getAutowireCapableBeanFactory();
    }

    @Autowired
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception{
        final Object job=super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}
