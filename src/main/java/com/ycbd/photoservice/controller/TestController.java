package com.ycbd.photoservice.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.Resources;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ycbd.photoservice.model.FileInfo;
import com.ycbd.photoservice.services.CmdService;
import com.ycbd.photoservice.services.PhotoService;
import com.ycbd.photoservice.services.SystemService;
import com.ycbd.photoservice.tools.ResultData;
import com.ycbd.photoservice.tools.Tools;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api(value = "test", tags = "测试模块")
@RequestMapping("test/")
@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class TestController {
    @Resource
    protected CmdService service;
    @Resource
    protected PhotoService photoservice;


    @ApiOperation(value = "exiv2", notes = "exiv2")
    @RequestMapping(value = "/exiv2", method = RequestMethod.POST)
    public ResultData<Map<String,String>> exiv2(String fileString,String keys) {

     return ResultData.success(service.getMetaObjValue(fileString,keys));

    }

     @ApiOperation(value = "fileinfo", notes = "fileinfo")
    @RequestMapping(value = "/fileinfo", method = RequestMethod.POST)
    public ResultData<FileInfo> fileinfo(String fileString,String driveString) {
        //"/Volumes/homes"

       FileInfo fileinfo= Tools.getListString(fileString,driveString);

     return ResultData.success(fileinfo);

    }
      @ApiOperation(value = "fileinfoMap", notes = "fileinfoMap")
    @RequestMapping(value = "/fileinfoMap", method = RequestMethod.POST)
    public ResultData<Map<String, Object>> fileinfoMap(String fileString) {

       Map<String, Object> fileinfo= photoservice.getMapData(fileString);

     return ResultData.success(fileinfo);

    }
    @ApiOperation(value = "fileSaveDb", notes = "fileSaveDb")
    @RequestMapping(value = "/fileSaveDb", method = RequestMethod.POST)
    public ResultData<Map<String, Object>> fileSaveDb(String fileString) {

       Map<String, Object> fileinfo= photoservice.getMapData(fileString);
       List<Map<String, Object>> saveMapList = new ArrayList<>();
       saveMapList.add(fileinfo);

       int count=photoservice.saveDataByList(saveMapList);
       fileinfo.put("saveCount", count);

     return ResultData.success(fileinfo);

    }


    @ApiOperation(value = "ffmpeg", notes = "ffmpeg")
    @RequestMapping(value = "/ffmpeg", method = RequestMethod.POST)
    public ResultData<String> ffmpeg(String videoFilePath,String savePath) {
        String result="";
        if(service.getVideoImage(videoFilePath,savePath))
            result=videoFilePath+" 视频截图成功，截图保存在："+savePath;
        else
            result=videoFilePath+" 视频截图不成功";

           

         return ResultData.success(result);
    }


    @ApiOperation(value = "testSqlite", notes = "")
    @RequestMapping(value = "/testSqlite", method = RequestMethod.POST)
    public ResultData<List<String>> testSqlite(String dbString) {
        Connection conn = null;
        List<String> result=new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+dbString);
            result.add(dbString+"Opened database successfully");
            System.out.println("Opened database successfully");
        } catch ( Exception e ) {
            result.add(dbString+e.getClass().getName() + ": " + e.getMessage());
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                 result.add(dbString+e.getClass().getName() + ": " + e.getMessage());
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return ResultData.success(result);
    }


}
