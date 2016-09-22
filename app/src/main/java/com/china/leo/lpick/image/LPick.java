package com.china.leo.lpick.image;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-22 上午11:16
 */

public final class LPick
{
    //可配置参数
    private int mPickCount = Constances.MAX_PICK_COUNT;
    private int mSpanCount = Constances.SPAN_COUNT;
    private int mPagerSize = Constances.PAGER_SIZE;

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

    }

    /**
     * 设置最多可选数量
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
     * @param pagerSize
     * @return
     */
    public LPick withPagerSize(int pagerSize)
    {
        mPagerSize = pagerSize;
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
     * @param source 源图片
     * @param destination 裁剪图片
     */
    public void crop(Activity activity, Uri source, Uri destination)
    {
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
        bundle.putInt(Constances.MAX_PICK_COUNT_KEY,mPickCount > 0 ? mPickCount : Constances.MAX_PICK_COUNT);
        bundle.putInt(Constances.PAGER_SIZE_KEY,mPagerSize > 0 ? mPagerSize : Constances.PAGER_SIZE);
        bundle.putInt(Constances.SPAN_COUNT_KEY,mSpanCount > 0 ? mSpanCount : Constances.SPAN_COUNT);
        return bundle;
    }


}
