package com.ycbd.photoservice.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ycbd.photoservice.services.PhotoService;
import com.ycbd.photoservice.services.SchedulerService;
import com.ycbd.photoservice.tools.ResultData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
@Api(value = "photo", tags = "图片处理模块")
@RequestMapping("api/")
@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class PhotoController {
    @Resource
    protected PhotoService pService;
    @Resource
    protected SchedulerService scheduleJob;

    @ApiOperation(value = "指定目录文件信息", notes = "获取指定目录文件信息")
    @PostMapping(value = "/getFileInfoByPath")
    @ResponseBody
    public ResultData<List<Map<String, Object>>> getFileInfoByPath(String path) throws IOException {
        return ResultData.success(pService.getFileInfoByPath(path,""));
    }
    @ApiOperation(value = "定时任务增加", notes = "获取指定目录文件信息")
    @PostMapping(value = "/scheduleJob")
    @ResponseBody
    public ResultData<Boolean> scheduleJob(Map<String,Object> taskMap) throws IOException {
        boolean result=false;
        try {
            scheduleJob.addJob(taskMap);
            result=true;
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ResultData.success(result);
    }
    
}
