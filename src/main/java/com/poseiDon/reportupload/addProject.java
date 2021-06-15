package com.poseiDon.reportupload;

import com.auth0.jwt.interfaces.DecodedJWT;
import net.iharder.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;

@WebServlet(name = "addProject", value = "/addProject")
public class addProject extends HttpServlet {

    private void processStudent_Project(sqlQuery query, String class_student_id, String project_name) throws Exception {
        String[] class_student_id_list = class_student_id.split(";");
        String project_id = query.getProjectID(project_name);
        Arrays.stream(class_student_id_list).forEach(student_id -> {
            try {
                query.updateStudentProject(student_id, project_id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ArrayList<String> processReceiveData(HttpServletRequest request) throws Exception {
        // �ϴ��ļ��洢Ŀ¼
        final String UPLOAD_DIRECTORY = "report";
        // �ϴ�����
        final int MEMORY_THRESHOLD = 1024 * 1024 * 10;  // ��������ڴ�������ֵΪ 10Mb
        final int MAX_FILE_SIZE = 1024 * 1024 * 10; // ������������ļ���СΪ10Mb
        final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // ������������СΪ50Mb
        ServletFileUpload fileUpload;

        DiskFileItemFactory DiskFactory = new DiskFileItemFactory();
        DiskFactory.setSizeThreshold(MEMORY_THRESHOLD); // ���������ڴ���ֵ
        DiskFactory.setRepository(new File(System.getProperty("java.io.tmpdir"))); // ����Ĭ�ϵ�tmpĿ¼

        fileUpload = new ServletFileUpload(DiskFactory);
        fileUpload.setFileSizeMax(MAX_FILE_SIZE); // ��������ϴ��ļ���С����
        fileUpload.setSizeMax(MAX_REQUEST_SIZE); // ������������С
        fileUpload.setHeaderEncoding("UTF-8"); // ����ͷ������

        String Path = request.getServletContext().getRealPath("./") + File.separator +
                UPLOAD_DIRECTORY; //����ʵ�鱨��洢·��
        List items = fileUpload.parseRequest(request);
        Iterator iter = items.iterator();
        HashMap dict = new HashMap<String, String>();
        ArrayList list = new ArrayList<String>();

        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (item.isFormField()) {
                String key = item.getFieldName();
                String value = item.getString();
                dict.put(key, value);
            } else {
                dict.remove("class");
                dict.forEach((key, value) -> {
                    list.add(value);
                });
                System.out.println(dict);
                String fileName = new String(Base64.decode((String) list.get(2)), Charset.forName("UTF-8"));

                sqlQuery query = new sqlQuery();
                String newProjectID = String.valueOf(Integer.valueOf(query.getProjectID()) + 1);
                fileName = fileName.substring(0, fileName.indexOf(".")) + "_" + newProjectID + "." + fileName.substring(fileName.indexOf(".") + 1, fileName.length());
                String uploadFile = Path + File.separator + fileName;
                list.add(request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getServletPath())) + "/report/" + fileName);
                File file = new File(uploadFile);
                item.write(file);
            }
        }
        return list;
    }

    private void processTeachermanageProjectID(sqlQuery query, String teacher_id) throws Exception {
        query.updateTeacherManageProjectID(teacher_id);
    }

    private void JsonResp(PrintWriter pw, String status) throws Exception {
        JSONObject objJson = new JSONObject();
        HashMap<String, String> dict = new HashMap<>();
        dict.put("status", status);
        pw.write(objJson.toJSONString(dict));
        pw.flush();
        pw.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ArrayList<String> list;
        try {
            list = processReceiveData(req);
            String project_course = new String(Base64.decode(list.get(1)), Charset.forName("UTF-8"));
            String project_name = new String(Base64.decode(list.get(4)), Charset.forName("UTF-8"));
            String project_class = list.get(3);
            String project_deadline = list.get(0);
            String token = list.get(5);
            DecodedJWT jwt = tokenSolve.Decrypt(token);
            String teacher_id = jwt.getClaim("teacher_id").asString();

            sqlQuery query = new sqlQuery();
            String project_teacher = query.getTeacherName(teacher_id);
            String Path = list.get(6);
            query.constructNewProject(Path, project_course, project_name, project_teacher, project_deadline, project_class);
            String class_student_id = query.getStudentID(project_class);
            processStudent_Project(query, class_student_id, project_name);
            processTeachermanageProjectID(query, teacher_id);
            query.CloseConn();
            JsonResp(resp.getWriter(), "success");
        } catch (Exception e) {
            try {
                JsonResp(resp.getWriter(), "failure");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
