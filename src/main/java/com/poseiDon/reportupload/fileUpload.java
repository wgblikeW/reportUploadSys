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
 * ʵ��ʵ�鱨���ϴ��߼�
 */
@WebServlet(name = "fileupload", value = "/fileupload")
public class fileUpload extends HttpServlet {
    /*
    ʵ�鱨���ϴ� ǰ���Ѵ����ϴ����ļ��������С����
     */

    // �ϴ��ļ��洢Ŀ¼
    private static final String UPLOAD_DIRECTORY = "uploadReport";

    // �ϴ�����
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 10;  // ��������ڴ�������ֵΪ 10Mb
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 10; // ������������ļ���СΪ10Mb
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // ������������СΪ50Mb

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
            uploadFile.createNewFile(); // ������Ӧ���ϴ��ļ�
        }
        return uploadFile;
    }

    private ServletFileUpload beforeReceive(HttpServletRequest request) {

        DiskFileItemFactory DiskFactory = new DiskFileItemFactory();
        DiskFactory.setSizeThreshold(MEMORY_THRESHOLD); // ���������ڴ���ֵ
        DiskFactory.setRepository(new File(System.getProperty("java.io.tmpdir"))); // ����Ĭ�ϵ�tmpĿ¼

        ServletFileUpload fileUpload = new ServletFileUpload(DiskFactory);
        fileUpload.setFileSizeMax(MAX_FILE_SIZE); // ��������ϴ��ļ���С����
        fileUpload.setSizeMax(MAX_REQUEST_SIZE); // ������������С
        fileUpload.setHeaderEncoding("UTF-8"); // ����ͷ������

        return fileUpload;
    }

    private void processReceiveData(List<?> items, String Path) throws Exception {
        Map dict = new HashMap<String, String>(); // ���ڽ���receive����ͷ������
        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (item.isFormField()) {
                String name = item.getFieldName(); // ��ȡͷ�����е���Ϣ ͷ�����а���token �� proj_name proj_id
                String value = item.getString();
                dict.put(name, value);
            } else {
                DecodedJWT jwt = tokenSolve.Decrypt((String) dict.get("token")); // ����toekn
                String className = jwt.getClaim("class").asString(); // �༶���� e.g �Ű�191
                String userName = jwt.getClaim("username").asString(); // �û�����  e.g 1915300017
                String proj_nm = (String) dict.get("proj_name"); // ��Ŀ���� e.g ���ݿ��ѯʹ��
                String proj_id = (String) dict.get("proj_id");
                proj_nm = new String(Base64.decode(proj_nm), Charset.forName("UTF-8")); // ʹ��base64���봫��������������� ��������໥ת��������

                sqlQuery query = new sqlQuery();
                query.conformSubmit(userName, proj_nm);
                query.CloseConn();

                String fileName = proj_nm + "-" + userName + "-" + className + ".pdf";
                // �𼶹����ļ���
                File uploadFile = fileCreate(Path, proj_nm, className, fileName, proj_id); // ����Ŀ¼·��
                item.write(uploadFile); // ��Զ�̻�ȡ������д���ļ�
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

        ServletFileUpload fileUpload = beforeReceive(request); // ��ʼ���������

        String Path = request.getServletContext().getRealPath("./") + File.separator +
                UPLOAD_DIRECTORY; //����ʵ�鱨��洢·��
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
