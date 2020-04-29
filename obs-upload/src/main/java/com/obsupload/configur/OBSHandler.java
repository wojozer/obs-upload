package com.obsupload.configur;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhengwj
 * @Description:
 * @Date: 2020/4/20 12:47
 * @Version: 1.0
 */
public class OBSHandler {

    private String accessKeyId;// 华为云的 Access Key Id
    private String accessKeySecret;// 华为云的 Access Key Secret
    private String endpoint; // 华为云连接的地址节点

    private String obsBucketName; // 创建的桶的名称
    private String url; // 访问OBS文件的url

    private static ObsClient obsClient; // 进行操作的华为云的客户端组件


    /**
     * 创建华为云OBS的本地控制器
     * @param accessKeyId
     * @param accessKeySecret
     * @param endpoint
     */
    public OBSHandler(String accessKeyId, String accessKeySecret, String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.endpoint = endpoint;
    }

    public OBSHandler(String accessKeyId, String accessKeySecret, String endpoint, String obsBucketName) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.endpoint = endpoint;
        this.obsBucketName = obsBucketName;
    }

    /**
     * 设置OBS访问的CDN路径
     * @param url  需要配置的访问OBS的CDN路径 传入的格式如 http://cdn.leimingyun.com/
     */
    public void setUrlForCDN(String url) {
        this.url = url;
    }

    /**
     * 设置OBS操作的同桶名称
     * @param obsBucketName
     */
    public void setObsBucketName(String obsBucketName) {
        this.obsBucketName = obsBucketName;
    }

    /**
     * 获取华为云提供的操作客户端实体类
     * @return
     */
    public ObsClient getObsClient() {
        if(obsClient == null) {
            obsClient = new ObsClient(accessKeyId, accessKeySecret, endpoint);
        }
        return obsClient;
    }

    /**
     * 下载ObsObject
     * @param bucketName    操作的桶的名称 例："wangmarket1232311"
     * @param filePath   需要下载的文件路径。 例："site/a.txt"
     * @return  下载文件的字节数组
     * @throws IOException
     */
    public byte[] getFileByteArray(String bucketName, String filePath) throws IOException {
        ObsObject obsObject = getObsClient().getObject(bucketName, filePath);
        InputStream input = obsObject.getObjectContent();
        byte[] b = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        while ((len = input.read(b)) != -1){
            bos.write(b, 0, len);
        }
        bos.close();
        input.close();
        return bos.toByteArray();
    }


    /**
     * 获取指定路径下的ObsObject数量
     * @param bucketName  操作的桶的名称 例："wangmarket1232311"
     * @param filePath 需要检索的文件夹路径 例："site/"
     * @return  检索搜文件下的ObsObject的数量
     */
    public Integer getFolderObjectsSize(String bucketName, String filePath) {
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        if(filePath != null && (!filePath.trim().equals(""))){
            request.setPrefix(filePath);
        }
        ObjectListing result = getObsClient().listObjects(request);
        return new Integer(result.getObjects().size());
    }

    /**
     * 获取指定路径下的ObsObject
     * @param bucketName 操作的桶的名称 例："wangmarket1232311"
     * @param filePath 需要检索的文件夹路径
     * @return 路径下的所有的ObsObject，包括子文件夹下的ObsObject
     */
    public List<ObsObject> getFolderObjects(String bucketName, String filePath) {
        List<ObsObject> list = new ArrayList<ObsObject>();
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        if(filePath != null && (!filePath.trim().equals(""))){
            request.setPrefix(filePath);
        }
        request.setMaxKeys(100);
        ObjectListing result;
        do{
            result = getObsClient().listObjects(request);
            for(ObsObject obsObject : result.getObjects()){
                list.add(obsObject);
            }
            request.setMarker(result.getNextMarker());
        }while(result.isTruncated());
        return list;
    }

    /**
     * 删除对象
     * @param bucketName 操作的桶的名称 例："wangmarket1232311"
     * @param fileName  需要删除的对象全名 例："site/20190817/localFile.sh"
     * @return
     */
    public DeleteObjectResult deleteObject(String bucketName, String fileName) {
        return getObsClient().deleteObject(bucketName, fileName);
    }

    /**
     * 创建文件夹
     * @param bucketName 操作的桶的名称 例："wangmarket1232311"
     * @param fileName  新建文件夹的路径，总根路径开始，请务必以"/"结尾。例："2019/0817/"
     * @return
     */
    public PutObjectResult mkdirFolder(String bucketName, String fileName) {
        return getObsClient().putObject(bucketName, fileName, new ByteArrayInputStream(new byte[0]));
    }

    /**
     * 通过流上传字符串为文件
     * @param bucketName  操作的桶的名称 例："wangmarket1232311"
     * @param fileName  上传的路径和文件名 例："site/2010/example.txt"
     * @param content  上传的String字符
     * @param encode  进行转换byte时使用的编码格式 例："UTF-8"
     * @return
     * @throws ObsException
     * @throws UnsupportedEncodingException
     */
    public PutObjectResult putStringFile(String bucketName, String fileName, String content, String encode) throws ObsException, UnsupportedEncodingException {
        return getObsClient().putObject(bucketName, fileName, new ByteArrayInputStream(content.getBytes(encode)));
    }


    /**
     * 上传文件本地文件
     * @param bucketName 操作的桶的名称 例："wangmarket1232311"
     * @param fileName  上传的路径和文件名 例："site/2010/example.txt"
     * @param localFile  需要上传的文件
     * @return
     */
    public PutObjectResult putLocalFile(String bucketName, String fileName, File localFile) {
        return getObsClient().putObject(bucketName, fileName, localFile);
    }


    /**
     * 上传文件流
     * @param bucketName 操作的桶的名称 例："wangmarket1232311"
     * @param fileName 上传的路径和文件名 例："site/2010/example.txt"
     * @param inputStream  上传文件的输入流
     * @return
     */
    public PutObjectResult putFileByStream(String bucketName, String fileName, InputStream inputStream) {
        return getObsClient().putObject(bucketName, fileName, inputStream);
    }


    /**
     * 通过流上传文件并设置指定文件属性
     * @param bucketName  操作的桶的名称 例："wangmarket1232311"
     * @param fileName  上传的路径和文件名 例："site/2010/example.txt"
     * @param inputStream  上传文件的输入流
     * @param metaData  上传文件的属性
     * @return
     */
    public PutObjectResult putFilebyInstreamAndMeta(String bucketName, String fileName, InputStream inputStream, ObjectMetadata metaData) {
        return getObsClient().putObject(bucketName, fileName, inputStream, metaData);
    }


    /**
     * OBS内对象复制
     * @param sourceBucketName   源文件的桶名称 例："wangmarket1232311"
     * @param sourcePath  源文件的路径和文件名 例："site/2010/example.txt"
     * @param destBucketName  目标文件的桶名称 例："swangmarket34578345"
     * @param destPath 目标文件的路径和文件名 例："site/2010/example_bak.txt"
     */
    public void copyObject(String sourceBucketName, String sourcePath,String destBucketName, String destPath) {
        getObsClient().copyObject(sourceBucketName, sourcePath, destBucketName, destPath);
    }


    /**
     * 获得原生OBSBucket的访问前缀
     * @return  桶原生的访问前缀，即不经过CDN加速的访问路径
     */
    public String getOriginalUrlForOBS() {
        return "//" + obsBucketName + "." + endpoint.substring(8, endpoint.length()) + "/";
    }


    /**
     * 通过bucket的名字和连接点信息获取bucket访问的url
     * @param bucketName  桶的名称 例："wangmarket21345665"
     * @param endpoint 连接点的名称 例："obs.cn-north-1"
     * @return 根据信息获得桶的访问路径 例："//wangmarket21345665.obs.cn-north-1.myhuaweicloud.com/"
     */
    public String getUrlByBucketName(String bucketName, String endpoint) {
        String url = null;
        if (url == null || url.length() == 0) {
            url = "//" + bucketName + "." +  endpoint + ".myhuaweicloud.com" + "/";
        }
        return url;
    }


    /**
     * 创建华为云ObsBucket，默认设置为标准存储，桶访问权限为公共读私有写，同策略为所有用户可读桶内对象和桶内对象版本信息
     * @param obsBucketName 创建桶的名称
     * @return  新创建的桶的名字
     */
    public String createOBSBucket(String obsBucketName) {
        // 将桶的名字进行保存
        this.obsBucketName = obsBucketName;
        ObsBucket obsBucket = new ObsBucket();
        obsBucket.setBucketName(obsBucketName);
        // 设置桶访问权限为公共读，默认是私有读写
        obsBucket.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        // 设置桶的存储类型为标准存储
        obsBucket.setBucketStorageClass(StorageClassEnum.STANDARD);
        // 创建桶
        getObsClient().createBucket(obsBucket);
        //设置桶策略
        String json = "{"
                + "\"Statement\":["
                + "{"
                + "\"Sid\":\"为授权用户创建OBS使用的桶策略\","
                + "\"Principal\":{\"ID\" : \"*\"},"
                + "\"Effect\":\"Allow\","
                + "\"Action\":[\"GetObject\",\"GetObjectVersion\"],"
                + "\"Resource\": [\"" + obsBucketName + "/*\"]"
                + "}"
                + "]}";
        getObsClient().setBucketPolicy(obsBucketName, json);
        return obsBucketName;
    }


    /**
     * 获取当前的桶列表
     * @return 当前桶的列表信息
     */
    public List<S3Bucket> getBuckets() {
        return getObsClient().listBuckets();
    }


    /**
     * 关闭当前的使用的OBSClient
     */
    public void closeOBSClient() {
        if(getObsClient() != null){
            try {
                getObsClient().close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回当前的创建桶的名称 例："wangmarket1232311"
     * @return 如果有桶，那么返回桶的名称，如 "wangmarket1232311" ，如果没有，则返回 null
     */
    public String getObsBucketName() {
        return this.obsBucketName;
    }


    /**
     *  返回当前的桶的访问路径 例：“ http://cdn.leimingyun.com/”
     * @return 若已经手动设置CDN路径返回为CND路径，反之则为OBS原始的访问路径
     */
    public String getUrl() {
        // 用户没有配置CDN，获的桶的原生访问路径
        if(url == null) {
            url = getOriginalUrlForOBS();
        }
        return url;
    }


    /**
     * 为对象设置公共读
     * @param objectKey
     */
    public HeaderResponse setObjectAclPubilcRead(String objectKey){

        return  obsClient.setObjectAcl(obsBucketName, objectKey, AccessControlList.REST_CANNED_PUBLIC_READ);

    }

    /**
     *  获得下载路径
     * @param objectKey
     * @return
     */
    public String signatureUrl(String objectKey){
        long expireSeconds = 3600L;
        Map<String, String> headers = new HashMap<String, String>();

        String contentType = "text/plain";
        headers.put("Content-Type", contentType);

        TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.PUT, expireSeconds);
        request.setBucketName(obsBucketName);
        request.setObjectKey(objectKey);
        request.setHeaders(headers);

        TemporarySignatureResponse response = obsClient.createTemporarySignature(request);

        return response.getSignedUrl();
    }

}
