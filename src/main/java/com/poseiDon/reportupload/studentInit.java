package com.poseiDon.reportupload;

import com.auth0.jwt.interfaces.DecodedJWT;
import net.iharder.Base64;
import org.json.simple.JSONObject;

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

@WebServlet(name = "studentInit", value = "/studentInit")
public class studentInit extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String username = req.getParameter("username");
        name = new String(Base64.decode(name), Charset.forName("UTF-8"));
        try {
            sqlQuery query = new sqlQuery();
            query.updateStudent(name, email, password, username);
            PrintWriter pw = resp.getWriter();
            HashMap<String, String> dict = new HashMap<>();
            dict.put("status", "success");
            pw.write(JSONObject.toJSONString(dict));
            pw.flush();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
