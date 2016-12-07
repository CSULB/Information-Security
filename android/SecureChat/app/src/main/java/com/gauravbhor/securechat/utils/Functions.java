package com.gauravbhor.securechat.utils;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.gauravbhor.securechat.activities.ChatActivity;

import org.json.JSONObject;
import org.libsodium.jni.Sodium;
import org.libsodium.jni.SodiumConstants;

import java.nio.charset.StandardCharsets;
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

    public static JSONObject getEncryptedMessage(String message, int type, Context context, byte[] receiverPublicKey, byte[] selfPrivateKey, long yourID) {

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] cipher1 = new byte[Sodium.crypto_secretbox_macbytes() + messageBytes.length];
        byte[] nonce1 = new byte[Sodium.crypto_secretbox_noncebytes()];
        byte[] symmetricKey = new byte[SodiumConstants.SECRETKEY_BYTES];

        Sodium.randombytes(nonce1, nonce1.length);
        Sodium.randombytes(symmetricKey, symmetricKey.length);

        // Encrypt the message
        if (Sodium.crypto_secretbox_easy(cipher1, messageBytes, messageBytes.length, nonce1, symmetricKey) != 0) {
            Toast.makeText(context, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
            return null;
        }

        byte[] cipher2 = new byte[Sodium.crypto_box_macbytes() + symmetricKey.length];
        byte[] nonce2 = new byte[Sodium.crypto_box_noncebytes()];
        Sodium.randombytes(nonce2, nonce2.length);

        // Encrypt the key
        if (Sodium.crypto_box_easy(cipher2, symmetricKey, symmetricKey.length, nonce2, receiverPublicKey, selfPrivateKey) != 0) {
            Toast.makeText(context, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
            return null;
        }

        try {
            JSONObject mess = new JSONObject();
            mess.put("type", type);
            mess.put("nonce1", Base64.encodeToString(nonce1, StaticMembers.BASE64_SAFE_URL_FLAGS));
            mess.put("nonce2", Base64.encodeToString(nonce2, StaticMembers.BASE64_SAFE_URL_FLAGS));
            mess.put("message1", Base64.encodeToString(cipher1, StaticMembers.BASE64_SAFE_URL_FLAGS));
            mess.put("message2", Base64.encodeToString(cipher2, StaticMembers.BASE64_SAFE_URL_FLAGS));
            mess.put("length", String.valueOf(symmetricKey.length));
            System.out.println("length: " + String.valueOf(symmetricKey.length));

            JSONObject parent = new JSONObject();
            parent.put("sender_id", yourID);
            parent.put("message", mess.toString());

            return parent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getEncryptedGroupMessage() {
        return null;
    }
}
