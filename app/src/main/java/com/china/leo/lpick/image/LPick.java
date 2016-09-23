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
        //UCrop默认参数
        configDefalut();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // 选择-配置参数
    //
    ///////////////////////////////////////////////////////////////////////////

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

    ///////////////////////////////////////////////////////////////////////////
    //
    // 裁剪-配置参数
    //
    ///////////////////////////////////////////////////////////////////////////

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
     * 是否隐藏底部控制器
     *
     * @param hide
     * @return
     */
    public LPick setHideBottomControls(boolean hide)
    {
        mOptions.setHideBottomControls(hide);
        return INSTANCE;
    }

    /**
     * 设置标题文字
     *
     * @param title
     * @return
     */
    public LPick setToolbarTitle(String title)
    {
        mOptions.setToolbarTitle(title);
        return INSTANCE;
    }

    /**
     * 设置Logo颜色
     *
     * @param color
     * @return
     */
    public LPick setLogoColor(int color)
    {
        mOptions.setLogoColor(color);
        return INSTANCE;
    }

    /**
     * 设置选中控件颜色
     *
     * @param color
     * @return
     */
    public LPick setActiveWidgetColor(int color)
    {
        mOptions.setActiveWidgetColor(color);
        return INSTANCE;
    }

    /**
     * 设置ToolBar上控件的文本颜色
     *
     * @param color
     * @return
     */
    public LPick setToolbarWidgetColor(int color)
    {
        mOptions.setToolbarWidgetColor(color);
        return INSTANCE;
    }

    /**
     * 设置状态栏颜色
     *
     * @param color
     * @return
     */
    public LPick setStatusBarColor(int color)
    {
        mOptions.setStatusBarColor(color);
        return INSTANCE;
    }

    /**
     * 设置Toolbar的背景颜色
     *
     * @param color
     * @return
     */
    public LPick setToolbarColor(int color)
    {
        mOptions.setToolbarColor(color);
        return INSTANCE;
    }

    /**
     * 是否显示网格线
     *
     * @param showCropGrid
     * @return
     */
    public LPick setShowCropGrid(boolean showCropGrid)
    {
        mOptions.setShowCropGrid(showCropGrid);
        return INSTANCE;
    }

    /**
     * 是否显示矩阵框
     *
     * @param isShowFrme
     * @return
     */
    public LPick setShowCropFrame(boolean isShowFrme)
    {
        mOptions.setShowCropFrame(isShowFrme);
        return INSTANCE;
    }

    /**
     * 设置矩阵框的颜色
     *
     * @param color
     * @return
     */
    public LPick setCropFrameColor(int color)
    {
        mOptions.setCropFrameColor(color);
        return INSTANCE;
    }

    /**
     * 是否显示圆形变暗层
     *
     * @param isCircle
     * @return
     */
    public LPick setCircleDimmedLayer(boolean isCircle)
    {
        mOptions.setCircleDimmedLayer(isCircle);
        return INSTANCE;
    }

    /**
     * 设置读取图片的最大尺寸
     *
     * @param maxBitmapSize
     * @return
     */
    public LPick setMaxBitmapSize(int maxBitmapSize)
    {
        mOptions.setMaxBitmapSize(maxBitmapSize);
        return INSTANCE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////


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
    private void configDefalut()
    {
        mOptions.useSourceImageAspectRatio();
        mOptions.setMaxBitmapSize(Constances.MAX_CROP_SIZE);
        mOptions.withMaxResultSize(Constances.MAX_RESULT_SIZE, Constances.MAX_RESULT_SIZE);
    }

}
