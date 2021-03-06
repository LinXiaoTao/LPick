package com.china.leo.lpick.image.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.china.leo.lpick.image.model.FolderModel;
import com.china.leo.lpick.image.model.PickModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 媒体工具类
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-21 上午11:08
 */

public final class MediaUtils
{
    private static Context mContext;

    public static MediaUtils with(Context context)
    {
        mContext = context;
        return new MediaUtils();
    }

    private MediaUtils()
    {
    }


    public Observable<List<PickModel>> queryImageModel(final int pager,final int pagerSize, final String bucketId)
    {
        return Observable
                .create(new Observable.OnSubscribe<List<PickModel>>()
                {
                    @Override
                    public void call(Subscriber<? super List<PickModel>> subscriber)
                    {
                        int offset = (pager - 1) * pagerSize;
                        List<String> projection = new ArrayList<>();
                        projection.add(MediaStore.Images.Media._ID);
                        projection.add(MediaStore.Images.Media.TITLE);
                        projection.add(MediaStore.Images.Media.DATA);
                        projection.add(MediaStore.Images.Media.BUCKET_ID);
                        projection.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                        projection.add(MediaStore.Images.Media.MIME_TYPE);
                        projection.add(MediaStore.Images.Media.DATE_ADDED);
                        projection.add(MediaStore.Images.Media.DATE_MODIFIED);
                        projection.add(MediaStore.Images.Media.LATITUDE);
                        projection.add(MediaStore.Images.Media.LONGITUDE);
                        projection.add(MediaStore.Images.Media.ORIENTATION);
                        projection.add(MediaStore.Images.Media.SIZE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        {
                            projection.add(MediaStore.Images.Media.WIDTH);
                            projection.add(MediaStore.Images.Media.HEIGHT);
                        }

                        String selection = null;
                        String[] selectionArgs = null;

                        if (!TextUtils.isEmpty(bucketId))
                        {
                            selection = MediaStore.Images.Media.BUCKET_ID + "=?";
                            selectionArgs = new String[]{bucketId};
                        }

                        Cursor cursor = mContext.getContentResolver()
                                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                        , projection.toArray(new String[projection.size()]),
                                        selection, selectionArgs,
                                        MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + pagerSize + " OFFSET " + offset);
                        List<PickModel> data = new ArrayList<PickModel>();

                        if (cursor != null && cursor.getCount() > 0)
                        {
                            cursor.moveToFirst();
                            do
                            {
                                data.add(handleModel(cursor));

                            }while (cursor.moveToNext());
                        }

                        subscriber.onNext(data);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<List<FolderModel>> queryImageFolder()
    {
        final String[] projection = new String[]{
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.ORIENTATION};
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return Observable
                .create(new Observable.OnSubscribe<List<FolderModel>>()
                {
                    @Override
                    public void call(Subscriber<? super List<FolderModel>> subscriber)
                    {
                        Cursor cursor = mContext.getContentResolver()
                                .query(uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
                        List<FolderModel> data = new ArrayList<FolderModel>();
                        if (cursor != null && cursor.getCount() > 0)
                        {
                            cursor.moveToFirst();
                            do
                            {
                                FolderModel model = handleFolder(cursor);

                                //获取数量
                                Cursor c = mContext.getContentResolver()
                                        .query(uri, projection, MediaStore.Images.Media.BUCKET_ID + "=?", new String[]{model.mFolderId}, null);
                                if (c != null && c.getCount() > 0)
                                {
                                    model.mImgCount = c.getCount();
                                }
                                if (c != null && !c.isClosed())
                                {
                                    c.close();
                                    c = null;
                                }

                                if (!data.contains(model))
                                    data.add(model);


                            } while (cursor.moveToNext());

                            if (cursor != null && !cursor.isClosed())
                            {
                                cursor.close();
                                cursor = null;
                            }

                            subscriber.onNext(data);
                            subscriber.onCompleted();

                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<List<FolderModel>>()
                {
                    @Override
                    public void call(List<FolderModel> folderModels)
                    {
                        if (folderModels == null)
                        {
                            folderModels = new ArrayList<FolderModel>();
                        }

                        FolderModel all = new FolderModel();
                        all.mFolderId = "";
                        all.mTitle = "所有图片";

                        int totalCount = 0;

                        if (!folderModels.isEmpty())
                        {
                            all.mTitleImg = folderModels.get(0)
                                    .mTitleImg;
                            for (FolderModel model : folderModels)
                            {
                                totalCount += model.mImgCount;
                            }

                            all.mImgCount = totalCount;
                        }

                        folderModels.add(0, all);
                    }
                });

    }

    /**
     * 根据图片orgid获取缩略图
     *
     * @param orgid
     * @param type
     * @return
     */
    public String getThumbnails(long orgid, int type)
    {
        String path = "";
        Cursor cursor = null;
        try
        {
            cursor = MediaStore.Images.Thumbnails
                    .queryMiniThumbnail(mContext.getContentResolver(), orgid, type, null);
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            if (cursor != null)
            {
                cursor.close();
                cursor = null;
            }
        }

        return path;
    }

    /**
     * 根据图片path获取缩略图
     *
     * @param orgid
     * @param type
     * @return
     */
    public Bitmap getThumbnail(long orgid, int type)
    {
        Bitmap result = null;
        Cursor cursor = null;
        try
        {
            result = MediaStore.Images.Thumbnails
                    .getThumbnail(mContext.getContentResolver(), orgid, type, null);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    // private method
    //
    ///////////////////////////////////////////////////////////////////////////

    private FolderModel handleFolder(Cursor cursor)
    {
        FolderModel model = new FolderModel();
        model.mFolderId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
        model.mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
        model.mTitleImg = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        return model;
    }

    private PickModel handleModel(Cursor cursor)
    {
        PickModel model = new PickModel();
        model.mImgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        model.mOrigId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        model.mIsPick = false;
        return model;
    }
}
