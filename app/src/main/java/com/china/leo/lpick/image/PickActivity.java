package com.china.leo.lpick.image;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.leo.lpick.R;
import com.china.leo.lpick.image.diff.PickDiffCallback;
import com.china.leo.lpick.image.model.PickModel;
import com.china.leo.lpick.image.utils.CameraUtils;
import com.china.leo.lpick.image.utils.MediaScanner;
import com.china.leo.lpick.image.utils.MediaUtils;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.china.leo.lpick.image.Constances.DETAIL_ACTION;
import static com.china.leo.lpick.image.Constances.MAX_PICK_COUNT;
import static com.china.leo.lpick.image.Constances.MAX_PICK_COUNT_KEY;
import static com.china.leo.lpick.image.Constances.PAGER_SIZE;
import static com.china.leo.lpick.image.Constances.PAGER_SIZE_KEY;
import static com.china.leo.lpick.image.Constances.PICK_COUNT_TEXT;
import static com.china.leo.lpick.image.Constances.PICK_SOUCRE_KEY;
import static com.china.leo.lpick.image.Constances.SOURCE_KEY;
import static com.china.leo.lpick.image.Constances.SPAN_COUNT;
import static com.china.leo.lpick.image.Constances.SPAN_COUNT_KEY;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-19 上午10:21
 */

@RuntimePermissions
public class PickActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    @BindView(R.id.txtTitle)
    TextView mTxtTitle;
    @BindView(R.id.btnOk)
    TextView mBtnOk;
    @BindView(R.id.pickRecyclerView)
    SuperRecyclerView mRecyclerView;
    @BindView(R.id.btnOpenFolder)
    Button mBtnOpenFolder;

    private PickImageAdapter mPickImageAdapter;
    private ArrayList<PickModel> mImageSource = new ArrayList<>();
    private int mPager = 1;
    private Loader mLoader;
    private CursorLoader mCursorLoader;
    private boolean mIsShowDialog = false;
    private File mSaveFile = null;
    private MediaScanner mMediaScanner;

    /**
     * 已选择的图片
     */
    private ArrayList<PickModel> mPickModelList = new ArrayList<>();
    private FolderListDialog mFolderListDialog;
    private final static int LOADER_IMAGE = 0;
    private final static int REQUEST_CODE = 0;

    private int mSpanCount = SPAN_COUNT;
    private int mPagerSize = PAGER_SIZE;
    private int mMaxPickCount = MAX_PICK_COUNT;


    //加载更多
    private OnMoreListener mOnMoreListener = new OnMoreListener()
    {
        @Override
        public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition)
        {
            if (mCursorLoader != null && !mLoader.isStarted())
            {
                int offset = (mPager - 1) * mPagerSize;
                mCursorLoader.setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + mPagerSize + " OFFSET " + offset);
                mLoader.reset();
                mLoader.startLoading();
            } else
            {
                if (mRecyclerView.isLoadingMore())
                {
                    mRecyclerView.setLoadingMore(false);
                }
            }
        }
    };

    //选择图片文件夹,通知更新
    private FolderListDialog.OnNotifyDataCallback mNotifyDataCallback = new FolderListDialog.OnNotifyDataCallback()
    {
        @Override
        public void onNotifyData(String folderId, String title)
        {
            mBtnOpenFolder.setText(title);
            mRecyclerView.setOnMoreListener(mOnMoreListener);
            mImageSource.clear();
            addTakeImage();
            mPager = 1;
            int offset = (mPager - 1) * mPagerSize;
            if (!TextUtils.isEmpty(folderId))
            {
                mCursorLoader.setSelection(MediaStore.Images.Media.BUCKET_ID + "=?");
                mCursorLoader.setSelectionArgs(new String[]{folderId});
            } else
            {
                mCursorLoader.setSelection("");
                mCursorLoader.setSelectionArgs(null);
            }
            mCursorLoader.setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + mPagerSize + " OFFSET " + offset);
            mLoader.reset();
            mLoader.startLoading();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_img);
        ButterKnife.bind(this);

        //动态检查权限
        PickActivityPermissionsDispatcher.initWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PickActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnClick({R.id.btnClose, R.id.btnOk, R.id.btnOpenFolder})
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btnClose:
                close();
                break;
            case R.id.btnOk:
                Intent data = new Intent();
                data.putParcelableArrayListExtra(PICK_SOUCRE_KEY, mPickModelList);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
            case R.id.btnOpenFolder:
                if (mFolderListDialog != null)
                {
                    if (mIsShowDialog)
                        closeFolderDialog();
                    else
                        openFolderDialog();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            if (mSaveFile != null && mSaveFile.exists())
            {
                mMediaScanner.scanFile(mSaveFile.getAbsolutePath(), "image/jpeg", new MediaScanner.ScanCallback()
                {
                    @Override
                    public void onScanCompleted(String[] images)
                    {
                        PickModel data = new PickModel();
                        data.mIsPick = false;
                        data.mImgPath = mSaveFile.getAbsolutePath();
                        if (!mImageSource.isEmpty())
                            mImageSource.add(1, data);
                        updateNotifyData(mPickImageAdapter.getSourceData(), mImageSource);
                    }
                });
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if (id == LOADER_IMAGE)
        {
            return mCursorLoader = MediaUtils.with(getApplicationContext())
                    .createQueryImageLoad(mPager, PAGER_SIZE, "");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (data != null && data.getCount() > 0)
        {
            List<PickModel> oldData = mPickImageAdapter.getSourceData();
            data.moveToFirst();
            do
            {
                mImageSource.add(handleModel(data));
            } while (data.moveToNext());
            mPager++;
            updateNotifyData(oldData, mImageSource);
        } else
        {
            mRecyclerView.setOnMoreListener(null);
//            Snackbar.make(mRecyclerView, "没有更多了", Snackbar.LENGTH_SHORT)
//                    .show();
        }

        mLoader.stopLoading();

        if (mRecyclerView.isLoadingMore())
            mRecyclerView.setLoadingMore(false);
        mRecyclerView.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mRecyclerView.hideMoreProgress();
            }
        }, 500);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
//        Logger.d("onLoaderReset");
    }

    @Override
    protected void onDestroy()
    {
        if (mFolderListDialog != null)
        {
            if (mFolderListDialog.isShowing())
                mFolderListDialog.dismiss();
            mFolderListDialog = null;
        }
        super.onDestroy();
    }

    @Override
    protected void handleBroadcast(Intent intent)
    {
        super.handleBroadcast(intent);
        if (intent.getAction().equals(DETAIL_ACTION))
        {
            List<PickModel> oldData = mPickImageAdapter.getSourceData();
            mImageSource = intent.getParcelableArrayListExtra(SOURCE_KEY);
            addTakeImage();
            mPickModelList = intent.getParcelableArrayListExtra(PICK_SOUCRE_KEY);
            refreshButton();
            updateNotifyData(oldData, mImageSource);
            mLoader.reset();
//            if (intent.hasExtra(SELECT_INDEX))
//                mRecyclerView.getRecyclerView()
//                        .scrollToPosition(intent.getIntExtra(SELECT_INDEX, 0));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // 动态权限申请处理
    //
    ///////////////////////////////////////////////////////////////////////////

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void init()
    {
        getBuildConfig(getIntent().getExtras());

        initLoader();
        initRecyclerView();
        initFolderDialog();
        mMediaScanner = new MediaScanner(this);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void handleDeniedMessage()
    {
        Snackbar snackbar = Snackbar.make(mRecyclerView, "获取权限失败", Snackbar.LENGTH_SHORT);
        snackbar.setCallback(new Snackbar.Callback()
        {
            @Override
            public void onDismissed(Snackbar snackbar, int event)
            {
                super.onDismissed(snackbar, event);
                finish();
            }
        });
        snackbar.show();
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void handleRationale(final PermissionRequest request)
    {
        new AlertDialog.Builder(this)
                .setMessage("应用需要您的授权才能读取您的图库")
                .setPositiveButton("授权", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        request.proceed();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void handleNeverAskAgain()
    {
        Snackbar snackbar = Snackbar
                .make(mRecyclerView, "请在\"设置\"重置应用权限", Snackbar.LENGTH_SHORT);
        snackbar.setCallback(new Snackbar.Callback()
        {
            @Override
            public void onDismissed(Snackbar snackbar, int event)
            {
                super.onDismissed(snackbar, event);
                finish();
            }
        });
        snackbar.show();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // private method
    //
    ///////////////////////////////////////////////////////////////////////////

    private void getBuildConfig(Bundle bundle)
    {
        mMaxPickCount = bundle.getInt(MAX_PICK_COUNT_KEY, MAX_PICK_COUNT);
        mPagerSize = bundle.getInt(PAGER_SIZE_KEY, PAGER_SIZE);
        mSpanCount = bundle.getInt(SPAN_COUNT_KEY, SPAN_COUNT);
    }

    private void close()
    {
        finish();
    }

    private void initRecyclerView()
    {
        addTakeImage();
        mRecyclerView.setAdapter(mPickImageAdapter = new PickImageAdapter(mImageSource));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, mSpanCount));
        mRecyclerView.setOnMoreListener(mOnMoreListener);
    }

    private void initLoader()
    {
        mLoader = getSupportLoaderManager()
                .initLoader(LOADER_IMAGE, null, this);
    }

    private void initFolderDialog()
    {
        if (mFolderListDialog == null)
        {
            mFolderListDialog = new FolderListDialog(this);
        }

        mFolderListDialog.setCallback(mNotifyDataCallback);
    }

    /**
     * 使用DiffUtil比较，部分刷新数据。
     * 注意:在DiffUtil.calculateDiff之前,新数据源不能影响到adapter已有的数据源
     *
     * @param oldData 旧数据源
     * @param newData 新数据源
     */
    private void updateNotifyData(final List<PickModel> oldData, final List<PickModel> newData)
    {
        Observable
                .create(new Observable.OnSubscribe<DiffUtil.DiffResult>()
                {

                    @Override
                    public void call(Subscriber<? super DiffUtil.DiffResult> subscriber)
                    {
                        subscriber.onNext(DiffUtil.calculateDiff(new PickDiffCallback(oldData, newData)));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DiffUtil.DiffResult>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        mPickImageAdapter.setSourceData(newData);
                        mPickImageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNext(DiffUtil.DiffResult diffResult)
                    {
                        mPickImageAdapter.setSourceData(newData);
                        diffResult.dispatchUpdatesTo(mPickImageAdapter);
                    }
                });
    }

    private void handlePickState(PickHolder holder, PickModel model)
    {
        if (mPickModelList.size() >= mMaxPickCount && !model.mIsPick)
        {
            holder.mCheckBox.setChecked(false);
            Snackbar.make(mRecyclerView, "已经达到最大数量了", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        model.mIsPick = !model.mIsPick;
        holder.mCheckBox.setChecked(model.mIsPick);


        if (mPickModelList.contains(model) && !model.mIsPick)
        {
            mPickModelList.remove(model);
        }
        if (!mPickModelList.contains(model) && model.mIsPick)
        {
            mPickModelList.add(model);
        }

        refreshButton();
    }

    private void refreshButton()
    {
        if (mPickModelList.size() > 0)
            mBtnOk.setText(String.format(PICK_COUNT_TEXT, mPickModelList.size(), mMaxPickCount));
        else
            mBtnOk.setText("完成");
    }

    private void addTakeImage()
    {
        PickModel takeModel = new PickModel();
        takeModel.mImgPath = "";
        takeModel.mIsPick = false;
        mImageSource.add(0, takeModel);
    }

    private void openFolderDialog()
    {
        mFolderListDialog.show();
    }

    private void closeFolderDialog()
    {
        mFolderListDialog.hide();
    }

    private class PickImageAdapter extends RecyclerView.Adapter<PickHolder>
    {
        private int mImageWidth = 0;
        private List<PickModel> mSourceData;


        public PickImageAdapter(List<PickModel> source)
        {
            mImageWidth = getResources().getDisplayMetrics().widthPixels / SPAN_COUNT;
            mSourceData = new ArrayList<>(source);
        }

        public List<PickModel> getSourceData()
        {
            return mSourceData;
        }

        public void setSourceData(List<PickModel> sourceData)
        {
            mSourceData.clear();
            mSourceData.addAll(sourceData);
        }

        @Override
        public PickHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View rootView = ((LayoutInflater) parent.getContext().getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_pick_img, parent, false);
            PickHolder pickHolder = new PickHolder(rootView);
            if (mImageWidth != 0)
            {
                ViewGroup.LayoutParams layoutParams = pickHolder.mImageView.getLayoutParams();
                layoutParams.width = mImageWidth;
                layoutParams.height = mImageWidth;
            }
            return pickHolder;
        }

        @Override
        public void onBindViewHolder(final PickHolder holder, final int position, List<Object> payloads)
        {
            if (payloads == null || payloads.isEmpty())
            {
                super.onBindViewHolder(holder, position, payloads);
            } else
            {
                try
                {
                    holder.mCheckBox
                            .setChecked(mPickModelList.contains(mSourceData.get(position)));
                    mSourceData.get(position)
                            .mIsPick = holder.mCheckBox.isChecked();

                    holder.mCheckBox
                            .setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    handlePickState(holder, mSourceData.get(position));
                                }
                            });
                } catch (Exception e)
                {

                    e.printStackTrace();
                }
//                Logger.d("holder = %s,position = %d,payloads = %s", holder.toString(), position, payloads.toString());
            }
        }

        @Override
        public void onBindViewHolder(final PickHolder holder, final int position)
        {
            if (mSourceData != null && mSourceData.size() > position)
            {
                final PickModel pickModel = mSourceData.get(position);
                if (!TextUtils.isEmpty(pickModel.mImgPath))
                {
                    Picasso.with(holder.mImageView.getContext())
                            .load(new File(pickModel.mImgPath))
                            .resize(mImageWidth, mImageWidth)
                            .centerCrop()
                            .into(holder.mImageView);
                    holder.mCheckBox
                            .setVisibility(View.VISIBLE);

                } else
                {
                    Picasso.with(holder.mImageView.getContext())
                            .load(R.mipmap.ic_camera)
                            .into(holder.mImageView);

                    holder.mCheckBox
                            .setVisibility(View.GONE);
                }

                holder.mCheckBox
                        .setChecked(mPickModelList.contains(mSourceData.get(position)));
                mSourceData.get(position)
                        .mIsPick = holder.mCheckBox.isChecked();

                holder.mCheckBox
                        .setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                handlePickState(holder, pickModel);
                            }
                        });

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (position == 0)
                        {
                            //拍照
                            mSaveFile = CameraUtils
                                    .openCamera(PickActivity.this, REQUEST_CODE);
                        } else
                        {
                            //大图
                            register(DETAIL_ACTION);
                            ArrayList<PickModel> data = new ArrayList<>(mImageSource);
                            data.remove(0);
                            ImageDetailActivity
                                    .openImgDetail(PickActivity.this, data, mPickModelList,
                                            position - 1, mMaxPickCount, holder.mImageView);
                        }
                    }
                });

            }
        }

        @Override
        public int getItemCount()
        {
            return mSourceData.size();
        }

    }

    private PickModel handleModel(Cursor cursor)
    {
        PickModel model = new PickModel();
        model.mImgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        model.mIsPick = false;
        return model;
    }

    private class PickHolder extends RecyclerView.ViewHolder
    {
        ImageView mImageView;
        CheckBox mCheckBox;

        public PickHolder(View itemView)
        {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.item_img);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.item_checkbox);
        }
    }
}
