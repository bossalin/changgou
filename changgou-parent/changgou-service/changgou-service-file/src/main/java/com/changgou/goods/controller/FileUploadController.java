package com.changgou.goods.controller;

import com.changgou.goods.file.FastDFSFile;
import com.changgou.goods.util.FastDFSClient;
import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wzq on 2020/11/17.
 */
@RestController
@RequestMapping("upload")
@CrossOrigin
public class FileUploadController {
    /***
     * 文件上传
     * @return
     */
    @PostMapping
    public String upload(@RequestParam("file")MultipartFile file) throws Exception {
        //封装一个FastDFSFile
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(), //文件名字
                file.getBytes(),            //文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename()));//文件扩展名

        //文件上传
        String[] uploads = FastDFSClient.upload(fastDFSFile);
        //组装文件上传地址
        return FastDFSClient.getTrackerUrl()+"/"+uploads[0]+"/"+uploads[1];

    }
}
