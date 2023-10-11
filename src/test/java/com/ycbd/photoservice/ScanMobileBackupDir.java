package com.ycbd.photoservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

public class ScanMobileBackupDir {
     private static final String ROOT_DIR = "/Volumes/homes/";
private List<String> scanMobileBackupDir(String user, String device) throws IOException {
    List<String> result = new ArrayList<>();
    // 指定时间为当前时间的前2小时
    // 获取当前时间
    long currentTime = System.currentTimeMillis();
    long targetTime = currentTime - 24 * 60 * 60 * 1000;
    // 获取指定用户的所有设备目录
    Path userHomePath = Paths.get(ROOT_DIR, user);
    Path deviceDirPath = null;
    if (StrUtil.isBlank(device)) {
        // 如果设备为空，则查询指定用户下所有设备
        deviceDirPath = Files.walk(userHomePath, 1).filter(Files::isDirectory).findAny().get();
    } else {
        // 如果设备不为空，则查询指定用户指定设备目录
        deviceDirPath = userHomePath.resolve("Photos").resolve("MobileBackup").resolve(device);
    }
     Files.walk(deviceDirPath, 3).filter(Files::isDirectory).forEach(path -> {
            // 获取目录的最后修改时间
            try {
                 System.out.println(path.toString()+" "+Files.getLastModifiedTime(path).toString());

                    long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
                    // Rest of the code
                     // 如果目录的最后修改时间大于指定时间，则该目录下有新增文件
                    if (lastModifiedTime > targetTime) {
                        result.add(path.toString());
                    }
                } catch (IOException e) {
                    // Handle the exception here
                    e.printStackTrace(); // Or any other appropriate error handling
                }
           
        });
        Path CameraDirPath=deviceDirPath.resolve("DCIM").resolve("Camera");
        Files.walk(CameraDirPath, 2).filter(Files::isDirectory).forEach(path -> {
            // 获取目录的最后修改时间
            try {
                 System.out.println(path.toString()+" "+Files.getLastModifiedTime(path).toString());

                    long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
                    // Rest of the code
                     // 如果目录的最后修改时间大于指定时间，则该目录下有新增文件
                    if (lastModifiedTime > targetTime) {
                        result.add(path.toString());
                    }
                } catch (IOException e) {
                    // Handle the exception here
                    e.printStackTrace(); // Or any other appropriate error handling
                }
           
        });
        //将所有符合条件的目录下的所有符合指定时间内的文件返回
        List<String> fileList=new ArrayList<>();
        result.stream().forEach(it -> {
        try {
            Files.walk(Paths.get(it), 1).forEach(path -> {
                        try {
                            System.out.println(path.toString() + " " + Files.getLastModifiedTime(path).toString());
                            long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
                    
                     // 如果文件的最后修改时间大于指定时间，新增文件加入返回数组
                            if (lastModifiedTime > targetTime && !Files.isDirectory(path)) {
                                fileList.add(path.toString());
                            }
                        } catch (IOException e) { // Catch the IOException
                            e.printStackTrace(); // Handle the exception here or use appropriate error handling
                        }
                    });
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

          return fileList;
   
    // Add the missing closing brace here
    }
   

 public static void main(String[] args) throws IOException {
        ScanMobileBackupDir scanMobileBackupDir = new ScanMobileBackupDir();
        List<String> result = new ArrayList<>();

        // 查询指定用户下所有设备
        // List<String> result = scanMobileBackupDir.scanMobileBackupDir("salina", "", "2023", "10");
        // System.out.println(result);

        // 查询指定用户指定设备
        result = scanMobileBackupDir.scanMobileBackupDir("salina", "EBG-AN00");
        System.out.println(result);
       
    }

}

   



   

