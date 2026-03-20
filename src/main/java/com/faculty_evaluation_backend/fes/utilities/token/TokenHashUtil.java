package com.faculty_evaluation_backend.fes.utilities.token;


import java.security.MessageDigest;

public class TokenHashUtil {
    public static String sha256(String token){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash){
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        }catch (Exception e){
            throw new RuntimeException("Error hashing token",e);
        }
    }
}
