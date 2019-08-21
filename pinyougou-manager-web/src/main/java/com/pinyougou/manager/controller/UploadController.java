package com.pinyougou.manager.controller;

import com.pinyougou.fastdfs.UploadUtil;
import com.pinyougou.http.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class UploadController {
    @Value("${TRACKER_PATH}")
    private String trackerserver;
    @Value("${FASTDFS_DOMAIN}")
    private String domain;


    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String subfix = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String url = UploadUtil.upload(trackerserver, bytes, subfix, domain);
            return new Result(true,url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Result(true,"上传失败");
    }
}
