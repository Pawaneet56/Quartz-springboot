
package com.example.quartz.component;


import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JobScheduleCreator {

    public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable, ApplicationContext context,String jobName,String jobGroup){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setApplicationContext(context);
        factoryBean.setName(jobName);
        factoryBean.setGroup(jobGroup);
        factoryBean.setDurability(isDurable);
        factoryBean.setJobClass(jobClass);

        JobDataMap jobDataMap=new JobDataMap();
        jobDataMap.put(jobName+jobGroup,jobClass.getName());
        factoryBean.setJobDataMap(jobDataMap);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
    public CronTrigger createCronTrigger(String triggerName, Date startTime, String cronExpression,int misfireInstruction){

        CronTriggerFactoryBean factoryBean=new CronTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setMisfireInstruction(misfireInstruction);
        try {
            factoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return factoryBean.getObject();
    }
    public SimpleTrigger createSimpleTrigger(String triggerName, Date startTime, Long repeatTime, int misfireInstruction){

        SimpleTriggerFactoryBean factoryBean=new SimpleTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
        factoryBean.setRepeatInterval(repeatTime);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        factoryBean.setMisfireInstruction(misfireInstruction);
        factoryBean.afterPropertiesSet();

        return factoryBean.getObject();
    }
}
