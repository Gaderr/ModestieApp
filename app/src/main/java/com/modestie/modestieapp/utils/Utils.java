package com.modestie.modestieapp.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils
{
    public static String getBase64Image(Bitmap bmp)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static byte[] getSHA256Hash(String data)
    {
        MessageDigest digest=null;
        try
        {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e1)
        {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(data.getBytes());
    }

    public static String bin2hex(byte[] data)
    {
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
    }
}
