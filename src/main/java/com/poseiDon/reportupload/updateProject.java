package com.poseiDon.reportupload;

import net.iharder.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;

@WebServlet(name = "updateProject", value = "/updateProject")
public class updateProject extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        InputStream inputStream = req.getInputStream();
        byte[] buf = new byte[1];
        String data = "";
        while (inputStream.read(buf) != -1) {
            data = data + new String(buf);
        }

        try {
            JSONObject objJson = (JSONObject) new JSONParser().parse(data);
            HashMap<String, String> dict = (HashMap<String, String>) objJson.get("updateProject");
            String id = new String(Base64.decode(dict.get("id")), Charset.forName("UTF-8"));
            String name = new String(Base64.decode(dict.get("name")), Charset.forName("UTF-8"));
            String course = new String(Base64.decode(dict.get("course")), Charset.forName("UTF-8"));
            String class_ = new String(Base64.decode(dict.get("class")), Charset.forName("UTF-8"));
            String deadline = new String(Base64.decode(dict.get("deadline")), Charset.forName("UTF-8"));
            int class_id = Integer.valueOf(class_);
            updateProjectQuery(id, name, course, class_id, deadline);
            PrintWriter pw = resp.getWriter();
            HashMap<String, String> output = new HashMap<>();
            output.put("status", "success");
            System.out.println(JSONObject.toJSONString(output));
            pw.write(JSONObject.toJSONString(output));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProjectQuery(String id, String name, String course, int class_id, String deadline) throws Exception {
        sqlQuery query = new sqlQuery();
        query.updateProject(id, name, course, class_id, deadline);
    }
}
