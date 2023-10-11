package com.ycbd.photoservice;

import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.ycbd.photoservice.services.SchedulerService;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

public class SchedulerServiceTest {

    @Test
    public void testAddJob() throws SchedulerException {
        // 创建测试所需的对象和数据
        Scheduler scheduler = mock(Scheduler.class);
        SchedulerService schedulerService = new SchedulerService();
        schedulerService.setScheduler(scheduler);
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("JobName", "testJob");
        taskMap.put("JobGroup", "testGroup");
        taskMap.put("TriggerName", "testJob");
        taskMap.put("TriggerGroup", "testGroup");
        taskMap.put("pathString", "/path/to/file");
        taskMap.put("Cron", "0 0 12 * * ?");

        // 调用被测试的方法
        schedulerService.addJob(taskMap);

        // 验证方法是否按预期调用
        // verify(scheduler).checkExists(any(JobKey.class));
        // verify(scheduler).deleteJob(any(JobKey.class));
        // verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }
}
