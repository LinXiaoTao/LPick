package com.china.leo.lpick.image.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 拍照工具类
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-21 下午5:28
 */

public final class CameraUtils
{
    private final static String IMAGE_STORE_FILE_NAME = "IMG_%s.jpg";

    /**
     * 判断设备是否有摄像头
     *
     * @param context
     * @return
     */
    public static boolean hasCamera(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
        {
            return false;
        }
        return true;
    }

    /**
     * 打开拍照
     */
    public static File openCamera(Activity context, int requestCode)
    {
        File saveFile = createSaveFile(context);
        Logger.d("图片路径:" + saveFile.getAbsolutePath());
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(context.getPackageManager()) != null)
        {
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveFile));
            context.startActivityForResult(captureIntent, requestCode);
        }
        return saveFile;
    }

    /**
     * 创建保存照片文件夹
     */
    public static File createSaveFile(Context context)
    {
        File dir = null;
        if (hasSDCardMounted())
        {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!dir.exists())
                dir.mkdirs();
        } else
        {
            dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        String filename = String.format(IMAGE_STORE_FILE_NAME, dateFormat.format(new Date()));
        return new File(dir, filename);
    }

    /**
     * 判断外部SD是否可用
     */
    public static boolean hasSDCardMounted()
    {
        String state = Environment.getExternalStorageState();
        if (state != null && state.equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        } else
        {
            return false;
        }
    }


}
