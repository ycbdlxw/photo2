package com.ycbd.photoservice.controller;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/videos")
@CrossOrigin(origins = "*")
public class VideoController {
    @GetMapping("/{directory}/{filename:.+}")
    public ResponseEntity<FileSystemResource> getVideo(@PathVariable String directory, @PathVariable String filename) {
        if(!directory.startsWith("_"))
            directory="_"+directory;
        String directoryPath = StrUtil.replace(directory, "_", "/");
        //判断字符串首个字符

        String encodedDirectory = URLEncoder.encode(directoryPath, StandardCharsets.UTF_8);
        String decodedDirectory = URLDecoder.decode(encodedDirectory, StandardCharsets.UTF_8);
        String videoPath ="";



        videoPath =decodedDirectory + File.separator + filename;// "/Volumes/homepc-1/sync/homenas/salina/Photos/MobileBackup/EBG-AN00/DCIM/Camera/2023/07/"
        // + filename; // Replace with your video
        // directory path

        try {
            FileSystemResource videoResource =new FileSystemResource(videoPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(videoResource);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());

        }
        return null;

    }

}