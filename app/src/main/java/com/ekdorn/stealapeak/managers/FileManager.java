package com.ekdorn.stealapeak.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileManager {
    public static final String PROFILE_IMAGES = "profile_images" + File.pathSeparator;

    public static void setFileInChat() {

    }

    public static void setProfilePic(String phone, Drawable pic, Context context) {
        try {
            //FileInputStream inStream = context.openFileInput(phone);
            FileOutputStream outStream = context.openFileOutput(PROFILE_IMAGES + phone, Context.MODE_PRIVATE);
            //FileChannel inChannel = inStream.getChannel();
            //FileChannel outChannel = outStream.getChannel();
            //inChannel.transferTo(0, inChannel.size(), outChannel);
            //inStream.close();

            Bitmap bitmap = ((BitmapDrawable) pic).getBitmap();
            ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
            bitmapStream.writeTo(outStream);

            outStream.flush();
            outStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Toast.makeText(context, "Image writing exception", Toast.LENGTH_SHORT).show();
        }
    }
}
