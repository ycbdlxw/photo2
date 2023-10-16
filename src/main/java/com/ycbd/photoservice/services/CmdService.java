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
            // ffmpeg -i C:\Video\test.mp4 -ss 1 -f image2 C\:Image\out.jpg回车

            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-ss");
            command.add("00:00:03");
            command.add("-i");
            command.add(videoFilePath);
            command.add("-frames:v");
            command.add("1");
            command.add(imageSavePath);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            // 将错误流与输出流合并
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取输出流
            // try (BufferedReader reader = new BufferedReader(new
            // InputStreamReader(process.getInputStream()))) {
            // String line;
            // while ((line = reader.readLine()) != null) {
            // // 处理命令输出
            // System.out.println(line);
            // }
            // }

            // 创建一个线程来等待命令执行完毕
            Thread waitThread = new Thread(() -> {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    // 发生异常时终止进程
                    process.destroy();
                }
            });
            waitThread.start();

            // 等待命令执行完毕，设置超时时间为1分钟
            try {
                waitThread.join(1 * 60 * 1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (waitThread.isAlive()) {
                // 超时，终止进程
                process.destroy();
                System.out.println("命令行操作执行超时");
                return false;
            } else {
                int exitCode = process.exitValue();
                if (exitCode == 0) {
                    System.out.println(imageSavePath + " 命令行操作执行成功");
                } else {
                    System.out.println("命令行操作执行失败");
                    return false;
                }
            }
            File file = new File(imageSavePath);
            return file.exists();

        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }

    }
}
