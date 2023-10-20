package com.ycbd.photoservice.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public Map<String, String> getMetaObjValue(String filePath, String keys) {
        Map<String, String> resultMap = new HashMap<>();
        List<String> command = new ArrayList<>();
        command.add("exiv2");
        if (!keys.contains(",")) {
            command.add("-pa");
            command.add("--grep");
            command.add(keys);
        } else {
            String[] keyArr = keys.split(",");
            for (String key : keyArr) {
                command.add("-pa");
                command.add("--grep");
                command.add(key);
            }
        }
        command.add(filePath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println("pid: " + process.pid());
            ;
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> list = Tools.getTags(StrUtil.split(line, " "));
                if (list.size() > 2) {
                    // System.out.println(line);
                    for (String key : keys.split(",")) {
                        if (line.contains(key)) {
                            resultMap.put(key, String.join(" ", ListUtil.sub(list, 3, list.size())).trim());
                        }

                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(filePath + " 命令行操作执行成功");
            } else {
                System.out.println(filePath + "命令行操作执行失败");
            }
            return resultMap;
        } catch (Exception e) {
            return resultMap;
        }

    }

    public String getMetaMd5(String filePath) {
        String result = RuntimeUtil.execForStr("exiv2 -pa " + filePath);
        if (StrUtil.isBlank(result))
            return "";
        MD5 md5 = new MD5();
        //去除file的相关内容
        String md5Str = md5.digestHex(result);
        return md5Str;

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
public boolean getVideoImage(String videoFilePath, String imageSavePath) {
    try {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-ss");
        command.add("00:00:03");
        command.add("-i");
        command.add(videoFilePath);
        command.add("-frames:v");
        command.add("1");
        command.add(imageSavePath);
        command.add("-y");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // 合并错误流和输出流
        Process process = processBuilder.start();

        // 创建一个新的线程来读取和显示命令的输出
        new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                     if (line.contains("already exists. Overwrite")) {
                break; // 如果输出包含"already exists. Overwrite"，则退出循环
            }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        int exitCode = process.waitFor(); // 等待命令执行完成
        if (exitCode == 0) {
            System.out.println(imageSavePath + " 命令行操作执行成功");
        } else {
            System.out.println("命令行操作执行失败");
            return false;
        }

        File file = new File(imageSavePath);
        return file.exists(); // 检查截图文件是否存在

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}
