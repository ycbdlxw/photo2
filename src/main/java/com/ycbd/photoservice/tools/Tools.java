package com.ycbd.photoservice.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.ycbd.photoservice.model.FileInfo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import net.coobird.thumbnailator.Thumbnails;



public class Tools {

       public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

     /**
     *
     * @param tags 标签去除内部空格
     * @return
     */
    public static List<String> getTags(List<String> tags) {
        List<String> tagstrim = new ArrayList<>();
        tags.forEach(g -> {
            if (!StrUtil.isEmpty(g))
                tagstrim.add(g);
        });
        return tagstrim;
    }

    public static String convertToStandardDateTime(String dateString) {
        Properties properties = new Properties();
        try (InputStream inputStream = ResourceUtil.getStream("dateformats.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        for (String key : properties.stringPropertyNames()) {
            String pattern = properties.getProperty(key);
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return dateTime.format(outputFormatter);
            } catch (DateTimeParseException e) {
                // Ignore and continue to the next formatter
            }
        }

        // Return empty string if none of the formatters match
        return "";
    }

    // 根据文件名称获取对应的日期时间
    public static String extractDateTime(String inputString) {
        String dateTimePattern = "\\d{8}_\\d{6}";
        String dateTimeString = inputString.replaceAll(".*(" + dateTimePattern + ").*", "$1");
        if (dateTimeString.equals(inputString))
            return "";
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            return "";
        }

    }

     public static String extractDateTimeByFile(String filepath) {
        try {
            // 获取文件路径
            Path file = Path.of(filepath);
            // 读取文件属性
            BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
            // 获取文件创建时间
            LocalDateTime dateTime = LocalDateTime.ofInstant(attributes.creationTime().toInstant(),
                    ZoneId.systemDefault());
            // 设置日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 格式化日期时间
            String formattedDateTime = dateTime.format(formatter);
            return formattedDateTime;
        } catch (Exception e) {
            return "";
        }
    }
    // 根据输入的字符串，根目录字符，进行拆分为：前段目录，后段目录，文件名，文件扩展名四个部分
    // 1:文件名称 2:图片绝对路径 3:图片缩略图路径
    public static FileInfo getListString(String filePath, String rootString) {
        // 创建FileInfo对象
        FileInfo fileInfo = new FileInfo();
        if(!StrUtil.endWith(rootString,File.separatorChar))
            rootString =rootString+File.separator;
        fileInfo.setRelativePath(StrUtil.replace(StrUtil.subBefore(filePath,File.separatorChar, true),rootString,""));
        // 获取文件名
        String str = FileNameUtil.getName(filePath);
        // 设置文件名
        fileInfo.setFileName(str);
        // 获取文件扩展名
        fileInfo.setFileExt(FileNameUtil.getSuffix(filePath));
        // 根据文件名获取文件类型
        String fileType = isImageOrVideoFile(fileInfo.getFileName());
        // 设置文件类型
        fileInfo.setFileType(fileType);
        // 拼接文件路径网址，文件类型，文件路径（如果是非windows系统，需要去除首个/字符），文件名称
        str =StrUtil.subBefore(fileInfo.getRelativePath(), File.separator, true).replace('/', '_');
        // 设置文件路径
        List<String> filenameList=StrUtil.split(fileInfo.getRelativePath(),File.separatorChar);
        fileInfo.setCurrentDir(filenameList.get(filenameList.size()-1));
        // 拼接目标文件路径
        return fileInfo;

    }

    public static String isImageOrVideoFile(String filename) {
        if (filename.toLowerCase().matches(".*\\.(jpg|jpeg|png|dng|gif)$"))
            return "images";
        if (filename.toLowerCase().matches(".*\\.(mp4|avi|rmvb|rm|flv|3gp|mkv|mov|wmv)$"))
            return "videos";

        return "";
    }

   public static boolean Thumbnails(FileInfo fileinfo,String rootString) {
        String fileType = fileinfo.getFileType();
        if (FileUtil.exist(fileinfo.getTargeFile(rootString)))
            return true;
        // 判断目标目录是否存在，如果不存在，则创建
        if (!FileUtil.exist(fileinfo.getTargePath(rootString)))
            FileUtil.mkdir(fileinfo.getTargePath(rootString));
        if (fileType.equals("images")) {
            int width = 300, heigh = 300;
            try {
                Thumbnails.of(new File(fileinfo.getFileNamePath(rootString)))
                        .size(width, heigh)
                        .toFile(new File(fileinfo.getTargeFile(rootString)));
                return true;

            } catch (IOException e) {
                return false;
            }

        }
        return true;
    }
}
