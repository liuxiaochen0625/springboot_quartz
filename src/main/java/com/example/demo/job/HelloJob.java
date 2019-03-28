package com.example.demo.job;

import java.util.Date;

import com.example.demo.service.IJobAndTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class HelloJob implements BaseJob {

    private static Logger _log = LoggerFactory.getLogger(HelloJob.class);

    @Autowired
    private IJobAndTriggerService service;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        _log.error("Hello Job执行时间: " + new Date());
        System.out.println(service.getJobAndTriggerDetails(1,10));

    }
}
