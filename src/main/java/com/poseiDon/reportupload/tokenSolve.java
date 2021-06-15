package com.poseiDon.reportupload;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * ����ǰ�˴��������token����֤��¼�ĺϷ���
 */
public class tokenSolve {

    public static boolean verify(String token) {
        try {
            // TODO �����뷵�� ֧��ǰ����֤
            Algorithm algorithm = Algorithm.HMAC256(tokenGenerate.TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static DecodedJWT Decrypt(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(tokenGenerate.TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
