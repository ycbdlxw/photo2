package com.ycbd.photoservice.model;

import lombok.Data;

@Data
public class FileInfo {
    //文件名称
    private String fileName;
    //图片绝对路径
    private String filePath;
    //文件类型
    private String fileType;
    //文件大小
    private String fileSize;
    //图片或是视频展示网址
    private String fileUrl;
    private String fileMd5;
    //目标文件目录路径
    private String targePath;
    //目标文件
    private String targeFile;
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
    
}
