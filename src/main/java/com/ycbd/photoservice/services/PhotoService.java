package com.ycbd.photoservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ycbd.photoservice.model.FileInfo;
import com.ycbd.photoservice.tools.Tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Service
public class PhotoService {
    @Resource
    protected SqiteService sqiteService;
    @Resource
    protected CmdService cmdService;
    @Resource
    protected ExivService exivService;

    // @Resource
    // private MessageSender messageSender;
    // @Resource
    // private RabbitTemplate rabbitTemplate;
    @Value("${system.root:/Volumes/homes}")
    String root;
    @Value("${system.prefix:http://homenas.ycbd.work:8080/}")
    String prefix;
    @Value("${system.runmode:command}")
    String runmode;


    /**
     * 根据路径及用户设备信息，查询最新的新增文件保存到数据库中
     * @param pathString
     * @param UserDevices
     * @return    新增成功的数据记录数
     */
    public int saveAddData(String pathString,List<String> UserDevices){
         List<String> fileDataList = new ArrayList<>();
        List<String> userfileDataList = new ArrayList<>();      
        try {
            long dbTime=dbInit(pathString);
            for (String it : UserDevices) {
                List<String> userdevices=StrUtil.split(it,"|");
                String device="";
                if(userdevices.size()>1)
                   device=userdevices.get(1);
                userfileDataList = scanMobileBackupDir(pathString, userdevices.get(0), device, dbTime,3);
                if (userfileDataList.size() > 0){
                    fileDataList.addAll(userfileDataList);
                    userfileDataList.clear();
                }   
            }
           

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int batchSize = 200;
        if (fileDataList.size() < batchSize) {
            saveData(fileDataList);
        } else {
            // 分批处理保存数据
            for (int i = 0; i < fileDataList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, fileDataList.size());
                List<String> batchDataList = fileDataList.subList(i, endIndex);
                saveData(batchDataList);
            }
        }

        return fileDataList.size();
    }

    public List<Map<String, Object>> getFileInfoByPath(List<String> UserDevices, String uuid) {
        int addCount= saveAddData(root,UserDevices);
        System.out.println("新增数据："+addCount);
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        String queryDbStr = "select * from fileinfo order by id  desc limit 0,200";
        dataList = sqiteService.query(queryDbStr);
        return dataList;
    }
    public int getQueryTotal(String UserDevices){
         List<String> user_device=StrUtil.split(UserDevices, "|");
        String countQuery = "select COUNT(*) from fileinfo where  user = '" + user_device.get(0) + "' and model = '" + user_device.get(1) + "' order by id desc";
        int total=0; 
        try {
            dbInit(root); 
            total= sqiteService.getTotal(countQuery);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return total;
    }
    public List<Map<String, Object>> getFileInfoByUserDevice(String UserDevices,int pageSize,int pageNumber) {
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        List<String> user_device=StrUtil.split(UserDevices, "|");
        String queryDbStr = "select * from fileinfo where  user = '" + user_device.get(0) + "' and model = '" + user_device.get(1) + "' order by id desc";
        List<String> columns = Arrays.asList("filename", "thumbnails", "url", "type", "currentDate", "shootingTime", "GPSFlag", "selected","filePath");
        try {
            dbInit(root);
            if(pageSize==0)
            pageSize=20;
            if(pageNumber==0)
            pageNumber=1;
            dataList=sqiteService.getDataByPage(queryDbStr,columns,pageSize,pageNumber);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        return dataList;
    }
    /**
     * 根据路径查询指定的数据库文件，并返回数据库文件的最后更新时间
     * @param pathString
     * @return
     * @throws IOException
     */
    public long dbInit(String pathString) throws IOException {
    String dbPathString = pathString + File.separator + "photos.db";
    sqiteService = new SqiteService(dbPathString);
    long dbTime=0;
    try {
        sqiteService.getConnection(dbPathString);
        // 判断数据库表是否存在
        if (!sqiteService.isTableExists()) {
            sqiteService.createDatabaseAndTable();
        }
        Path path = Paths.get(dbPathString);
        dbTime = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return dbTime;
}
// 获取不存在数据的数据
    private List<String> getDataNotIn(List<String> saveDataList) {
        String query = saveDataList.stream()
                .map(str -> "'" + StrUtil.replace(str,root,"") + "'")
                .collect(Collectors.joining(","));
        List<String> dataNotInCurrentMonth = sqiteService.queryDataNotInCurrentMonth(" filepath  in (" + query + ")",
                saveDataList,root);

        return dataNotInCurrentMonth;
    }
    private void saveData(List<String> saveFileList) {
        if (saveFileList.size() < 1)
            return;
         List<String> dataNotInCurrentMonth = getDataNotIn(saveFileList);
         if(dataNotInCurrentMonth.size()<1)return;
        List<Map<String, Object>> saveMapList = new ArrayList<>();
        for (String it : dataNotInCurrentMonth) {
            Map<String, Object> map = getMapData(it);
            saveMapList.add(map);
        }
        
        sqiteService.saveDataToTable(saveMapList);
    }

    public Map<String, Object> getMapData(String it) {
        if (it.contains("@") || it.contains("thumbnails"))
            return new HashMap<>();

        File itfile = FileUtil.file(it);
        // 如果是目录直接返回
        if (itfile.isDirectory())
            return new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        // 拆分为目录内容，文件内容两部分
        FileInfo fileinfo = Tools.getListString(it, root);
        // 获取文件类型
        String fileType = Tools.isImageOrVideoFile(fileinfo.getFileName());
        // 如果不是相片或是图片，直接返回
        if (StrUtil.isEmpty(fileType))
            return new HashMap<>();
        fileinfo.setFileType(fileType);
        boolean ThumbnailsFlag = getThumbnailsFlag(fileinfo, runmode, fileType, cmdService);
        if (ThumbnailsFlag)
            map.put("thumbnails", fileinfo.getThumbnails());
        else
            map.put("thumbnails", "");
        map.put("url", fileinfo.getFileUrl());
        map.put("filename", fileinfo.getFileName());
        map.put("filePath", fileinfo.getFilePath());
        map.put("selected", false);
        map.put("currentDir", fileinfo.getCurrentDir());
        map.put("type", fileinfo.getFileType());
        List<String> item = StrUtil.split(it, File.separator);
        if (item.size() > 1)
            map.put("user", item.get(3));
        else
            map.put("user", "");
        if (it.contains("Camera") && item.size() > 6)
            map.put("model", item.get(6));
        else
            map.put("model", "");

        if (fileType.equals("images"))
            exivService.getMetaDataInfo(it, map);
        else {
            String dateTimeStr = Tools.extractDateTimeByFile(map.get("filePath").toString());
            map.put("currentDate", StrUtil.split(dateTimeStr, " ").get(0));
            map.put("shootingTime", dateTimeStr);
        }

        System.out.println("currentDate: " + map.get("currentDate"));
        return map;

    }
   public Boolean getThumbnailsFlag(FileInfo fileinfo, String runmode, String fileType, CmdService cmdService) {
        boolean ThumbnailsFlag = false;
        if (fileType.equals("videos"))
            System.out.println("getTargeFile: " + fileinfo.getTargeFile());
        if (FileUtil.exist(fileinfo.getTargeFile()))
            return true;
        // 判断目标目录是否存在，如果不存在，则创建
        if (!FileUtil.exist(fileinfo.getTargePath()))
            FileUtil.mkdir(fileinfo.getTargePath());
        if (fileType.equals("videos")) {

            if (!FileUtil.isFile(fileinfo.getTargeFile())) {
                if (runmode.equals("command"))
                    ThumbnailsFlag = cmdService.getVideoImage(fileinfo.getFilePath(), fileinfo.getTargeFile());
                if (runmode.equals("script")) {
                    List<String> cmdList = cmdService.ffmpeg(fileinfo.getFilePath(), fileinfo.getTargeFile());
                    if (cmdList != null && cmdList.size() > 0) {
                        if (cmdList.get(cmdList.size() - 1).contains("Invalid data"))
                            ThumbnailsFlag = false;
                    }
                }

            }

        } else {
            try {
                ThumbnailsFlag = Tools.Thumbnails(fileinfo);
            } catch (Exception e) {
                return false;
            }
        }
        return ThumbnailsFlag;
    }


    private List<String> scanMobileBackupDir(String ROOT_DIR, String user, String device, long dbtime,int maxDepth)
            throws IOException {
        List<String> result = new ArrayList<>();
        // 根据数据库最后的更新时间为标准，如果文件或是目录大于则表示有文件或是数据没有更新到数据库
        long targetTime = dbtime;
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
        Files.walk(deviceDirPath, maxDepth).filter(Files::isDirectory).forEach(path -> {
            // 获取目录的最后修改时间
            try {
              //  System.out.println(path.toString() + " " + Files.getLastModifiedTime(path).toString());

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
        //相机数据处理
        Path CameraDirPath = deviceDirPath.resolve("DCIM").resolve("Camera");
        Files.walk(CameraDirPath, 2).filter(Files::isDirectory).forEach(path -> {
            // 获取目录的最后修改时间
            try {
               // System.out.println(path.toString() + " " + Files.getLastModifiedTime(path).toString());

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
        // 将所有符合条件的目录下的所有符合指定时间内的文件返回
        List<String> fileList = new ArrayList<>();
        result.stream().forEach(it -> {
            try {
                Files.walk(Paths.get(it), 1).forEach(path -> {
                    try {
                      //  System.out.println(path.toString() + " " + Files.getLastModifiedTime(path).toString());
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

   
}
