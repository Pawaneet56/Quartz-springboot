package com.example.quartz.entity;

import lombok.Data;

@Data
public class SchedulerJobInfo {
    private String jobId;
    private String jobName;
    private String jobClass;
    private String jobGroup;
    private String jobStatus;
    private String cronExpression;
    private String desc;
    private String interfaceName;
    private Long repeatTime;
    private Boolean cronJob;
}
