package com.china.leo.lpick.image.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-21 上午11:09
 */

public class PickModel implements Parcelable
{
    public String mImgPath;
    public boolean mIsPick;
    //缩略图
    public String mThumbnails;

    @Override
    public boolean equals(Object o)
    {
        //比较图片路径
        if (o instanceof PickModel && ((PickModel) o).mImgPath.equals(mImgPath))
            return true;
        return false;
    }

    @Override
    public String toString()
    {
        return String.valueOf(mIsPick);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.mImgPath);
        dest.writeByte(this.mIsPick ? (byte) 1 : (byte) 0);
        dest.writeString(this.mThumbnails);
    }

    public PickModel()
    {
    }

    protected PickModel(Parcel in)
    {
        this.mImgPath = in.readString();
        this.mIsPick = in.readByte() != 0;
        this.mThumbnails = in.readString();
    }

    public static final Creator<PickModel> CREATOR = new Creator<PickModel>()
    {
        @Override
        public PickModel createFromParcel(Parcel source)
        {
            return new PickModel(source);
        }

        @Override
        public PickModel[] newArray(int size)
        {
            return new PickModel[size];
        }
    };
}
