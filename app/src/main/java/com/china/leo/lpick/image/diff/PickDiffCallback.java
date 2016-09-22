package com.china.leo.lpick.image.diff;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.china.leo.lpick.image.model.PickModel;

import java.util.List;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-19 下午5:20
 */

public class PickDiffCallback extends DiffUtil.Callback
{

    private List<PickModel> mOldData;
    private List<PickModel> mNewData;

    public PickDiffCallback(List<PickModel> oldData, List<PickModel> newData)
    {
        mOldData = oldData;
        mNewData = newData;
    }

    @Override
    public int getOldListSize()
    {
        return mOldData.size();
    }

    @Override
    public int getNewListSize()
    {
        return mNewData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
    {
        return mOldData.get(oldItemPosition).mImgPath.equals(mNewData.get(newItemPosition).mImgPath);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
    {
        return mOldData.get(oldItemPosition).mIsPick == mNewData.get(newItemPosition).mIsPick;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition)
    {
//        Logger.d(String.format("oldIndex = %d,value = %s\n newIndex = %d,value = %s", oldItemPosition
//                , mOldData.get(oldItemPosition).mIsPick + "", newItemPosition,
//                mNewData.get(newItemPosition).mIsPick + ""));
        Bundle bundle = new Bundle();
        bundle.putBoolean(String.valueOf(newItemPosition), mNewData.get(newItemPosition).mIsPick);
        return bundle;
    }
}
