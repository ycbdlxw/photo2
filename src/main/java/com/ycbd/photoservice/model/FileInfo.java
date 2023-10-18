package com.ycbd.photoservice.model;

import java.io.File;

import lombok.Data;

@Data
public class FileInfo {
    //文件名称
    private String fileName;
    //图片绝对路径
   // private String filePath;
    public String getFilePath(String root) {
        return root +File.separator+ relativePath;
    }
     public String getFileNamePath(String root) {
        return root + relativePath+File.separator+fileName;
    }
    //文件的相对路径
    private String relativePath;
    //文件类型
    private String fileType;
    //文件大小
    private String fileSize;
    //图片或是视频展示网址
     public String getFileUrl(String root) {
        return root+ relativePath+fileName;
    }
    private String fileMd5;
    //目标文件目录路径
    public String getTargePath(String root) {
        return root+File.separator+"thumbnails"+File.separator + relativePath;
    }
    //目标文件
    public String getTargeFile(String root) {
        if(relativePath.startsWith("/"))
        return root+File.separator+"thumbnails" + relativePath+File.separator+fileName;

        else
        return root+File.separator+"thumbnails"+File.separator + relativePath+File.separator+fileName;
    }
    //文件扩展名
    private String fileExt;
    //图片视频缩略图路径 
    private String thumbnails;
    //当前目录
    private String currentDir;
    //当前日期
    private String currentDate;
    //当前位置
    private String currentLocation;
    private String title;
    private String desc;
    private String GPSFlag;
    private String rooString;
    
}
