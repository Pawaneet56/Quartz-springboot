package com.example.quartz.service;

import com.example.quartz.component.JobScheduleCreator;
import com.example.quartz.entity.SchedulerJobInfo;
import com.example.quartz.job.SimpleCronJob;
import com.example.quartz.repository.SchedulerRepository;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.quartz.TriggerKey;
import java.util.Date;

@Service
@Transactional
public class SchedulerJobService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerJobService.class);
    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private SchedulerRepository schedulerRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JobScheduleCreator scheduleCreator;

    public void saveOrUpdate(SchedulerJobInfo schedulerJobInfo) throws ClassNotFoundException {
        if(!schedulerJobInfo.getCronExpression().isEmpty()){
            schedulerJobInfo.setJobClass(SimpleCronJob.class.getName());
            schedulerJobInfo.setCronJob(true);
        }
        else{
            schedulerJobInfo.setJobClass(SimpleCronJob.class.getName());
            schedulerJobInfo.setCronJob(false);
            schedulerJobInfo.setRepeatTime((long) 1);
        }
        if(StringUtils.isEmpty(schedulerJobInfo.getJobId())){
            log.info("Job Info: {}", schedulerJobInfo);
            scheduleNewJob(schedulerJobInfo);
        }
        else{
            updateScheduleJob(schedulerJobInfo);
        }
        schedulerJobInfo.setDesc("i am job number " + schedulerJobInfo.getJobId());
        schedulerJobInfo.setInterfaceName("interface_" + schedulerJobInfo.getJobId());
        log.info(">>>>> jobName = [{}] created.", schedulerJobInfo.getJobName());

    }
    private void scheduleNewJob(SchedulerJobInfo jobInfo) {
        Scheduler scheduler= schedulerFactoryBean.getScheduler();
        try {
            JobDetail jobDetail= JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass())).withIdentity(jobInfo.getJobName(),jobInfo.getJobGroup()).build();
            if (!scheduler.checkExists(jobDetail.getKey())) {
                jobDetail = scheduleCreator.createJob(
                        (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()), false, context,
                        jobInfo.getJobName(), jobInfo.getJobGroup());
                Trigger trigger;
                if (jobInfo.getCronJob()) {
                    trigger = scheduleCreator.createCronTrigger(
                            jobInfo.getJobName(),
                            new Date(),
                            jobInfo.getCronExpression(),
                            SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                } else {
                    trigger = scheduleCreator.createSimpleTrigger(
                            jobInfo.getJobName(),
                            new Date(),
                            jobInfo.getRepeatTime(),SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                }
                scheduler.scheduleJob(jobDetail,trigger);
                jobInfo.setJobStatus("SCHEDULED");
                schedulerRepository.save(jobInfo);
                log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " scheduled.");
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
            }
        }catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateScheduleJob(SchedulerJobInfo jobInfo){
        Trigger newTrigger;
        if(jobInfo.getCronJob()){
            newTrigger=scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(), jobInfo.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        }
        else{
            newTrigger=scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(), jobInfo.getRepeatTime(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        }
        try{
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobInfo.getJobName()),newTrigger);
            jobInfo.setJobStatus("EDITED & SCHEDULED");
            schedulerRepository.save(jobInfo);
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " updated and scheduled.");
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }
    public boolean startJobNow(SchedulerJobInfo schedulerJobInfo){
        try{
            SchedulerJobInfo jobInfo=schedulerRepository.findByJobName(schedulerJobInfo.getJobName());
            jobInfo.setJobStatus("SCHEDULED & STARTED");
            schedulerRepository.save(jobInfo);
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(schedulerJobInfo.getJobName(),schedulerJobInfo.getJobGroup()));
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " scheduled and started now.");
            return true;
        }
        catch(SchedulerException e){
            log.error("Failed to start new job - {}", schedulerJobInfo.getJobName(), e);
            return false;
        }
    }
    public boolean pauseJob(SchedulerJobInfo jobInfo){
        try{
            SchedulerJobInfo existingJobInfo=schedulerRepository.findByJobName(jobInfo.getJobName());
            existingJobInfo.setJobStatus("PAUSED");
            schedulerRepository.save(existingJobInfo);
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(jobInfo.getJobName(),jobInfo.getJobGroup()));
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " paused.");
            return true;
        }
        catch(SchedulerException e){
            log.error("Failed to pause job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }
    public boolean resumeJob(SchedulerJobInfo jobInfo){
        try{
            SchedulerJobInfo existingJobInfo=schedulerRepository.findByJobName(jobInfo.getJobName());
            existingJobInfo.setJobStatus("RESUMED");
            schedulerRepository.save(existingJobInfo);
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(jobInfo.getJobName(),jobInfo.getJobGroup()));
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " resumed.");
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to resume job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }
    public boolean deleteJob(SchedulerJobInfo jobInfo) {
        try{
            SchedulerJobInfo existingJobInfo=schedulerRepository.findByJobName(jobInfo.getJobName());
            schedulerRepository.delete(existingJobInfo);
            schedulerFactoryBean.getScheduler().deleteJob(new JobKey(jobInfo.getJobName(),jobInfo.getJobGroup()));
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " deleted.");
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to delete job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }
}
