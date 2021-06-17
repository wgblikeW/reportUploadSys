package com.poseiDon.reportupload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.simple.*;

/**
 * 获取数据库中用户请求的实验项目信息并打包成JSON返回给前端
 */

@WebServlet(name = "getreport", value = "/getreport")
public class getReportStatus extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String token = req.getParameter("token");
        resp.setContentType("text/json; charset=utf-8");
        HashMap<String, String> dict = new HashMap<>();
        HashMap<String, String> url = new HashMap<>();
        ArrayList<Object> reqJ = new ArrayList<>();
        ArrayList<Object> list = new ArrayList<>();
        DecodedJWT jwt = tokenSolve.Decrypt(token);
        String username = jwt.getClaim("username").asString();
        try {

            ResultSet resultSet;
            sqlQuery query = new sqlQuery();
            resultSet = query.StatusQuery(username);
            JSONObject objJson = new JSONObject();
            // TODO 提交情况JSON proj_submit
            while (resultSet.next()) {
                dict.put("proj_id", String.valueOf(resultSet.getInt("project_id")));
                dict.put("proj_name", resultSet.getString("project_name"));
                dict.put("proj_class", resultSet.getString("project_course"));
                dict.put("proj_prof", resultSet.getString("project_teacher"));
                dict.put("proj_ddl", resultSet.getString("project_deadline"));

                if (query.getSubmitStatus(username, dict.get("proj_name"), dict.get("proj_class"))) {
                    dict.put("proj_submit", "submit");
                } else {
                    dict.put("proj_submit", "no-submit");
                }

                list.add(resultSet.getString("project_url"));
                reqJ.add(dict.clone()); // reqJ.add(dict) 添加的是dict对象 其所有的元素都是dict本身 而不会发生变化（随着dict变化而变化）
                dict.clear();
            }
            objJson.put("results", reqJ);
            objJson.put("urlsets", list);
            PrintWriter pw = resp.getWriter();
            pw.write(objJson.toJSONString());
            pw.flush();

            pw.close();
            resultSet.close();
            query.CloseConn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
