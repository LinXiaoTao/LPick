package com.china.leo.lpick.image;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-19 下午3:23
 */

public class BaseActivity extends AppCompatActivity
{
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onDestroy()
    {
        if (mBroadcastReceiver != null)
        {
            unRegister();
        }
        super.onDestroy();
    }

    protected void register(String action)
    {
        initReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, new IntentFilter(action));
    }

    protected void unRegister()
    {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    protected void sendLocalBroadcast(Intent intent)
    {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    protected void handleBroadcast(Intent intent)
    {
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // private method
    //
    ///////////////////////////////////////////////////////////////////////////

    private void initReceiver()
    {
        if (mBroadcastReceiver != null)
        {
            unRegister();
        }

        mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                handleBroadcast(intent);
                unRegister();
            }
        };
    }

}
