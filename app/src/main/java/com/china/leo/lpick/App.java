package com.china.leo.lpick;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-23 下午1:07
 */

public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();


        if (!LeakCanary.isInAnalyzerProcess(this))
            LeakCanary.install(this);
    }
}
