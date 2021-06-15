package com.poseiDon.reportupload;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import net.iharder.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@WebServlet(name = "addClass", value = "/addClass")
public class addClass extends HttpServlet {

    private void alterTeacherManageID(String teacher_id, String class_id) throws Exception {
        sqlQuery query = new sqlQuery();
        System.out.println(teacher_id + " " + class_id);
        query.updateTeacherManageID(teacher_id, class_id);
    }

    private boolean checkClass(String class_name) {
        try {
            sqlQuery query = new sqlQuery();
            return query.checkClass(class_name);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String addClass(String class_name, String class_student_id) throws Exception {
        sqlQuery query = new sqlQuery();
        class_student_id = class_student_id.substring(0, class_student_id.length() - 1);
        return query.updateClass(class_name, class_student_id);
    }


    private void JsonResp(PrintWriter pw, String status) throws Exception {
        JSONObject objJson = new JSONObject();
        HashMap<String, String> dict = new HashMap<>();
        dict.put("status", status);
        pw.write(objJson.toJSONString(dict));
        pw.flush();
        pw.close();
    }

    private void addStudent(String class_student_id) throws Exception {
        sqlQuery query = new sqlQuery();
        if (!query.whetherInstduent(class_student_id)) {
            String init_password = "123456";
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(init_password.getBytes(StandardCharsets.UTF_8));
            String encoded_password = Hex.encodeHexString(bytes);
            query.addNewStudent(class_student_id, encoded_password);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
//        req.setCharacterEncoding("gbk");
        InputStream inputStream = req.getInputStream();
        byte[] buf = new byte[1];
        String data = "";
        while (inputStream.read(buf) != -1) {
            data = data + new String(buf);
        }
        try {
            JSONObject objJson = (JSONObject) new JSONParser().parse(data);
            HashMap<String, String> dict = (HashMap<String, String>) objJson.get("newClass");
            String class_name = new String(Base64.decode(dict.get("class_name")), Charset.forName("UTF-8"));
//            System.out.println(class_name);
            if (!checkClass(class_name)) {
                JsonResp(resp.getWriter(), "failure");
                return;
            }
            String token = dict.get("token");
            DecodedJWT jwt = tokenSolve.Decrypt(token);
            String teacher_id = jwt.getClaim("teacher_id").asString();
            String class_student_id = dict.get("class_student_id");
            String[] class_student_id_split = class_student_id.split("\n");
            AtomicReference<String> class_student_id_list = new AtomicReference<>("");
            Arrays.stream(class_student_id_split).forEach(item -> {
                try {
                    addStudent(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                class_student_id_list.set(class_student_id_list + item + ";");
            });
            String class_id = addClass(class_name, String.valueOf(class_student_id_list));
            alterTeacherManageID(teacher_id, class_id);
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
