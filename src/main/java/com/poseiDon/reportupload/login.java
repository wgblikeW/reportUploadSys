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
 * 实现用户登录验证逻辑
 */
@WebServlet(name = "login", value = "/login")
public class login extends HttpServlet {
    boolean INITIALIZED = false;

    private boolean checkUser(String username, String password, boolean isTeacher) {
        /**
         * 登录关键步骤，通过用户名与密码检查用户的合法性 用户密码已在前端使用sha256进行加密
         * mysql student table中存储的password字段内容即为加密后的字符串
         * @param username 待检测的用户名
         * @param password 待检测的密码
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
            System.out.println("登录模块出现异常。定位信息:checkUser");
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /**
         * 该接口处理登录事务，当接收到登录请求时验证提交的用户名与密码
         * 通过则予以放行 否则则返回拒绝访问
         */
        INITIALIZED = false;
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/json; charset=utf-8");
        boolean isTeacher;
        // 前端使用GET方式以明文提交信息 编码为base64
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

        PrintWriter pw = resp.getWriter(); // 将登录验证结果返回至前端
        HashMap<String, String> map = new HashMap<>();
        // 验证用户名与密码的合法性
        if (checkUser(username, password, isTeacher)) {
            if (!isTeacher && !INITIALIZED) {
                map.put("result", "INITIALIZED");
            } else {
                map.put("result", "ACCESSED");
                map.put("token", tokenGenerate.token(username, password, isTeacher)); // 合法用户 生成 token
            }
        } else {
            map.put("result", "DENIED");
        }
        pw.write(JSONObject.toJSONString(map));
        pw.flush();
        pw.close();
    }
}
