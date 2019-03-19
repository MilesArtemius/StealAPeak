package com.ekdorn.stealapeak.managers;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class CryptoManager {
    private static final String ALGORITHM   = "RSA";
    private static final String ENCODING    = "UTF-16";
    private static final String BROKEN      = "BROKEN_MESSAGE";
    private static final String PRIVATE_KEY = "private_key";
    private static final String PUBLIC_KEY  = "public_key";

    public static void init(Context context) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(1024);
            KeyPair pair = keyGen.generateKeyPair();

            String publicKey = Base64.encodeToString(pair.getPublic().getEncoded(), Base64.URL_SAFE);
            String privateKey = Base64.encodeToString(pair.getPrivate().getEncoded(), Base64.URL_SAFE);
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(PRIVATE_KEY, privateKey)
                    .putString(PUBLIC_KEY, publicKey)
                    .apply();
        } catch (NoSuchAlgorithmException nsae) {
            Toast.makeText(context, "Something happened with encryption...", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPublicKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PUBLIC_KEY, "");
    }

    public static String decode(Context context, String message) {
        if (message.equals(BROKEN)) return BROKEN;

        String privateKey = PreferenceManager.getDefaultSharedPreferences(context).getString(PRIVATE_KEY, "");
        byte[] byteKey = Base64.decode(privateKey, Base64.URL_SAFE);

        String result;
        try {
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey key = factory.generatePrivate(new PKCS8EncodedKeySpec(byteKey));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            result = new String(cipher.doFinal(Base64.decode(message, Base64.DEFAULT)), ENCODING);
        } catch (Exception e) {
            e.fillInStackTrace();
            Toast.makeText(context, "Encryption error", Toast.LENGTH_SHORT).show();
            result = BROKEN;
        }
        return result;
    }

    public static String encode(String message, String publicKey) {
        byte[] byteKey = Base64.decode(publicKey, Base64.URL_SAFE);

        String result;
        try {
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
            PublicKey key = factory.generatePublic(new X509EncodedKeySpec(byteKey));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            result = Base64.encodeToString(cipher.doFinal(message.getBytes(ENCODING)), Base64.DEFAULT);
        } catch (Exception e) {
            e.fillInStackTrace();
            result = BROKEN;
        }
        return result;
    }
}
