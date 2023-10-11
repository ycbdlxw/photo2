package com.ycbd.photoservice.controller;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author lxw
 * com.ycbd.exiv2service.controller
 * @create 2023/8/31 11:22
 * @description
 */
@RestController
public class ImageController {
    private final ResourceLoader resourceLoader;
    public ImageController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    @GetMapping("/images/{directory}/{imageName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String directory, @PathVariable String imageName) throws IOException {
        String directoryPath ="";
        if(directory.contains("@eaDir"))
        {
            List<String> spiltDirs=StrUtil.split(directory, '@');
            directoryPath=StrUtil.replace(spiltDirs.get(0), "_", File.separator)+"@"+spiltDirs.get(1).replaceFirst("_",File.separator)+File.separator;
        }
        else
            directoryPath=StrUtil.replace(directory, "_", File.separator);

        String encodedDirectory = URLEncoder.encode(directoryPath, StandardCharsets.UTF_8);
        String decodedDirectory = URLDecoder.decode(encodedDirectory, StandardCharsets.UTF_8);

        org.springframework.core.io.Resource resource = resourceLoader.getResource("file:"+File.separator + decodedDirectory + File.separator + imageName);
        if (resource.exists()) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
        } else {
            System.out.println("resource: "+"file:/" + decodedDirectory + "/" + imageName);
            return ResponseEntity.notFound().build();
        }
    }
}
