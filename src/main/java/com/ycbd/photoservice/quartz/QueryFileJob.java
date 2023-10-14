package com.ycbd.photoservice.quartz;

import java.util.List;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ycbd.photoservice.services.PhotoService;

import cn.hutool.core.util.StrUtil;

public class QueryFileJob implements Job {
    
    private final PhotoService photoService;

    public QueryFileJob(PhotoService photoService) {
        this.photoService = photoService;
    }
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        String pathString = jobDataMap.getString("pathString");
        String UserDevices= jobDataMap.getString("UserDevices");
        System.out.println(UserDevices+" "+pathString+" 执行任务开始！");
        List<String> userDeviceList=StrUtil.split(UserDevices,",");
        int saveCount= photoService.saveAddData(pathString,userDeviceList);
        System.out.println(UserDevices+"新增保存 "+saveCount+" 数据成功！");
        // 执行其他逻辑
    }
}
