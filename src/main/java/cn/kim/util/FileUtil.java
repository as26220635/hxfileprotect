package cn.kim.util;

import cn.kim.common.attr.Attribute;
import cn.kim.common.attr.AttributePath;
import cn.kim.common.attr.ConfigProperties;
import cn.kim.common.sequence.Sequence;
import cn.kim.common.springmvc.BASE64DecodedMultipartFile;
import cn.kim.common.tag.FileInput;
import cn.kim.entity.ActiveUser;
import cn.kim.entity.CxfFileWrapper;
import cn.kim.entity.CxfState;
import cn.kim.service.FileService;
import cn.kim.service.FileWS;
import cn.kim.service.impl.FileWSImplService;
import com.google.common.collect.Maps;
import com.sun.istack.internal.ByteArrayDataSource;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by 余庚鑫 on 2017/3/5.
 */
@Component
@Log4j2
public class FileUtil {

    @Autowired
    private FileService fileService;
    private static FileUtil fileUtil;

    private static BASE64Decoder decoder = new BASE64Decoder();

    public void setSysUploadFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @PostConstruct
    public void init() {
        fileUtil = this;
        fileUtil.fileService = this.fileService;

    }

    /**
     * 保存文件
     *
     * @param file
     * @param configure
     * @return
     * @throws IOException
     */
    public static Map<String, Object> saveFile(MultipartFile file, Map<String, Object> configure) throws IOException {
        configure.put("allowFile", FileInput.FILE);
        return saveFileToDisk(file, configure);
    }

    /**
     * 保存视频
     *
     * @param file
     * @param configure
     * @return
     * @throws IOException
     */
    public static Map<String, Object> saveVideo(MultipartFile file, Map<String, Object> configure) throws IOException {
        configure.put("allowFile", FileInput.VIDEO);
        return saveFileToDisk(file, configure);
    }

    /**
     * 保存图片
     *
     * @param file
     * @param configure
     * @return
     * @throws IOException
     */
    public static Map<String, Object> saveImgFile(MultipartFile file, Map<String, Object> configure) throws IOException {
        configure.put("allowFile", FileInput.IMAGE);
        return saveFileToDisk(file, configure);
    }

    /**
     * 保存文件
     *
     * @param file
     * @param configure
     * @return
     */
    public static Map<String, Object> saveFileToDisk(MultipartFile file, Map<String, Object> configure) {

        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(16);

        try {
            CxfState cxfIsOnline = isCxfOnline();

            if (!cxfIsOnline.isOnline()) {
                throw new ConnectException("附件服务器连接失败!");
            }
            ActiveUser activeUser = AuthcUtil.getCurrentUser();
            //操作员ID
            String operatorId = TextUtil.toString(configure.get("SO_ID"));
            //使用表ID
            String fileTableId = TextUtil.toString(configure.get("SF_TABLE_ID"));
            //使用表名
            String fileTableNAME = TextUtil.toString(configure.get("SF_TABLE_NAME"));
            //使用代码
            String fileDictTypeCode = TextUtil.toString(configure.get("SF_SDT_CODE"));
            String fileDictInfoCode = TextUtil.toString(configure.get("SF_SDI_CODE"));
            //查看类型 0 不需要登录就可以查看 1需要登录
            Integer seeType = TextUtil.toInt(configure.get("SF_SEE_TYPE"));
            //文件保存起始名称
            String typeCode = TextUtil.toString(configure.get("SF_TYPE_CODE"));
            //扩展文件路径
            String extendName = TextUtil.toString(configure.get("SF_EXTEND_NAME"));
            //设置操作员ID
            if (!ValidateUtil.isEmpty(activeUser)) {
                operatorId = activeUser.getId();
            }

            if (ValidateUtil.isEmpty(fileTableNAME)) {
                throw new NullPointerException("参数错误!");
            }
            if (ValidateUtil.isEmpty(typeCode)) {
                throw new NullPointerException("参数错误!");
            }

            if (file != null && file.getOriginalFilename() != null && file.getOriginalFilename().length() > 0) {
                //上传文件原始名称
                String originalFilename = file.getOriginalFilename();
                //判断后缀是否是允许的
                String allowFile = TextUtil.toString(configure.get("allowFile"));

                String fileType = FileTypeUtil.getFileType(file);

                if (FileInput.IMAGE.equals(allowFile)) {
                    if (!FileUtil.isCheckSuffix(originalFilename, ConfigProperties.ALLOW_SUFFIX_IMG)) {
                        resultMap.put("code", Attribute.STATUS_ERROR);
                        resultMap.put("message", "图片后缀不符合!");
                        return resultMap;
                    }
                } else if (FileInput.VIDEO.equals(allowFile)) {
                    if (!FileUtil.isCheckSuffix(originalFilename, ConfigProperties.ALLOW_SUFFIX_VIDEO)) {
                        resultMap.put("code", Attribute.STATUS_ERROR);
                        resultMap.put("message", "文件后缀不符合!");
                        return resultMap;
                    }
                } else {
                    if (!FileUtil.isCheckSuffix(originalFilename, ConfigProperties.ALLOW_SUFFIX_FILE)) {
                        resultMap.put("code", Attribute.STATUS_ERROR);
                        resultMap.put("message", "文件后缀不符合!");
                        return resultMap;
                    }
                }

                //是否生成缩略图
                boolean isThumbnail = TextUtil.toBoolean(configure.get("isThumbnail"));
                //文件服务器存储路径
                String filepath = typeCode + File.separator + (ValidateUtil.isEmpty(extendName) ? "" : extendName + File.separator) + DateUtil.getDate(DateUtil.FORMAT2) + File.separator;
                //文件后缀
                String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                //保存缓存文件
                //保存的ID也是名称
                String id = Sequence.getId();
                String thumbnailId = id + "-thumbnail." + suffix;
                //文件缓存路径
                String cacheDir = AttributePath.SERVICE_PATH_DEFAULT + filepath;
                // + "." + suffix;
                String cachePath = cacheDir + id;
                String cacheCutPath = cachePath + ".jpeg";
                String cacheThumbnailPath = cacheDir + thumbnailId;
                //创建文件夹
                createDir(cacheDir);

                //新文件
                File newFile = new File(cachePath);
                //将内存中的文件写入磁盘11
                file.transferTo(newFile);
                //如果是图片.重新生成防止jsp恶意代码
                if (FileUtil.isCheckSuffix(fileType, ConfigProperties.ALLOW_SUFFIX_IMG)) {
                    ImageUtil.addWaterMark(cachePath, cachePath, 0, 0, 0);
                }
                try {
                    //是否压缩图片
                    Boolean isCompress = TextUtil.toBoolean(configure.get("isCompress"));
                    if (isCompress) {
                        Thumbnails.of(newFile).size((Integer) configure.get("width"), (Integer) configure.get("height")).toFile(newFile);
                    }

                    //是否压缩截取图片
                    Boolean isCut = TextUtil.toBoolean(configure.get("isCut"));
                    if (isCut) {
                        if (ImageUtil.imgCut(cachePath, TextUtil.toInt(configure.get("zoomX")), TextUtil.toInt(configure.get("zoomY")), TextUtil.toInt(configure.get("zoomW")), TextUtil.toInt(configure.get("zoomH")), TextUtil.toInt(configure.get("scaleWidth")), TextUtil.toInt(configure.get("scaleHeight")))) {
                            //压缩
                            Thumbnails.of(newFile).size(250, 250).toFile(cacheCutPath);
                            newFile = new File(cacheCutPath);
                        } else {
                            //图片截取错误
                            resultMap.put("code", Attribute.STATUS_ERROR);
                            resultMap.put("message", "图片裁剪错误,请重试!");
                            return resultMap;
                        }
                    }
                    //拿到字节数组
                    byte[] fileBytes = toByteArray(newFile);
                    // 上传附件到附件服务器
                    //签发1分钟的有效token
                    boolean isUpload = uploadCxfFile(TokenUtil.baseKey(UUID.randomUUID(), fileTableNAME), id + "." + suffix, filepath, fileBytes);
                    if (isUpload) {
                        //上传缩略图
                        if (isThumbnail) {
                            float scale = ValidateUtil.isEmpty(configure.get("scale")) ? 1f : TextUtil.toFloat(configure.get("scale"));
                            Thumbnails.of(cachePath).scale(scale).toFile(cacheThumbnailPath);
                            fileBytes = toByteArray(cacheThumbnailPath);
                            uploadCxfFile(TokenUtil.baseKey(UUID.randomUUID(), fileTableNAME), thumbnailId, filepath, fileBytes);
                        }
                        //保存记录到数据库
                        Map<String, Object> fileMap = Maps.newHashMapWithExpectedSize(13);
                        fileMap.put("ID", id);
                        fileMap.put("SO_ID", operatorId);
                        fileMap.put("SF_TABLE_ID", fileTableId);
                        fileMap.put("SF_TABLE_NAME", fileTableNAME);
                        fileMap.put("SF_SDT_CODE", fileDictTypeCode);
                        fileMap.put("SF_SDI_CODE", fileDictInfoCode);
                        fileMap.put("SF_ORIGINAL_NAME", file.getOriginalFilename());
                        fileMap.put("SF_NAME", id + "." + suffix);
                        fileMap.put("SF_PATH", filepath);
                        fileMap.put("SF_SUFFIX", suffix);
                        fileMap.put("SF_SIZE", file.getSize());
                        fileMap.put("SF_SEE_TYPE", seeType);
                        fileMap.put("SF_ENTRY_TIME", DateUtil.getDate());
                        fileUtil.fileService.insertFile(fileMap);

                        resultMap.put("code", Attribute.STATUS_SUCCESS);
                        //文件id
                        resultMap.put("id", id);
                        //加密ID
                        resultMap.put("encryptId", CommonUtil.idEncrypt(id));
                        //文件原始名称
                        resultMap.put("originName", originalFilename);
                        //新的文件名字
                        resultMap.put("newName", id);
                        //文件保存路径
                        resultMap.put("uploadPath", filepath);
                        //文件服务器路径
                        resultMap.put("location", TextUtil.toString(fileMap.get("SF_PATH")) + "@@@" + TextUtil.toString(fileMap.get("SF_NAME")));
                        resultMap.put("locationThumbnail", TextUtil.toString(fileMap.get("SF_PATH")) + "@@@" + thumbnailId);
                        FileUtil.filePathTobase64(resultMap, "location");
                        FileUtil.filePathTobase64(resultMap, "locationThumbnail");
                    } else {
                        throw new FileUploadException("文件上传附件服务器失败!");
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    //删除缓存文件
                    FileUtil.deleteFile(cachePath);
                    FileUtil.deleteFile(cacheCutPath);
                    FileUtil.deleteFile(cacheThumbnailPath);
                }
            } else {
                resultMap.put("code", Attribute.STATUS_ERROR);
                resultMap.put("message", "没有找到文件!");
            }
            return resultMap;
        } catch (Exception e) {
            resultMap.put("code", Attribute.STATUS_ERROR);
            resultMap.put("message", e.getMessage());
            e.printStackTrace();
            return resultMap;
        }
    }

    /**
     * 保存文件信息到数据库
     *
     * @param configure
     * @return
     */
    public static Map<String, Object> saveFileInfo(Map<String, Object> configure) {

        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(16);

        try {
            ActiveUser activeUser = AuthcUtil.getCurrentUser();
            //操作员ID
            String operatorId = TextUtil.toString(configure.get("SO_ID"));
            //使用表ID
            String fileTableId = TextUtil.toString(configure.get("SF_TABLE_ID"));
            //使用表名
            String fileTableNAME = TextUtil.toString(configure.get("SF_TABLE_NAME"));
            //使用代码
            String fileDictTypeCode = TextUtil.toString(configure.get("SF_SDT_CODE"));
            String fileDictInfoCode = TextUtil.toString(configure.get("SF_SDI_CODE"));
            //查看类型 0 不需要登录就可以查看 1需要登录
            Integer seeType = TextUtil.toInt(configure.get("SF_SEE_TYPE"));
            //设置操作员ID
            if (!ValidateUtil.isEmpty(activeUser)) {
                operatorId = activeUser.getId();
            }

            String id = ValidateUtil.isEmpty(configure.get("SF_NAME_NO")) ? TextUtil.toString(configure.get("ID")) : TextUtil.toString(configure.get("SF_NAME_NO"));

            //保存记录到数据库
            Map<String, Object> fileMap = Maps.newHashMapWithExpectedSize(13);
            fileMap.put("ID", id);
            fileMap.put("SO_ID", operatorId);
            fileMap.put("SF_TABLE_ID", fileTableId);
            fileMap.put("SF_TABLE_NAME", fileTableNAME);
            fileMap.put("SF_SDT_CODE", fileDictTypeCode);
            fileMap.put("SF_SDI_CODE", fileDictInfoCode);
            fileMap.put("SF_ORIGINAL_NAME", configure.get("SF_ORIGINAL_NAME"));
            fileMap.put("SF_NAME", configure.get("SF_NAME"));
            fileMap.put("SF_PATH", configure.get("SF_PATH"));
            fileMap.put("SF_SUFFIX", configure.get("SF_SUFFIX"));
            fileMap.put("SF_SIZE", configure.get("SF_SIZE"));
            fileMap.put("SF_SEE_TYPE", seeType);
            fileMap.put("SF_ENTRY_TIME", DateUtil.getDate());
            fileUtil.fileService.insertFile(fileMap);

            return resultMap;
        } catch (Exception e) {
            resultMap.put("code", Attribute.STATUS_ERROR);
            resultMap.put("message", e.getMessage());
            e.printStackTrace();
            return resultMap;
        }
    }

    /**
     * 不存在文件夹就创建
     *
     * @param path
     */
    public static void createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 判断文件夹是否存在
     *
     * @param path
     */
    public static boolean judeDirExists(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath
     * @return
     */
    public static boolean judeFileExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param path
     */
    public static void deleteFile(String path) {
        if (!ValidateUtil.isEmpty(path)) {
            File file = new File(path);
            // 判断目录或文件是否存在
            if (!file.exists()) {
            } else {
                // 判断是否为文件
                // 为文件时调用删除文件方法
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    public static boolean move(File srcFile, String destPath) {
        // Destination directory
        File dir = new File(destPath);

        // Move file to new directory
        boolean success = srcFile.renameTo(new File(dir, srcFile.getName()));

        return success;
    }

    public static boolean move(String srcFile, String destPath) {
        // File (or directory) to be moved
        File file = new File(srcFile);

        // Destination directory
        File dir = new File(destPath);

        // Move file to new directory
        boolean success = file.renameTo(new File(dir, file.getName()));

        return success;
    }

    public static void copy(String oldPath, String newPath) throws IOException {
        int bytesum = 0;
        int byteread = 0;
        File oldfile = new File(oldPath);
        if (oldfile.exists()) {
            InputStream inStream = new FileInputStream(oldPath);
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            int length;
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
//                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
        }
    }

    public static void copy(File oldfile, String newPath) throws IOException {
        int bytesum = 0;
        int byteread = 0;
        //File     oldfile     =     new     File(oldPath);
        if (oldfile.exists()) {
            InputStream inStream = new FileInputStream(oldfile);
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
//                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
        }
    }

    /**
     * 判断图片后缀
     *
     * @param name
     * @param suffix
     * @return
     */
    public static boolean isCheckSuffix(String name, String[] suffix) {
        if (!ValidateUtil.isEmpty(name)) {
            //转换为小写
            name = name.toLowerCase();
            //完全不允许上传的文件
//        if (name.endsWith(".jsp") || name.endsWith(".jspx") || name.endsWith(".jspf") || name.endsWith(".java")) {
//            return false;
//        }
            //判断后缀不区分大小写
            for (String s : suffix) {
                if (name.endsWith(s.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 去除后缀
     *
     * @param fileName
     * @return
     */
    public static String removeSuffix(String fileName) {
        if (fileName.indexOf(".") != -1) {
            return fileName.substring(0, fileName.indexOf("."));
        }
        return fileName;
    }

    /**
     * 判断是不是静态文件
     *
     * @return
     */
    public static boolean isStaticFile(String url) {
        if (url.indexOf("/druid/") != -1) {
            return true;
        } else if (url.indexOf("/check.jpg") != -1) {
            return true;
        } else if (url.indexOf("/mobile/") != -1) {
            return true;
        } else if (url.indexOf("/downloadFile/") != -1) {
            return true;
        } else if (url.indexOf("/perview/") != -1) {
            return true;
        } else if (url.indexOf("/static/") != -1) {
            return true;
        }
        return false;
    }

    /**
     * 计算文件大小，返回KB MB GB
     *
     * @param strSize
     * @return
     */
    public static String convertFileSize(String strSize) {
        long size = Long.parseLong(strSize);
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }

    /**
     * inputstream转为file
     *
     * @param ins
     * @param file
     * @throws IOException
     */
    public static void inputStreamToFile(InputStream ins, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
    }

    /**
     * 拿到文件后缀
     *
     * @param file
     */
    public static String getSuffix(File file) {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String getSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 文件增加名字
     *
     * @param str
     * @param addStr
     * @return
     */
    public static String fileNameAddStr(String str, String addStr) {
        return str.substring(0, str.lastIndexOf(".")) + addStr + "." + getSuffix(str);
    }

    /**
     * 判断文件服务器是否在线
     *
     * @return
     * @throws IOException
     */
    public static CxfState isCxfOnline() throws IOException {
        CxfState cxfIsOnline = new CxfState();

        String endpoint = ConfigProperties.AFFIX_FILE_SERVER_URL;
        URL url = new URL(endpoint);
        HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
        httpc.setConnectTimeout(1000);
        if (httpc.getResponseCode() != 200) {
            cxfIsOnline.setOnline(false);
        } else {
            cxfIsOnline.setOnline(true);
        }
        cxfIsOnline.setUrl(url);

        return cxfIsOnline;
    }

    /**
     * 从文件服务器获取文件
     *
     * @param fileMap
     * @return
     * @throws IOException
     */
    public static CxfFileWrapper getCxfFileWrapper(Map<String, Object> fileMap) throws IOException {
        String ID = TextUtil.toString(fileMap.get("ID"));
        String SF_TABLE_NAME = TextUtil.toString(fileMap.get("SF_TABLE_NAME"));
        String SF_NAME = TextUtil.toString(fileMap.get("SF_NAME"));
        String SF_PATH = TextUtil.toString(fileMap.get("SF_PATH"));

        // 拿到文件
        CxfState cxfState = isCxfOnline();
        FileWSImplService ss = new FileWSImplService(cxfState.getUrl());
        FileWS port = ss.getFileWSImplPort();
        CxfFileWrapper downloadFilefile = new CxfFileWrapper();
        downloadFilefile.setFilePath(SF_PATH);
        downloadFilefile.setFileName(SF_NAME);
        downloadFilefile.setFileToken(TokenUtil.baseKey(ID, SF_TABLE_NAME));
        return port.download(downloadFilefile);
    }

    /**
     * 从文件服务器获取文件
     *
     * @param url
     * @param token
     * @param name
     * @param path
     * @return
     */
    public static CxfFileWrapper getCxfFileWrapper(URL url, String token, String name, String path) {
        // 拿到文件
        FileWSImplService ss = new FileWSImplService(url);
        FileWS port = ss.getFileWSImplPort();
        CxfFileWrapper downloadFilefile = new CxfFileWrapper();
        downloadFilefile.setFilePath(path);
        downloadFilefile.setFileName(name);
        downloadFilefile.setFileToken(token);
        return port.download(downloadFilefile);
    }

    /**
     * 返回InputStream
     *
     * @param file
     * @return
     */
    public static InputStream getInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回InputStream
     *
     * @param filePath
     * @return
     */
    public static InputStream getInputStream(String filePath) throws IOException {
        File file = new File(filePath);
        return new FileInputStream(file);
    }

    /**
     * 返回byte[]数组
     *
     * @param dataHandler
     * @return
     * @throws IOException
     */
    public static byte[] getBytesByDataHandler(DataHandler dataHandler) throws IOException {
        return toByteArray(dataHandler.getInputStream());
    }

    /**
     * 根据cxf文件获取InputStream
     *
     * @param cxfFileWrapper
     * @return
     * @throws IOException
     */
    public static InputStream getInputStream(CxfFileWrapper cxfFileWrapper) throws IOException {
        // 获取流
        return cxfFileWrapper.getFile().getInputStream();
    }

    /**
     * 上传文件到服务器
     *
     * @param token
     * @param name
     * @param path
     * @param bytes
     * @return
     * @throws IOException
     */
    public static boolean uploadCxfFile(String token, String name, String path, byte[] bytes) throws IOException {
        // 上传文件到文件服务器
        String endpoint = ConfigProperties.AFFIX_FILE_SERVER_URL;

        String md5 = getFileMd5(bytes);

        URL url = new URL(endpoint);
        // 上传附件
        FileWSImplService ss = new FileWSImplService(url);
        FileWS port = ss.getFileWSImplPort();

        CxfFileWrapper uploadfile = new CxfFileWrapper();
        DataHandler data = new DataHandler(new ByteArrayDataSource(bytes, "application/octet-stream"));
        uploadfile.setFile(data);
        uploadfile.setFileName(name);
        uploadfile.setFilePath(path);
        uploadfile.setFileToken(token);
        uploadfile.setFileMD5(md5);

        boolean uploadreturn = port.upload(uploadfile);
        return uploadreturn;
    }

    /**
     * 删除服务器文件
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static boolean delServiceFile(String key) throws Exception {
        Map<String, Object> file = fileUtil.fileService.selectFile(key);
        if (ValidateUtil.isEmpty(file)) {
            return false;
        }
        CxfState cxfState = FileUtil.isCxfOnline();
        //服务器是否在线
        if (!cxfState.isOnline()) {
            return false;
        }

        String SF_NAME = TextUtil.toString(file.get("SF_NAME"));
        String SF_TABLE_NAME = TextUtil.toString(file.get("SF_TABLE_NAME"));
        String SF_PATH = TextUtil.toString(file.get("SF_PATH"));

        boolean isSuccess = delServerFile(cxfState.getUrl(), TokenUtil.baseKey(key, SF_TABLE_NAME), SF_NAME, SF_PATH);
        if (isSuccess) {
            fileUtil.fileService.deleteFile(key);
        }
        return isSuccess;
    }

    /**
     * 删除服务器文件
     *
     * @param url
     * @param token
     * @param name
     * @param path
     * @return
     * @throws Exception
     */
    public static boolean delServerFile(URL url, String token, String name, String path) throws Exception {
        // 删除附件
        FileWSImplService ss = new FileWSImplService(url);
        FileWS port = ss.getFileWSImplPort();
        CxfFileWrapper deletefile = new CxfFileWrapper();
        deletefile.setFileName(name);
        deletefile.setFilePath(path);
        deletefile.setFileToken(token);
        boolean deletereturn = port.delete(deletefile);
        return deletereturn;
    }

    /**
     * 根据InputStream 获取BYTE数组
     *
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }

            return output.toByteArray();
        } catch (Exception e) {
            throw e;
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * 根据文件获取BYTE数组
     *
     * @param file
     * @return
     */
    public static byte[] toByteArray(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return toByteArray(fis);
    }

    /**
     * 根据文件路径获取BYTE数组
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(String filePath) throws IOException {
        return toByteArray(new File(filePath));
    }

    /**
     * 获取MD5
     *
     * @param file
     * @return
     */
    public static String getFileMd5(MultipartFile file) {
        byte[] uploadBytes = null;
        try {
            uploadBytes = file.getBytes();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] digest = md5.digest(uploadBytes);
        String hashString = new BigInteger(1, digest).toString(16);
        return hashString.toUpperCase();
    }

    /**
     * 获取MD5
     *
     * @param bytes
     * @return
     */
    public static String getFileMd5(byte[] bytes) {
        byte[] uploadBytes = bytes;

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] digest = md5.digest(uploadBytes);
        String hashString = new BigInteger(1, digest).toString(16);
        return hashString.toUpperCase();
    }

    /**
     * 保存文件到本地
     *
     * @param inputStream
     * @param path
     * @return
     */
    public static boolean saveFileToLocal(InputStream inputStream, String path) {
        OutputStream os = null;
        try {
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流保存到本地文件
            File tempFile = new File(path);
            String mkdirPath = tempFile.isDirectory() ? tempFile.getPath() : tempFile.getParent();
            tempFile = new File(mkdirPath);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }

            os = new FileOutputStream(path);
            // 开始读取
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 完毕，关闭所有链接
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * InputStream转为String
     *
     * @param in
     * @return
     */
    public static String convertToString(InputStream in) {
        BufferedReader bf = null;
        try {
            //最好在将字节流转换为字符流的时候 进行转码
            bf = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = bf.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        } catch (Exception e) {
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 转换为InputStream
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream getInputStreamByFilePath(String filePath) throws FileNotFoundException {
        return new FileInputStream(new File(filePath));
    }

    /**
     * 浏览器下载
     *
     * @param response
     * @param inputStream
     * @param fileName
     * @param prefix      后缀
     * @return
     * @throws IOException
     */
    public static boolean download(HttpServletResponse response, InputStream inputStream, String fileName, String prefix) throws IOException {
        byte[] b = null;

        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
            // 设置下载文件名
            String pdf = new String(new String(fileName.getBytes(), "ISO-8859-1")) + "." + prefix;

            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment;filename=" + pdf);
            b = new byte[1024];
            int len = 0;
            while ((len = bufferedInputStream.read(b)) != -1) {
                bufferedOutputStream.write(b, 0, len);
            }
            bufferedOutputStream.flush();

            System.out.println(DateUtil.getDate() + "下载文件,项目名:" + fileName + prefix);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println("浏览器下载异常...");
        } finally {
            try {
                b = null;
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
            } catch (IOException e) {
                // e.printStackTrace();
//                System.out.println("浏览器下载IO异常...");
            }
        }

        return false;
    }

    /**
     * byte[] 转为MultipartFile
     *
     * @param base64
     * @return
     */
    public static MultipartFile base64ToMultipart(String base64) {
        try {
            String[] baseStrs = base64.split(",");

            BASE64Decoder decoder = new BASE64Decoder();
            byte[] b = new byte[0];
            b = decoder.decodeBuffer(baseStrs[1]);

            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }

            return new BASE64DecodedMultipartFile(b, baseStrs[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件路径字段base64
     *
     * @param list
     * @param field
     */
    public static void filePathTobase64(List<Map<String, Object>> list, String field) {
        if (list != null) {
            for (Map<String, Object> map : list) {
                filePathTobase64(map, field);
            }
        }
    }

    /**
     * 文件路径字段base64
     *
     * @param map
     * @param field
     */
    public static void filePathTobase64(Map<String, Object> map, String field) {
        if (map != null) {
            try {
                String val = TextUtil.toString(map.get(field));
                if (val.contains(",")) {
                    //数组重新拼接
                    val = Arrays.stream(val.split(",")).map(m -> {
                        try {
                            return new String(Base64.encode(m).getBytes("UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            return "";
                        }
                    }).collect(Collectors.joining(","));
                } else {
                    //地址是视频 添加缩略图位置
                    if (FileUtil.isCheckSuffix(val, ConfigProperties.ALLOW_SUFFIX_VIDEO)) {
                        String thumbnails = val.substring(0, val.lastIndexOf("."));
                        map.put(field + "_THUMBNAILS", new String(Base64.encode(thumbnails + "-thumbnails.jpg").getBytes("UTF-8")));
                    }
                    val = new String(Base64.encode(val).getBytes("UTF-8"));
                }
                map.put(field, val);
            } catch (UnsupportedEncodingException e) {
            }
        }
    }

    /**
     * OutputStream 转为 InputStream
     *
     * @param out
     * @return
     * @throws Exception
     */
    public static InputStream parse(OutputStream out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(isCxfOnline());
    }
}
