package com.ycbd.photoservice.services;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.ycbd.photoservice.Mappers.PhotoMapper;
import com.ycbd.photoservice.tools.Tools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * @author lxw
 * com.ycbd.exiv2service.services
 * @create 2022/10/10 09:38
 * @description
 */
@Service
@Slf4j
public class ExivService {
    @Resource
    protected  CmdService cmdService;
    @Resource
    protected  PhotoMapper mapper;
    @Value("${system.script:/Users/ycbd/exiv2}")
    String script;
     @Value("${system.root:/Volumes/homes}")
    String root;
    public void getMetaDataInfo(String it, Map<String,Object> map){
      String dateTimeStr="";
        Map<String,String> metaMap=cmdService.getMetaObjValue(it,"subject,ImageDescription,DateTime,Exif.Image.Model,GPS");
        if(ObjectUtil.isNotNull(metaMap.get("subject")))
        map.put("title",  metaMap.get("subject").toString());
        else
        map.put("title", "");
        if(ObjectUtil.isNotNull(metaMap.get("ImageDescription")))
        map.put("desc", metaMap.get("ImageDescription").toString());
        else
        map.put("desc", "");
        if(ObjectUtil.isNotNull(metaMap.get("Exif.Image.Model")) && ObjectUtil.isEmpty(map.get("model")))
         map.put("model", metaMap.get("Exif.Image.Model").toString());
        //根据元数据的日期时间获取
         if(ObjectUtil.isNotNull(metaMap.get("DateTime"))){
            String dateTimeString= metaMap.get("DateTime").toString();
            dateTimeStr=Tools.convertToStandardDateTime(dateTimeString);
            
         }
          //根据文件名称日期时间获取
         if(StrUtil.isEmpty(dateTimeStr)){
             dateTimeStr=Tools.extractDateTime(map.get("filename").toString());
         }
          //根据文件创建日期时间获取
         if(StrUtil.isEmpty(dateTimeStr))
            {
                dateTimeStr=Tools.extractDateTimeByFile(map.get("filePath").toString());
            }
        //根据文件最后修改日期时间获取
        if(StrUtil.isEmpty(dateTimeStr)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            File itfile= FileUtil.file(it);
            dateTimeStr = sdf.format(FileUtil.lastModifiedTime(itfile));
        }
       
         map.put("currentDate", StrUtil.split(dateTimeStr, " ").get(0));
         map.put("shootingTime",dateTimeStr );
         map.put("GPSFlag", ObjectUtil.isNotNull(metaMap.get("GPS")));
         map.put("md5", cmdService.getMetaMd5(it));
    }
   
    /**
     *
     * @param filename
     * @param queryStr   如果为空，则获取所有的metadata数据，如果有值，则按此值从所有的metadata中进行筛选
     * @return           返回查询筛选后的相关数据
     */
    public List<String> getMetaData(String filename,String queryStr) {
        String cmdstr = "exiv2 -pa ";
        if(StrUtil.isEmpty(queryStr))
            cmdstr+= "\"" + filename + "\"";
        else
        {
             cmdstr="sh "+script+"/grep.sh %s %s";
            cmdstr=String.format(cmdstr,queryStr,filename);
        }

        List<String> result = RuntimeUtil.execForLines(cmdstr);
        return result;
    }
    public List<String> getMetaDataByCmd(String filename)  {
        List<String> result =new ArrayList<>();// RuntimeUtil.execForLines("exiv2 -pa " + "\"" + filename + "\"");
        List<String> command = new ArrayList<>();
        command.add("exiv2");
            command.add("-pa");
            command.add( filename );
        ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process;
            try {
                process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);    
            }
            int exitCode = process.waitFor();
            process.destroy();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch(InterruptedException ie){

            }
            
            return result;
    }

    /**
     * 根据文件获取的相关的内容，进行初始化数据库字典工作，完成表名，字段列名，字段类型的内容保存，为后期的
     * 自动建表提供数据
     * @param filename
     * @return
     */
    public List<String> initDict(String filename){
        String cmdstr="exiv2 -pa "+filename;
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        int total=0;
        for (String it:result ) {
            List<String> tags= Tools.getTags(StrUtil.split(it," ")) ;
            String tag=tags.get(0);
            List<String> table_columns=StrUtil.split(tag,".");
            String table="",column="";
            if(table_columns.get(0).equals("Exif")){
                table=table_columns.get(1);
            }
            else
            {
                table=table_columns.get(0);
            }
            column=StrUtil.split(tag,".").get(2);
            String querySql=" exif_name='"+column+"' and parent_name='"+table+"'";
            // if(!baseService.getIsExitByWhere("Exif_dict",querySql,"")) {
            //     String sqlStr = " insert into Exif_dict (exif_name,column_type,parent_name) value ('" + column + "','" + tags.get(1) + "','" + table + "')";
            //     int count = baseService.updateSql(sqlStr);
            //     total+=count;
            // }
            System.out.println(table+":"+column+" "+tags.get(1));
        }
        return result;
    }

    /**
     * 更新相关的文件的数字，原始日期时间
     * @param filename
     * @param dataTime  更新的日期时间
     * @return
     */
    public List<String> editDataTime(String filename,String dataTime){
        String cmdstr="sh "+script+"/editDateTime.sh %s %s";
        cmdstr=String.format(cmdstr,dataTime,filename);
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        if(result.isEmpty())return new ArrayList<>();
        result.add(cmdstr);
        return result;
    }

    /**
     *
     * @param filename   如果是文件名，则仅更改当前文件，如果是路径，则更改当前路径下的所有文件的名称
     * @return
     */
    public List<String> rename(String filename,String path){
        if(path.contains(" ")) {
            File dir=FileUtil.file(path);
            path = path.replace(" ", "_");
            FileUtil.rename(dir,path,true);
        }
        String cmdstr="sh "+script+"/rename.sh %s %s";
        if(StrUtil.isEmpty(filename) || filename.equals("jpg"))
            filename="*.jpg";
        else
            if(filename.equals("JPG"))
                filename="*.JPG";

        cmdstr=String.format(cmdstr,path,filename);
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        if(result.isEmpty())return new ArrayList<>();
        result.add(cmdstr);
        return result;
    }

    /**
     *    文件移动
     * @param filename  文件名称
     * @param path       目标路径
     * @return
     */
    public List<String> movefile(String filename,String path){
        File targetPath= FileUtil.file(path);
        if(!targetPath.exists())
             FileUtil.mkdir(path);
        String cmdstr="mv -n "+filename+" "+path;
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        if(result.isEmpty())return new ArrayList<>();
        result.add(cmdstr);
        return result;
    }

    /**
     *  增加相片内容，包括标题，相册名称，相片描述，位置信息
     * @param filename  保存的相片文件名称
     * @param subject   文件标题内容，可以多个，以,号隔开
     * @return
     */
    public List<String>  addContent(String filename,String content,String scriptname){
        String cmdstr="sh "+script+"/"+scriptname+".sh %s %s";
        cmdstr=String.format(cmdstr,content,filename);
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        if(result.isEmpty())return new ArrayList<>();
        result.add(cmdstr);
        return result;
    }
     
    
    public List<String>  trimSpace(String path){
        String cmdstr="sh "+script+"/trim.sh %s ";
        cmdstr=String.format(cmdstr,path);
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        if(result.isEmpty())return new ArrayList<>();
        result.add(cmdstr);
        return result;
    }
    /**
     *
     * @param filename        保存的相片文件名称
     * @param LatitudeValue   经度数据
     * @return
     */
    public List<String>  addLatitudeInfo(String filename,String[] LatitudeValue){//int Latitude1,int Latitude2,int Latitude3){
        String cmdstr="sh "+script+"/addLatitude.sh %s %s %s %s";
        cmdstr=String.format(cmdstr,LatitudeValue[0],LatitudeValue[1],LatitudeValue[2],filename);
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        if(result.isEmpty())return new ArrayList<>();
        result.add(cmdstr);
        return result;
    }

    /**
     *
     * @param filename        保存的相片文件名称
     * @param LongitudeValue  纬度数据
     * @return
     */
    public List<String>  addLongitudeInfo(String filename,String[] LongitudeValue){//int Longitude1,int Longitude2,int Longitude3){
        String cmdstr="sh "+script+"/addLongitude.sh %s %s %s %s";
        cmdstr=String.format(cmdstr,LongitudeValue[0],LongitudeValue[1],LongitudeValue[2],filename);
        List<String> result= RuntimeUtil.execForLines(cmdstr);
        if(result.isEmpty())return new ArrayList<>();
        result.add(cmdstr);
        return result;
    }

    /**
     *
     * @param filename  需要保存的文件名称
     * @param gpsdata   gps数据
     * @return
     */
    public List<String> addGPSInfo(String filename, Map<String,Object> gpsdata){
        String Latitude= MapUtil.getStr(gpsdata,"Latitude");
        String Longitude= MapUtil.getStr(gpsdata,"Longitude");
        String[] LatitudeValue=Latitude.split(",");
        String[] LongitudeValue=Longitude.split(",");
        List<String> LatitudeResult=addLatitudeInfo(filename,LatitudeValue);
        if(!LatitudeResult.isEmpty()){
            return LatitudeResult;
        }
        List<String> LongitudeReult=addLongitudeInfo(filename,LongitudeValue);
        if(LongitudeReult.isEmpty())
        return new ArrayList<>();
        return  LongitudeReult;
    }

    /**
     *  获取指定图片的gps相关信息，同时，也获取了当前相片的日期时间内容
     * @param filename  图片名称
     * @return
     */
    public Map<String, Object> getGPSInfo(String filename){
        Map<String,Object> result=new HashMap<>();
        List<String> metaResult=  getMetaData(filename,"GPSInfo");
        String value="";
        for (String it:metaResult ) {
            List<String> tags= Tools.getTags(StrUtil.split(it," ")) ;
            List<String > tagTitles=StrUtil.split(tags.get(0),".");
            if(tagTitles.size()<1 || !tagTitles.contains("GPSInfo"))continue;
            switch (tagTitles.get(2)){
                case "GPSLatitude":
                    value=tags.get(3).replace("deg","")+",";
                    value+=tags.get(4).replace("'","")+",";
                    value+=tags.get(5).replace("\"","");
                    result.put("Latitude",value);
                    break;
                case "GPSLongitude":
                    value=tags.get(3).replace("deg","")+",";
                    value+=tags.get(4).replace("'","")+",";
                    value+=tags.get(5).replace("\"","");
                    result.put("Longitude",value);
                    break;
//                case "GPSAltitude":
//                    value=tags.get(3).replace(" m","");
//                    result.put("Altitude",value);
//                    break;
            }
        }
        return result;

    }

    public  String getExifValue(String filename,String key){
        String result="";
        List<String> MakeResult=  getMetaData(filename,key);
        if(MakeResult.isEmpty())return "";
        String content="";
        for (String value:MakeResult
        ) {
            if(!StrUtil.subPre(value,4).equals("Exif"))continue;
            if(StrUtil.contains(value,key)) {
                content = value;
                break;
            }
        }
        List<String> contents = Tools.getTags(StrUtil.split(content, " "));
        for (int i=3;i<contents.size();i++){
            result +=contents.get(i)+"-";
        }
        result=StrUtil.subBefore(result,"-",true);
        return result;


    }
    public  String getModel(String filename){
        List<String> ModelResult=  getMetaData(filename,"Exif.Image.Model");
        String result="";
        if(ModelResult.isEmpty())return "";
        String modelContent="";
        for (String model:ModelResult
             ) {
            if(!StrUtil.subPre(model,4).equals("Exif"))continue;
            if(StrUtil.contains(model,"Exif.Image.Model")) {
                modelContent = model;
                break;
            }
        }
        List<String> content = Tools.getTags(StrUtil.split(modelContent, " "));
        for (int i=3;i<content.size();i++){
            result +=content.get(i)+"-";
        }
        result=StrUtil.subBefore(result,"-",true);
        return result;
    }
     public  String getTilte(String filename){
         List<String> ModelResult=  getMetaData(filename,"Xmp.dc.subject");
         if(ModelResult.size()>0)
         return ModelResult.get(0);
         return "";
     }


    public String getDataTime(String filename){
        if(StrUtil.isEmpty(filename)){
            log.info(filename+" is null!");
            return "";
        }
        File file=FileUtil.file(filename);
        if(!file.exists()){
            log.info(filename+" is not exit!");
            return "";
        }
        List<String> dateTimeResult=  getMetaData(filename,"DateTime");
        for (String dt:dateTimeResult ) {
            List<String> dateTimes = Tools.getTags(StrUtil.split(dt, " "));
            List<String> dateTime=StrUtil.split(dateTimes.get(0),".");
            if(dateTime.size()<2)continue;
            if(dateTimes.size()<4){
                log.error(filename+dateTimes);
                continue;
            }
            try {
                if(!dateTime.get(2).contains("DateTime"))continue;
            } catch (Exception e) {
                log.error(filename+e.getMessage());
            }
            return dateTimes.get(3)+" "+dateTimes.get(4);
        }
        return "";

    }
    
    public String getDescription(String filename) {
        List<String> ModelResult=  getMetaData(filename,"Xmp.dc.subject");
         if(ModelResult.size()>0)
         return ModelResult.get(0);
         return "";
    }

   // 在ExivService.java文件中添加以下方法
public List<Map<String, Object>> addInfoToFiles(String filenames, String content,int processingMode ) {
    List<Map<String, Object>> result = new ArrayList<>();
   

    if (StrUtil.isEmpty(filenames)) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", "文件路径不能为空");
        result.add(map);
        return result;
    }

    Map<String,Object> gpsdata=new HashMap<>();
    
    if(processingMode==1){
         if (content.endsWith(".jpg") || content.endsWith(".JPG"))
            gpsdata=getGPSInfo(content);
        else
        {
            int id=0;
          if(NumberUtil.isInteger(content)){
           id = Integer.parseInt(content);
           gpsdata= mapper.getGPSInfo(id);
          }
            
            
        }
        if(MapUtil.isEmpty(gpsdata))
        {
            Map<String, Object> map = new HashMap<>();
            map.put("error", "选择无有效的位置数据信息");
            result.add(map);
            return result;
        }
    }
     
    List<String> filenameList = StrUtil.split(filenames, ",");
    final Map<String,Object> gpsfiledata=gpsdata;
     List<String> updateFiles= new ArrayList<>();
    filenameList.forEach(it -> {
        if (it.endsWith(".jpg") || it.endsWith(".JPG")) {
            Map<String, Object> map = new HashMap<>();
            String fileString = root + it;
            List<String> excelResult = new ArrayList<>();
            String scriptName="";
            switch (processingMode) {
                case 1:
                //分为三种情况处理，上传文件，自我选择为文件名，下拉选择为下拉ID值  
                   excelResult = addGPSInfo(fileString, gpsfiledata);
                    break;
                case 2:
                    scriptName="addsubject";
                    break;
                case 3:  
                     scriptName="addAblum";
                    break;
                case 4:     
                    scriptName="addImageDescription";
                    break;
                default:
                    break;
            }
            if(processingMode>1)
               excelResult = addContent(fileString, content, scriptName);
           
            map.put(it, excelResult);
            if(excelResult.size()<1)
                updateFiles.add(fileString);
            result.add(map);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put(it, "非jpg文件暂时不能处理");
            result.add(map);
        }
    });
    if(updateFiles.size()>0)
    {
        Map<String, Object> map = new HashMap<>();
        String updateSql=updateContentSql(updateFiles,content,processingMode);
         map.put("updateSql", updateSql);
        result.add(map);
    }
    return result;
}
public String updateContentSql(List<String> filepathList, String content, int processingMode) {

    // 更新保存数据库
  
        List<String> updatedFilepaths = filepathList.stream()
                .map(filepath -> "'" + filepath + "'")
                .collect(Collectors.toList());
        String updatedFilepathsStr = String.join(",", updatedFilepaths);

        String updateSql = "";
        switch (processingMode) {
            case 1:
                 updateSql = "UPDATE fileinfo SET GPSFlag = 1 WHERE filepath IN (" + updatedFilepathsStr + ")";
                break;
            case 2:
                updateSql = "UPDATE fileinfo SET title = '" + content + "' WHERE filepath IN (" + updatedFilepathsStr + ")";
                break;
            case 3:
                updateSql = "UPDATE fileinfo SET ablum = '" + content + "' WHERE filepath IN (" + updatedFilepathsStr + ")";
                break;
            case 4:
                updateSql = "UPDATE fileinfo SET `desc` = '" + content + "' WHERE filepath IN (" + updatedFilepathsStr + ")";
                break;
            default:
                break;
        }
    
    return updateSql;
}
}
