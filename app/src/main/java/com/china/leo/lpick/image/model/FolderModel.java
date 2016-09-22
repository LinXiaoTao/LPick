package com.china.leo.lpick.image.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-21 上午11:13
 */

public class FolderModel implements Parcelable
{
    public int mImgCount;
    public String mTitle;
    public String mTitleImg;
    public String mFolderId;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.mImgCount);
        dest.writeString(this.mTitle);
        dest.writeString(this.mTitleImg);
        dest.writeString(this.mFolderId);
    }

    public FolderModel()
    {
    }

    protected FolderModel(Parcel in)
    {
        this.mImgCount = in.readInt();
        this.mTitle = in.readString();
        this.mTitleImg = in.readString();
        this.mFolderId = in.readString();
    }

    public static final Creator<FolderModel> CREATOR = new Creator<FolderModel>()
    {
        @Override
        public FolderModel createFromParcel(Parcel source)
        {
            return new FolderModel(source);
        }

        @Override
        public FolderModel[] newArray(int size)
        {
            return new FolderModel[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof FolderModel && ((FolderModel)o).mFolderId.equals(mFolderId))
        {
            return true;
        }

        return false;
    }
}
