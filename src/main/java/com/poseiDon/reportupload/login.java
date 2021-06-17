package com.poseiDon.reportupload;

import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;

/**
 * ʵ���û���¼��֤�߼�
 */
@WebServlet(name = "login", value = "/login")
public class login extends HttpServlet {
    boolean INITIALIZED = false;

    private boolean checkUser(String username, String password, boolean isTeacher) {
        /**
         * ��¼�ؼ����裬ͨ���û������������û��ĺϷ��� �û���������ǰ��ʹ��sha256���м���
         * mysql student table�д洢��password�ֶ����ݼ�Ϊ���ܺ���ַ���
         * @param username �������û���
         * @param password ����������
         */
        try {
            ResultSet resultSet;
            sqlQuery login = new sqlQuery();
            resultSet = login.loginQuery(username, password, isTeacher);
            boolean checkFlag = false;
            while (resultSet.next()) {
                checkFlag = true;
                if (!isTeacher) INITIALIZED = resultSet.getBoolean("student_initialized");
            }
            resultSet.close();
            login.CloseConn();
            return checkFlag;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("��¼ģ������쳣����λ��Ϣ:checkUser");
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /**
         * �ýӿڴ����¼���񣬵����յ���¼����ʱ��֤�ύ���û���������
         * ͨ�������Է��� �����򷵻ؾܾ�����
         */
        INITIALIZED = false;
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/json; charset=utf-8");
        boolean isTeacher;
        // ǰ��ʹ��GET��ʽ�������ύ��Ϣ ����Ϊbase64
        String token = req.getParameter("withToken");
        if (token != null) {
            token = new String(Base64.getDecoder().decode(token), Charset.forName("UTF-8"));
            withToken(token, resp);
        } else {
            if (req.getParameter("isTeacher") != null && req.getParameter("isTeacher").equals("teacher")) {
                isTeacher = true;
            } else isTeacher = false;
            withoutToken(resp, req, isTeacher);
        }
    }

    private void withToken(String token, HttpServletResponse resp) throws IOException {
        PrintWriter pw = resp.getWriter();
        HashMap<String, String> map = new HashMap<>();
        if (tokenSolve.verify(token)) {
            map.put("result", "ACCESSED");
        } else {
            map.put("result", "DENIED");
        }
        pw.write(JSONObject.toJSONString(map));
        pw.flush();
        pw.close();
    }

    private void withoutToken(HttpServletResponse resp, HttpServletRequest req, boolean isTeacher) throws IOException {
        String username = req.getParameter("username");
        username = new String(Base64.getDecoder().decode(username), Charset.forName("UTF-8"));
        String password = req.getParameter("password");
        password = new String(Base64.getDecoder().decode(password), Charset.forName("UTF-8"));

        PrintWriter pw = resp.getWriter(); // ����¼��֤���������ǰ��
        HashMap<String, String> map = new HashMap<>();
        // ��֤�û���������ĺϷ���
        if (checkUser(username, password, isTeacher)) {
            if (!isTeacher && !INITIALIZED) {
                map.put("result", "INITIALIZED");
            } else {
                map.put("result", "ACCESSED");
                map.put("token", tokenGenerate.token(username, password, isTeacher)); // �Ϸ��û� ���� token
            }
        } else {
            map.put("result", "DENIED");
        }
        pw.write(JSONObject.toJSONString(map));
        pw.flush();
        pw.close();
    }
}
