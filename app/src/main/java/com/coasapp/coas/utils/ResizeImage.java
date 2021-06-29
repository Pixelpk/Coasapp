package com.coasapp.coas.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ResizeImage {

    public static String getResizedImage(String filePath) {


        String resizedImageFile;
        int maxWidthHeight = 1200;
        double factor = 1;
        int newWidth, newHeight;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //bmOptions.inSampleSize=8;
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap imageRotate = rotateBitmap(bitmap, orientation);
            int originalWidth = imageRotate.getWidth();
            int originalHeight = imageRotate.getHeight();

            if (originalWidth > originalHeight) {
                factor = (double) originalWidth / maxWidthHeight;
                newWidth = maxWidthHeight;
                newHeight = (int) Math.round(originalHeight / factor);
            } else {
                factor = (double) originalHeight / maxWidthHeight;
                newHeight = maxWidthHeight;
                newWidth = (int) Math.round(originalWidth / factor);
            }
            APPHelper.showLog("Size", factor + " " + originalWidth + " " + originalHeight + " " + newWidth + " " + newHeight);

            if (factor >= 1) {
                imageRotate = Bitmap.createScaledBitmap(imageRotate, newWidth, newHeight, true);

            } else {
                imageRotate = Bitmap.createScaledBitmap(imageRotate, originalWidth, originalHeight, true);

            }
           /* newHeight = originalHeight / factor;
            newWidth = originalWidth / factor;*/
            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/.ResizedImages");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
            String s = "Resize_" + dateFormat.format(calendar.getTime()) + ".jpg";

            File f = new File(mFolder.getAbsolutePath(), s);

            resizedImageFile = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                imageRotate.compress(Bitmap.CompressFormat.JPEG, 99, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                resizedImageFile = filePath;
                Log.d("Image", e.getMessage());
            } catch (Exception e) {

                e.printStackTrace();
            }

            return resizedImageFile;
        } catch (IOException e) {
            e.printStackTrace();
            resizedImageFile = filePath;
        }
        return resizedImageFile;
        //return filePath;
        //return filePath;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

}
