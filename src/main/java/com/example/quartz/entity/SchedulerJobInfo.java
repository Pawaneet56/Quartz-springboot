package com.example.quartz.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class SchedulerJobInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String jobId;
    private String jobName;
    private String jobGroup;
    private String jobStatus;
    private String jobClass;
    private String cronExpression;
    private String desc;
    private String interfaceName;
    private Long repeatTime;
    private Boolean cronJob;
}
