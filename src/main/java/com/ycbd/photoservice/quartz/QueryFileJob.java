package com.ycbd.photoservice.quartz;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ycbd.photoservice.services.PhotoService;

public class QueryFileJob implements Job {
    
    private PhotoService photoService; // 请确保您已经注入了PhotoService
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        String pathString = jobDataMap.getString("pathString");
        List<Map<String, Object>> fileInfo = photoService.getFileInfoByPath(pathString, "");
        // 执行其他逻辑
    }
}
