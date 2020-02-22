package com.changgou.file;


import org.apache.commons.io.IOUtils;
import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;


/**
 * @PackageName: com.changgou.file
 * @ClassName: FileTest
 * @Author: suibo
 * @Date: 2019/12/29 19:27
 * @Description: //fastDFS分布式文件存储系统测试
 */
public class FileTest {

    //测试文件上传到storage
    @Test
    public void testFileUpload() throws Exception{
        //1.连接fastDFS
        ClientGlobal.init("F:\\changgou\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fdfs_client.conf");
        //2.Tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        //3.Storage
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);
        //4.上传文件
        String[] strings = storageClient.upload_file("C:\\Users\\眭博\\Desktop\\aaa.jpg", "jpg", null);
        for (String string : strings) {
            System.out.println(string);
        }

        //5.关闭资源
        storageServer.close();
        trackerServer.close();
    }

    //测试从storage文件下载
    @Test
    public void testFileDown() throws Exception{
        //1.初始化
        ClientGlobal.init("F:\\changgou\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fdfs_client.conf");
        //2.Tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        //3.Storage
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);
        //4.下载
        byte[] bytes = storageClient.download_file("group1", "M00/00/00/wKjIgF4JSnaAYtf-AAANt9KDpWU734.jpg");
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\眭博\\Desktop\\aaa.jpg"));
        IOUtils.write(bytes,outputStream);
        //5.关闭资源
        outputStream.close();
        storageServer.close();
        trackerServer.close();
    }

    //测试从storage中删除文件
    @Test
    public void testFileDelete() throws Exception{
        //1.初始化,加载配置文件
        ClientGlobal.init("F:\\changgou\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fdfs_client.conf");
        //2.Tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        //3.Storage
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);
        //4.删除
        storageClient.delete_file("group1", "M00/00/00/wKjIgF4JSnaAYtf-AAANt9KDpWU734.jpg");
        //5.关闭资源
        storageServer.close();
        trackerServer.close();
    }

}
