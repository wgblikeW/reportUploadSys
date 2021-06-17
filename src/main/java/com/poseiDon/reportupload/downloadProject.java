package com.poseiDon.reportupload;

import com.alibaba.fastjson.JSON;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;

@WebServlet(name = "getReport", value = "/getReport")
public class downloadProject extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int len = 0;
        byte[] buf = new byte[4096];
        String project_id = req.getParameter("id");
        resp.setContentType("application/x-download");
        resp.setHeader("Content-Disposition", "attachment; filename=1.rar");
        String PATH = req.getServletContext().getRealPath("./") + File.separator + "uploadReport" + File.separator + project_id + ".zip";
        File file = new File(PATH);
        FileInputStream fileInputStream = new FileInputStream(file);
        ServletOutputStream servletOutputStream = resp.getOutputStream();
        while ((len = fileInputStream.read(buf)) != -1) {
            servletOutputStream.write(buf, 0, len);
        }
        servletOutputStream.flush();
        servletOutputStream.close();
        fileInputStream.close();
    }
}
