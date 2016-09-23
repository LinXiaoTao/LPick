package com.china.leo.lpick.image;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.util.BitmapLoadUtils;

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
import rx.functions.Action1;
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
public class PickActivity extends BaseActivity
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
    private boolean mIsShowDialog = false;
    private File mSaveFile = null;
    private MediaScanner mMediaScanner;
    private String mFolderId = "";

    //已选择的图片
    private ArrayList<PickModel> mPickModelList = new ArrayList<>();
    private FolderListDialog mFolderListDialog;
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
            loadMoreData(mFolderId);
        }
    };

    //选择图片文件夹,通知更新
    private FolderListDialog.OnNotifyDataCallback mNotifyDataCallback = new FolderListDialog.OnNotifyDataCallback()
    {
        @Override
        public void onNotifyData(String folderId, String title)
        {
            mBtnOpenFolder.setText(title);
            mImageSource.clear();
            addTakeImage();
            mPager = 1;
            mFolderId = folderId;

            //开启加载更多
            enableLoadingMore(true);
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
    protected void onResume()
    {
        initFolderDialog();
        super.onResume();
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
        initRecyclerView();
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
        mRecyclerView.setNumberBeforeMoreIsCalled(mSpanCount);
    }

    private void initFolderDialog()
    {
        if (mFolderListDialog == null)
        {
            mFolderListDialog = new FolderListDialog(this);
            mFolderListDialog.setCallback(mNotifyDataCallback);
        }
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

    //添加拍照
    private void addTakeImage()
    {
        PickModel takeModel = new PickModel();
        takeModel.mImgPath = "";
        takeModel.mIsPick = false;
        mImageSource.add(0, takeModel);
    }

    /**
     * 加载更多数据
     *
     * @param bucketId 文件夹id
     */
    private void loadMoreData(String bucketId)
    {
        MediaUtils
                .with(this)
                .queryImageModel(mPager, mPagerSize, bucketId)
                .doOnNext(new Action1<List<PickModel>>()
                {
                    @Override
                    public void call(List<PickModel> pickModels)
                    {
                        if (pickModels == null || pickModels.isEmpty())
                        {
                            //关闭加载更多
                            enableLoadingMore(false);
                        }
                    }
                })
                .subscribe(new Subscriber<List<PickModel>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        mPager++;
                        List<PickModel> oldData = mPickImageAdapter.getSourceData();
                        updateNotifyData(oldData, mImageSource);
                        closeLoadingMore();
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        closeLoadingMore();
                    }

                    @Override
                    public void onNext(List<PickModel> pickModels)
                    {
                        mImageSource.addAll(pickModels);
                    }
                });
    }

    //关闭加载更多
    private void closeLoadingMore()
    {
        mRecyclerView.setLoadingMore(false);
        mRecyclerView.hideMoreProgress();
    }

    /**
     * 开启/关闭 加载更多
     *
     * @param isEnable
     */
    private void enableLoadingMore(boolean isEnable)
    {
        if (mRecyclerView != null)
        {
            if (!isEnable)
                mRecyclerView.removeMoreListener();
            else
                mRecyclerView.setOnMoreListener(mOnMoreListener);
        }
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
                    holder.mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Observable
                                .create(new Observable.OnSubscribe<Point>()
                                {
                                    @Override
                                    public void call(Subscriber<? super Point> subscriber)
                                    {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inJustDecodeBounds = true;
                                        BitmapFactory.decodeFile(pickModel.mImgPath, options);
//                                        Logger.d("原始图片大小:(%d,%d),%fMB", options.outWidth, options.outHeight
//                                                , (float) options.outHeight * options.outWidth * 4 / 1024 / 1024);
                                        int inSampleSize = BitmapLoadUtils.calculateInSampleSize(options
                                                , Constances.MAX_THUMB_SIZE, Constances.MAX_THUMB_SIZE);

//                                        Logger.d("缩放比例为%d", inSampleSize);
                                        subscriber.onNext(new Point(options.outWidth / inSampleSize
                                                , options.outHeight / inSampleSize));
                                        subscriber.onCompleted();
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Point>()
                                {
                                    @Override
                                    public void call(Point point)
                                    {
                                        Picasso.with(holder.mImageView.getContext())
                                                .load(new File(pickModel.mImgPath))
                                                .resize(point.x, point.y)
                                                .centerCrop()
                                                .into(holder.mImageView, new Callback()
                                                {
                                                    @Override
                                                    public void onSuccess()
                                                    {
//                                                        showBitmapInfo(holder.mImageView);
                                                    }

                                                    @Override
                                                    public void onError()
                                                    {

                                                    }
                                                });
                                    }
                                });

                    holder.mCheckBox
                            .setVisibility(View.VISIBLE);

                } else
                {
                    holder.mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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

    private void showBitmapInfo(ImageView imageView)
    {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Logger.d("图片大小:%f,宽:%d,高:%d", (float) bitmap.getAllocationByteCount() / 1024 / 1024
                , bitmap.getWidth(), bitmap.getHeight());
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
