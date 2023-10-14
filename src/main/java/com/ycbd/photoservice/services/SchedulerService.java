package com.ycbd.photoservice.services;

import java.util.Map;

import javax.annotation.Resource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import com.ycbd.photoservice.quartz.QueryFileJob;

import cn.hutool.core.map.MapUtil;


@Service
public class SchedulerService {
    @Resource
    protected   Scheduler scheduler;

    public void addJob(Map<String,Object> taskMap) throws SchedulerException {
        JobKey jobKey = new JobKey(MapUtil.getStr(taskMap, "JobName"),MapUtil.getStr(taskMap, "JobGroup"));
        // 如果存在这个任务，则删除
        if(scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
        JobDetail jobDetail =null;
        jobDetail = JobBuilder.newJob(QueryFileJob.class)
                        .withIdentity(jobKey)
                        .usingJobData("pathString",MapUtil.getStr(taskMap, "pathString"))
                        .usingJobData("UserDevices",MapUtil.getStr(taskMap, "UserDevices"))
                        .build();

        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(MapUtil.getStr(taskMap, "Cron"));
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(MapUtil.getStr(taskMap, "TriggerName"),MapUtil.getStr(taskMap, "TriggerGroup"))
                .withSchedule(cronScheduleBuilder).build();
        scheduler.scheduleJob(jobDetail,trigger);
    }

    public void removeJob(String jobName, String jobGroup,String triggerName,String triggerGroup) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        Trigger trigger =  scheduler.getTrigger(triggerKey);
        if (trigger == null) {
            return;
        }
        // 停止触发器
        scheduler.pauseTrigger(triggerKey);
        // 移除触发器
        scheduler.unscheduleJob(triggerKey);
        // 删除任务
        scheduler.deleteJob(jobKey);
    }

    public void setScheduler(Scheduler scheduler2) {
    }
    
}
