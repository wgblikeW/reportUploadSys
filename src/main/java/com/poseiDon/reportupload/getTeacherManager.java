package com.poseiDon.reportupload;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@WebServlet(name = "getTeacherManager", value = "/getTeacherManager")
public class getTeacherManager extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/json; charset=utf-8");

        String token = req.getParameter("token");
        HashMap<String, String> dict = new HashMap<>();
        ArrayList<Object> reqJ = new ArrayList<>();
        DecodedJWT jwt = tokenSolve.Decrypt(token);
        String teacher_id = jwt.getClaim("teacher_id").asString();

        try {
            // TODO
            ResultSet resultSet;
            sqlQuery status = new sqlQuery();
            resultSet = status.StatusQueryFromTeacher(teacher_id);
            resultSet.next();
            String project_id = resultSet.getString("teacher_project_id");
            String[] project_id_list;
            project_id_list = project_id.split(";");
            // TODO 字符串处理 00001;00002;00003;
            JSONObject objJson = new JSONObject();
            for (int i = 0; i < project_id_list.length; i++) {
                resultSet = status.project_info(project_id_list[i]);
                while (resultSet.next()) {
                    // TODO
                    dict.put("project_id", resultSet.getString("project_id"));
                    dict.put("project_name", resultSet.getString("project_name"));
                    dict.put("project_deadline", resultSet.getString("project_deadline"));
                    dict.put("project_course", resultSet.getString("project_course"));
                    dict.put("project_submitted", String.valueOf(status.statistic_submit_members(project_id_list[i])));
                    dict.put("project_headcount", String.valueOf(status.statistic_members(project_id_list[i])));
                    dict.put("class_name", status.project_class(project_id_list[i]));
                }
                reqJ.add(dict.clone()); // reqJ.add(dict) 添加的是dict对象 其所有的元素都是dict本身 而不会发生变化（随着dict变化而变化）
                dict.clear();
            }
            objJson.put("results", reqJ);
            PrintWriter pw = resp.getWriter();
            pw.write(objJson.toJSONString());
            pw.flush();

            pw.close();
            resultSet.close();
            status.CloseConn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
