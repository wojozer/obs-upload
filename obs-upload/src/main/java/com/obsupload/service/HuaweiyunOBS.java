package com.obsupload.service;

import com.obsupload.configur.OBSHandler;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhengwj
 * @Description:
 * @Date: 2020/4/20 12:45
 * @Version: 1.0
 */
@Component
public class HuaweiyunOBS {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private OBSHandler obsHandler;

    @Value("${huawei.obs.accessKeyId}")
    private String accessKeyId;        //华为云的 Access Key Id
    @Value("${huawei.obs.accessKeySecret}")
    private String accessKeySecret;    //华为云的 Access Key Secret
    @Value("${huawei.obs.obsEndpoint}")
    private String obsEndpoint;	//格式如 obs.cn-north-1.myhuaweicloud.com
    @Value("${huawei.obs.bucketName}")
    private String bucketName;        //obs桶名

    @Value("${huawei.obs.parentPath}")
    private String parentPath;         //监控路径

    /**
     * 以下配置用于 补数据
     */
    @Value("${local.specifiedPaths:}")
    private String specifiedPaths;  //指定上传路径
    @Value("${local.vice.isopen:false}")
    private boolean isOpenVice;   //是否开启副应用

    /**
     * 获取连接
     * @return
     */
    public OBSHandler getObsHander() {
        if(obsHandler == null) {
            obsHandler = new OBSHandler(accessKeyId,accessKeySecret,obsEndpoint);
            // 如果设置过CDN的路径测设置为CDN路径，没有设置则为桶原生的访问路径
            //obsHandler.setUrlForCDN(Global.get("ATTACHMENT_FILE_URL"));
            // 在数据库中读取进行操作的桶的明恒
            obsHandler.setObsBucketName(bucketName);
            // 对桶名称进行当前类内缓存
            bucketName = obsHandler.getObsBucketName();
        }
        return obsHandler;
    }


    /**
     * 增量录音文件上传OBS
     * @param file
     */
    @Async
    public void excute(File file){
        if(file.isFile() && file.getName().endsWith(".mp3")){
            int index = file.getAbsolutePath().indexOf("monitor");
            String fileName =file.getAbsolutePath().substring(index).replaceAll("\\\\", "/");
            try{
                getObsHander().putLocalFile(bucketName, fileName, file);
                getObsHander().setObjectAclPubilcRead(fileName);
                String url =  getObsHander().signatureUrl(fileName);
                log.info(url);
            }catch (Exception e){
                log.error("上传{}失败:{}",fileName,e);
            }finally {
    //            getObsHander().closeOBSClient();
            }
        }
    }

    /**
     * 文件监控
     *          增量上传
     */
    public void monitoring(){
        if(isOpenVice){
            log.info("开启上传指定路径下文件");
            uploadSpecified();
        }
        log.info("开启监控.....");
        // 轮询间隔 5 秒
        long interval = TimeUnit.SECONDS.toMillis(1);
        // 创建过滤器
        IOFileFilter directories = FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                HiddenFileFilter.VISIBLE);
        IOFileFilter files    =  FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".mp3"));
        IOFileFilter filter = FileFilterUtils.or(directories, files);
        // 使用过滤器
        FileAlterationObserver observer = new FileAlterationObserver(new File(parentPath), filter);
        //不使用过滤器
        //FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir));
        observer.addListener(new FileListener());
        //创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        // 开始监控
        try {
            monitor.start();
        }catch (Exception e){
            log.error("执行出错:{}",e);
        }

    }


    /**
     * 上传指定文件夹下的文件
     *
     */
    public void uploadSpecified(){
        if(StringUtils.isEmpty(specifiedPaths)){
            return;
        }
        String[] paths = specifiedPaths.split(",");
        for(String specifiedPath : paths){
            File specifiedFile = new File(specifiedPath);
            if(specifiedFile.isDirectory()){
                File[] files = specifiedFile.listFiles();
                for (File file : files){
                    excute(file);
                }
            }
        }
    }



    /**
     * 文件夹监听器
     */
    class FileListener extends FileAlterationListenerAdaptor {

        private final Logger log = LoggerFactory.getLogger(getClass());

        /**
         * 文件创建执行
         */
        public void onFileCreate(File file) {
            log.info("[新建]:" + file.getAbsolutePath());
            excute(file);
        }

        /**
         * 文件删除
         */
        public void onFileDelete(File file) {
        //    log.info("[删除]:" + file.getAbsolutePath());
        }

        /**
         * 目录创建
         */
        public void onDirectoryCreate(File directory) {
            log.info("[新建]:" + directory.getAbsolutePath());
        }

        public void onStart(FileAlterationObserver observer) {
            // TODO Auto-generated method stub
            super.onStart(observer);
        }
        public void onStop(FileAlterationObserver observer) {
            // TODO Auto-generated method stub
            super.onStop(observer);
        }

    }

}
