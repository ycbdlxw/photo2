package com.ycbd.photoservice.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ycbd.photoservice.tools.Tools;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;

@Service
public class CmdService {
     @Value("${system.script:/Users/ycbd/exiv2}")
    String script;

       public Map<String,String> getMetaObjValue(String filePath,String keys){
        Map<String,String> resultMap=new HashMap<>();
        List<String> command = new ArrayList<>();
            command.add("exiv2");
            if(!keys.contains(",")){
            command.add("-pa");
            command.add("--grep");
            command.add(keys);  
            }
            else
            {
                String[] keyArr=keys.split(",");
                for(String key:keyArr){
                    command.add("-pa");
                    command.add("--grep");
                    command.add(key);
                }
            }
            command.add(filePath);
            try{
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
           System.out.println("pid: "+process.pid()); ;
            String line;
            while ((line = reader.readLine()) != null) {
                      List<String> list= Tools.getTags(StrUtil.split(line," ")) ;
                      if(list.size()>2){
                        // System.out.println(line);
                        for(String key:keys.split(",")){
                            if(line.contains(key)){
                               resultMap.put(key, String.join(" ", ListUtil.sub(list,3,list.size())).trim());
                            }
                           
                        }
                     }  
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(filePath+" 命令行操作执行成功");
            } else {
                System.out.println(filePath+"命令行操作执行失败");
            }
            return resultMap;
            }catch (Exception e){
                return resultMap;
            }


    }


    public String getMetaMd5(String filePath){
        String result=RuntimeUtil.execForStr("exiv2 -pa "+filePath);
        if(StrUtil.isBlank(result))return "";
        MD5 md5=new MD5();
        String md5Str = md5.digestHex(result);
        return md5Str;

    }

    public boolean getVideoImage(String filePath, String targeFile) {
        return false;
    }

      public List<String> ffmpeg(String videoPath, String savePath) {
        String cmdstr = "sh " + script + "/ffmpeg.sh %s %s";
        cmdstr = String.format(cmdstr, videoPath, savePath);
        List<String> result = RuntimeUtil.execForLines(cmdstr);
        if (result.isEmpty()) {
            return new ArrayList<>();
        }
        return result;
    }
    
}
