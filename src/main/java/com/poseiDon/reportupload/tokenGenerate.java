package com.poseiDon.reportupload;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.Algorithm;

import javax.xml.transform.Result;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成用户验证token
 * token域中包括用户名、密码、失效时间、学生所属班级
 */
public class tokenGenerate {

    private static final long EXPIRE_DATE = 30 * 60 * 100000;
    public static final String TOKEN_SECRET = "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEAsmqTJ9rRhZg1L9Dl" +
            "8UJKmJG3o4EpkM99tc6CBHvrPnWREyw21ycYeNdJ415YC/0ksYiBqdHZb65PLli3" +
            "0XM8GwIDAQABAkAuq3AY0cVS09C91EABztxicmN98pBj39K7VuxtrdiIOppQv24L" +
            "xTJ0tC0j/lTm4/VK43SGikiBxl94WJoeJzkhAiEA6oZRn8vTRz3FMUZIOUQzay8T" +
            "FE0b0G4f8o4wm+kmHzECIQDCwPl9anaX51SrtARKoszuu92f/F1n599uOJvVntb1" +
            "CwIhAM9OehqBprojrQqYcMFGOxl03C2m135Pyiezbt5yooMRAiEAohADjSHy3iVa" +
            "iCedCp6++krK+j7/W1/Qxd/FjdZH+lECIQCnKHPm82z/oGFNNxXrkCfSzgTcYGKQ" +
            "mSY++kLc/uIlWA==";

    public static String token(String username, String password, boolean isTeacher) {
        try {
            sqlQuery query = new sqlQuery();
            String _class = query.ofClass(username);
            Date date = new Date(System.currentTimeMillis() + EXPIRE_DATE);
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            Map<String, Object> header = new HashMap<>();
            header.put("typ", "JWT");
            header.put("alg", "HS256");
            String token;
            if (isTeacher) {
                String teacherID;
                teacherID = query.teacherID(username);
                token = JWT.create()
                        .withHeader(header)
                        .withClaim("username", username)
                        .withClaim("password", password)
                        .withClaim("teacher_id", teacherID)
                        .withExpiresAt(date)
                        .sign(algorithm);
            } else {
                token = JWT.create()
                        .withHeader(header)
                        .withClaim("username", username)
                        .withClaim("password", password)
                        .withClaim("class", _class)
                        .withExpiresAt(date)
                        .sign(algorithm);
            }
            query.CloseConn();
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
