package com.ycbd.photoservice.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ycbd.photoservice.services.ExivService;
import com.ycbd.photoservice.services.PhotoService;
import com.ycbd.photoservice.tools.ResultData;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
@Api(value = "photo", tags = "图片处理模块")
@RequestMapping("api/")
@RestController
@CrossOrigin(origins = "*")
public class PhotoController {
    @Resource
    protected PhotoService pService;

    @Resource
    protected ExivService exivService;

    @ApiOperation(value = "指定目录所有用户文件信息", notes = "获取指定目录所有用户文件信息并保存到数据")
    @PostMapping(value = "/getFileInfoByPath")
    @ResponseBody
    public ResultData<List<Map<String, Object>>> getFileInfoByPath(String path) throws IOException {
        List<String> UserDevices=new ArrayList<>();
        UserDevices.add("salina|EBG-AN00");
        UserDevices.add("ycbd|M2002J9E");

        return ResultData.success(pService.getFileInfoByPath(UserDevices,""));
    }
    @ApiOperation(value = "指定用户目录文件信息", notes = "获取指定用户目录文件信息并保存到数据库")
    @PostMapping(value = "/getUserFile")
    @ResponseBody
    public ResultData<List<Map<String, Object>>> getUserFile(String UserDevice) throws IOException {
        List<String> UserDevices=StrUtil.split(UserDevice,","); 
        return ResultData.success(pService.getFileInfoByPath(UserDevices,""));
    }
    @ApiOperation(value = "指定用户目录文件信息", notes = "获取指定用户目录文件信息")
    @PostMapping(value = "/getUserDeviceFile")
    @ResponseBody
    public ResultData<List<Map<String, Object>>> getUserDeviceFile(String UserDevice,int pageNumber) throws IOException {
     return ResultData.success(pService.getFileInfoByUserDevice(UserDevice,20,pageNumber));
    }

     @ApiOperation(value = "指定用户目录文件信息记录总数", notes = "获取指定用户目录文件信息")
    @PostMapping(value = "/getTotal")
    @ResponseBody
    public ResultData<Integer> getTotal(String UserDevice) throws IOException {
     return ResultData.success(pService.getQueryTotal(UserDevice));
    }
   

    @ApiOperation(value = "addSubject", notes = "增加相片标题内容")
    @RequestMapping(value = "/addSubject", method = RequestMethod.POST)
    public ResultData<List<String>> addSubject(String filename,String subject){
        return ResultData.success(exivService.addSubject(filename,subject));
    }
    @ApiOperation(value = "updateTime", notes = "增加相片标题内容")
    @RequestMapping(value = "/updateTime", method = RequestMethod.POST)
    public ResultData<List<String>> updateTime(String filename,String dateTime){
        return ResultData.success(exivService.editDataTime(filename,dateTime));
    }
    @ApiOperation(value = "addLatitude", notes = "增加相片经度值")
    @RequestMapping(value = "/addLatitude", method = RequestMethod.POST)
    public ResultData<List<String>> addLatitude(String filename,int Latitude1,int Latitude2,int Latitude3){
        String[] LatitudeValue=new String[3];
        LatitudeValue[0]=Latitude1+"";
        LatitudeValue[1]=Latitude2+"";
        LatitudeValue[2]=Latitude3+"";
        return ResultData.success(exivService.addLatitudeInfo(filename,LatitudeValue));
    }
    @ApiOperation(value = "addLongitude", notes = "增加相片纬度值")
    @RequestMapping(value = "/addLongitude", method = RequestMethod.POST)
    public ResultData<List<String>> addLongitude(String filename,int Longitude1,int Longitude2,int Longitude3){
        String[] LongitudeValue=new String[3];//{"","",""};
        LongitudeValue[0]=Longitude1+"";
        LongitudeValue[1]=Longitude2+"";
        LongitudeValue[2]=Longitude3+"";
        return ResultData.success(exivService.addLongitudeInfo(filename,LongitudeValue));
    }
    @ApiOperation(value = "addGPS", notes = "增加相片GPS值")
    @RequestMapping(value = "/addGPS", method = RequestMethod.POST)
    public ResultData<List<String>> addGPS(String filename, @RequestBody Map<String,Object> gpsdata){
        return ResultData.success(exivService.addGPSInfo(filename,gpsdata));
    }

    @ApiOperation(value = "getGPS", notes = "相片GPS值")
    @RequestMapping(value = "/getGPS", method = RequestMethod.POST)
    public ResultData<Map<String,Object>> getGPS(String filename){
        return ResultData.success(exivService.getGPSInfo(filename));
    }

   
    
}
