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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@WebServlet(name = "getClass", value = "/getClass")
public class getClass extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String token = req.getParameter("token");
        DecodedJWT jwt = tokenSolve.Decrypt(token);
        String teacher_id = jwt.getClaim("teacher_id").asString();
        ArrayList<Object> list = new ArrayList<>();
        try {
            sqlQuery query = new sqlQuery();
            String class_name_class_id = query.getTeacherManageClass(teacher_id);
            if (class_name_class_id.equals("")) {
                reactResp(resp.getWriter(), list);
                return;
            }
            String[] class_student_id_list = class_name_class_id.split(";");
            HashMap<String, String> dict = new HashMap<>();
            Arrays.stream(class_student_id_list).forEach(id -> {
                try {
                    dict.put("id", id);
                    dict.put("name", query.getClassName(id));
                    list.add(dict.clone());
                    dict.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            reactResp(resp.getWriter(), list);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void reactResp(PrintWriter pw, ArrayList<Object> list) {
        JSONObject objJson = new JSONObject();
        objJson.put("results", list);
        pw.write(objJson.toJSONString());
        pw.flush();
        pw.close();
    }
}
