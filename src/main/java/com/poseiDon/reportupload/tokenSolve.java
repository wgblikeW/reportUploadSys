package com.poseiDon.reportupload;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * 解码前端传输过来的token，验证登录的合法性
 */
public class tokenSolve {

    public static boolean verify(String token) {
        try {
            // TODO 条件码返回 支持前端认证
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
