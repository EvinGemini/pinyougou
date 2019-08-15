package com.pinyougou.fastdfs;

import org.csource.fastdfs.*;

public class UploadUtil {
    /***
     * 获取配置文件路径
     * @param conf
     * @return
     */

    public static String getConfig(String conf) {
        if (conf.startsWith("classpath:")) {
            conf = conf.replace("classpath:",UploadUtil.class.getResource("/").getPath());
        }
        return conf;
    }

    /***
     * 图片上传，同时获取访问路径
     * @param trackerserver
     * @param buffer
     * @param subfix
     * @param domain
     * @return
     */
    public static String upload(String trackerserver, byte[] buffer, String subfix,String domain) {
        //文件上传
        String[] infos = upload(trackerserver,buffer,subfix);
        //拼接url，并返回
        return domain+infos[0]+"/"+infos[1];
    }

    /***
     * FastDFS图片上传工具类
     * @param trackerserver
     *              tracker服务端链接的socket链接IP和端口，格式 tracker_server=192.168.174.130:22122
     *              这里的trackerserver是完整的文件，供客户端代码读取解析，文件格式如上
     *
     * @param buffer   文件的字节数组
     * @param subfix   上传的文件后缀
     * @return
     */
    public static String[] upload(String trackerserver, byte[] buffer, String subfix) {
        try {
            trackerserver = getConfig(trackerserver);
            //初始化tracker服务
            ClientGlobal.init(trackerserver);

            //创建一个Tracker客户端
            TrackerClient tracker = new TrackerClient();

            //链接tracker服务
            TrackerServer trackerServer = tracker.getConnection();

            //通过链接tracker获得Storage信息并创建一个Storage服务端和客户端
            StorageServer storageServer = null;
            StorageClient storageClient = new StorageClient(trackerServer, storageServer);

            /***
             * 通过客户端想storage上传图片
             * buffer：文件的字节数组
             * subfix:文件的后缀
             * 上传响应信息
             * fileinfos[0]:文件存储的组名
             * fileinfos[1]:文件的其他存储区间名和文件名的组合
             */
            String[] fileinfos= storageClient.upload_file(buffer, subfix, null);

            return fileinfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] upload(String trackerserver, String vfile) {

        try {
            trackerserver = getConfig(trackerserver);
            vfile = getConfig(vfile);

            //初始化tracker服务
            ClientGlobal.init(trackerserver);

            //创建一个Tracker客户端
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();


            //通过链接tracker获得Storage信息并创建一个Storage服务端和客户端
            StorageServer storageServer = null;
            StorageClient storageClient = new StorageClient(trackerServer, storageServer);

            String[] fileinfos= storageClient.upload_file(vfile,null,null);
            System.out.println("1:" + fileinfos[0] + ",2:" + fileinfos[1]);
            return fileinfos;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
