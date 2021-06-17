package com.poseiDon.reportupload;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.iharder.Base64;

/**
 * 实现实验报告上传逻辑
 */
@WebServlet(name = "fileupload", value = "/fileupload")
public class fileUpload extends HttpServlet {
    /*
    实验报告上传 前端已处理上传的文件类型与大小限制
     */

    // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "uploadReport";

    // 上传配置
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 10;  // 设置最大内存运行阈值为 10Mb
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 10; // 设置最大运行文件大小为10Mb
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 设置最大请求大小为50Mb

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    private File fileCreate(String Path, String proj_nm, String className, String fileName, String proj_id) throws IOException {
        File uploadFile = new File(Path + File.separator + proj_id);
        if (!uploadFile.exists()) {
            uploadFile.mkdir();
        }
        uploadFile = new File(Path + File.separator + proj_id + File.separator + proj_nm);
        if (!uploadFile.exists()) {
            uploadFile.mkdir();
        }
        uploadFile = new File(Path + File.separator + proj_id + File.separator + proj_nm + File.separator + className);
        if (!uploadFile.exists()) {
            uploadFile.mkdir();
        }
        uploadFile = new File(Path + File.separator + proj_id + File.separator + proj_nm + File.separator + className + File.separator + fileName);
        System.out.println(uploadFile.getAbsolutePath());
        if (!uploadFile.exists()) {
            uploadFile.createNewFile(); // 创建相应的上传文件
        }
        return uploadFile;
    }

    private ServletFileUpload beforeReceive(HttpServletRequest request) {

        DiskFileItemFactory DiskFactory = new DiskFileItemFactory();
        DiskFactory.setSizeThreshold(MEMORY_THRESHOLD); // 设置最大的内存阈值
        DiskFactory.setRepository(new File(System.getProperty("java.io.tmpdir"))); // 设置默认的tmp目录

        ServletFileUpload fileUpload = new ServletFileUpload(DiskFactory);
        fileUpload.setFileSizeMax(MAX_FILE_SIZE); // 设置最大上传文件大小限制
        fileUpload.setSizeMax(MAX_REQUEST_SIZE); // 设置最大请求大小
        fileUpload.setHeaderEncoding("UTF-8"); // 设置头部编码

        return fileUpload;
    }

    private void processReceiveData(List<?> items, String Path) throws Exception {
        Map dict = new HashMap<String, String>(); // 用于解析receive到的头部数据
        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (item.isFormField()) {
                String name = item.getFieldName(); // 获取头部域中的信息 头部域中包括token 与 proj_name proj_id
                String value = item.getString();
                dict.put(name, value);
            } else {
                DecodedJWT jwt = tokenSolve.Decrypt((String) dict.get("token")); // 解析toekn
                String className = jwt.getClaim("class").asString(); // 班级名称 e.g 信安191
                String userName = jwt.getClaim("username").asString(); // 用户名称  e.g 1915300017
                String proj_nm = (String) dict.get("proj_name"); // 项目名称 e.g 数据库查询使用
                String proj_id = (String) dict.get("proj_id");
                proj_nm = new String(Base64.decode(proj_nm), Charset.forName("UTF-8")); // 使用base64编码传输过来的中文数据 解决编码相互转换的问题

                sqlQuery query = new sqlQuery();
                query.conformSubmit(userName, proj_nm);
                query.CloseConn();

                String fileName = proj_nm + "-" + userName + "-" + className + ".pdf";
                // 逐级构建文件夹
                File uploadFile = fileCreate(Path, proj_nm, className, fileName, proj_id); // 构建目录路径
                item.write(uploadFile); // 将远程获取的数据写入文件
                confirmedSubmit(userName, proj_nm);
            }
        }
    }

    private void confirmedSubmit(String userName, String proj_nm) throws Exception {
        sqlQuery query = new sqlQuery();
        query.conformSubmit(userName, proj_nm);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ServletFileUpload fileUpload = beforeReceive(request); // 初始化传输参数

        String Path = request.getServletContext().getRealPath("./") + File.separator +
                UPLOAD_DIRECTORY; //设置实验报告存储路径
        File uploadDir = new File(Path);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
        try {
            List<?> items = fileUpload.parseRequest(request);
            processReceiveData(items, Path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
