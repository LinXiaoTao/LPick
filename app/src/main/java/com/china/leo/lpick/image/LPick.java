package com.china.leo.lpick.image;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.yalantis.ucrop.UCrop;

import static com.yalantis.ucrop.UCrop.EXTRA_OUTPUT_URI;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-22 上午11:16
 */

public final class LPick
{
    public static final int REQUEST_CROP = UCrop.REQUEST_CROP;
    public static final int RESULT_ERROR = UCrop.RESULT_ERROR;


    //可配置参数
    private int mPickCount = Constances.MAX_PICK_COUNT;
    private int mSpanCount = Constances.SPAN_COUNT;
    private int mPagerSize = Constances.PAGER_SIZE;
    //x , y 裁剪比例
    private float mAspectRationX;
    private float mAspectRationY;
    //裁剪的最大尺寸
    private int mMaxSizeX;
    private int mMaxSizeY;

    private UCrop.Options mOptions;
    private static LPick INSTANCE;

    public static LPick getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (LPick.class)
            {
                INSTANCE = new LPick();
            }
        }
        return INSTANCE;
    }

    private LPick()
    {
        mOptions = new UCrop.Options();
    }

    /**
     * 设置最多可选数量
     *
     * @param count
     * @return
     */
    public LPick withPickCount(int count)
    {
        mPickCount = count;
        return INSTANCE;
    }

    /**
     * 设置列数
     *
     * @param count
     * @return
     */
    public LPick withSpanCount(int count)
    {
        mSpanCount = count;
        return INSTANCE;
    }

    /**
     * 设置每次读取数量
     *
     * @param pagerSize
     * @return
     */
    public LPick withPagerSize(int pagerSize)
    {
        mPagerSize = pagerSize;
        return INSTANCE;
    }

    /**
     * x , y 裁剪比例
     *
     * @param x
     * @param y
     * @return
     */
    public LPick withAspectRatio(float x, float y)
    {
        mOptions.withAspectRatio(x, y);
        return INSTANCE;
    }

    /**
     * 设置裁剪的最大尺寸
     *
     * @param w
     * @param h
     * @return
     */
    public LPick withMaxResultSize(int w, int h)
    {
        mOptions.withMaxResultSize(w, h);
        return INSTANCE;
    }

    /**
     * 使用源图片的比例
     *
     * @return
     */
    public LPick useSourceImageAspectRatio()
    {
        mOptions.useSourceImageAspectRatio();
        return INSTANCE;
    }

    /**
     * 开启选择图片
     *
     * @param activity
     * @param requestCode
     */
    public void pick(Activity activity, int requestCode)
    {
        Intent intent = new Intent(activity, PickActivity.class);
        intent.putExtras(pickBuild());
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 开启裁剪图片
     *
     * @param source      源图片
     * @param destination 裁剪图片
     */
    public void crop(Activity activity, Uri source, Uri destination)
    {
        UCrop.of(source, destination)
                .withOptions(mOptions)
                .start(activity);
    }

    /**
     * 获取输出Uri
     *
     * @param intent
     * @return
     */
    public static Uri getOutput(Intent intent)
    {
        return intent.getParcelableExtra(EXTRA_OUTPUT_URI);
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // private method
    //
    ///////////////////////////////////////////////////////////////////////////

    //配置选择器参数
    private Bundle pickBuild()
    {
        Bundle bundle = new Bundle();
        bundle.putInt(Constances.MAX_PICK_COUNT_KEY, mPickCount > 0 ? mPickCount : Constances.MAX_PICK_COUNT);
        bundle.putInt(Constances.PAGER_SIZE_KEY, mPagerSize > 0 ? mPagerSize : Constances.PAGER_SIZE);
        bundle.putInt(Constances.SPAN_COUNT_KEY, mSpanCount > 0 ? mSpanCount : Constances.SPAN_COUNT);
        return bundle;
    }

    //Ucrop默认配置
    private void configDefalut(Activity activity)
    {
        mOptions.useSourceImageAspectRatio();
        mOptions.withMaxResultSize(Constances.MAX_WIDTH,Constances.MAX_HEIGHT);
    }

}
