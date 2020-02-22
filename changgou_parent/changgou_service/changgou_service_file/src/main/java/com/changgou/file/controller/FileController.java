package com.changgou.file.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.util.FastDFSClient;
import com.changgou.file.util.FastDFSFile;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @PackageName: com.changgou.file.controller
 * @ClassName: FileController
 * @Author: suibo
 * @Date: 2019/12/29 21:17
 * @Description: //fastDFS分布式文件存储系统上传文件
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public Result upload(MultipartFile file){
        try {
            //1.判断文件是否存在
            if(file==null){
                throw new RuntimeException("文件不存在");
            }
            //2.获取文件完整文件名
            String originalFilename = file.getOriginalFilename();
            if(StringUtils.isEmpty(originalFilename)){
                throw new RuntimeException("文件不存在");
            }
            //3.获取文件后缀
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //4.获取文件内容
            byte[] content = file.getBytes();
            //5.获取文件上传的封装实体类
            FastDFSFile fastDFSFile = new FastDFSFile(originalFilename,content,extName);
            //6.调用工具类进行文件上传
            String[] upload = FastDFSClient.upload(fastDFSFile);
            //7.获得url
            String url = FastDFSClient.getTrackerUrl() + upload[0] + "/" +upload[1];
            return new Result(true, StatusCode.OK,"文件上传成功",url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.ERROR,"文件上传失败");
        }
    }
}
