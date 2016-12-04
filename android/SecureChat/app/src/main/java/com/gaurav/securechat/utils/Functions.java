package com.gaurav.securechat.utils;

import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Functions {
    /*
    * Credits: http://stackoverflow.com/a/33085670/2058134
    * */
    public static String hash(String algorithm, String data, String salt) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(salt.getBytes("UTF-8"));
            byte[] bytes = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    /*
    * Credits: http://stackoverflow.com/a/39356436/2058134
    * */
    public static String HMAC(String algorithm, String data, String key) {
        try {
            byte[] byteKey = key.getBytes("UTF-8");
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, algorithm);
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
