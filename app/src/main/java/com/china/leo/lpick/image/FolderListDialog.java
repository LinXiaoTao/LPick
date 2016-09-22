package com.china.leo.lpick.image;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.china.leo.lpick.R;
import com.china.leo.lpick.image.model.FolderModel;
import com.china.leo.lpick.image.utils.MediaUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-21 上午10:50
 */

public class FolderListDialog extends BottomSheetDialog
{

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private List<FolderModel> mFolderModels = new ArrayList<>();
    private FolderAdapter mAdapter;
    private String mFolderId = "";
    private OnNotifyDataCallback mCallback;
    private BottomSheetBehavior mBottomSheetBehavior;

    public FolderListDialog(@NonNull Context context)
    {
        super(context);
    }

    public FolderListDialog(@NonNull Context context, @StyleRes int theme)
    {
        super(context, theme);
    }

    protected FolderListDialog(@NonNull Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener)
    {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initDialog();
        setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
    }

    @Override
    protected void onStart()
    {
        initView();
        initData();
        super.onStart();
    }

    public void setCallback(OnNotifyDataCallback callback)
    {
        mCallback = callback;
    }

    @Override
    public void hide()
    {
        if (mBottomSheetBehavior != null)
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        else
            super.hide();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // private method
    //
    ///////////////////////////////////////////////////////////////////////////

    private void initDialog()
    {
        View rootView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_folder_list, null);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_folder);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_loading);

        setContentView(rootView);
        try
        {
            //解决BottomSheet状态为hidden，无法显示
            FrameLayout bottomSheet = (FrameLayout) getDelegate().findViewById(android.support.design.R.id.design_bottom_sheet);
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
            {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState)
                {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                    {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset)
                {

                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initData()
    {
        showLoading();
        MediaUtils
                .with(getContext().getApplicationContext())
                .queryImageFolder()
                .subscribe(new Subscriber<List<FolderModel>>()
                {
                    @Override
                    public void onCompleted()
                    {
                        hideLoading();
                        if (mAdapter != null)
                        {
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                    }

                    @Override
                    public void onNext(List<FolderModel> folderModels)
                    {
                        mFolderModels = folderModels;
                    }
                });
    }

    private void initView()
    {
        mRecyclerView.setAdapter(mAdapter = new FolderAdapter());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private void showLoading()
    {
        if (mProgressBar != null)
        {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading()
    {
        if (mProgressBar != null)
        {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * 处理选择
     *
     * @param v
     * @param model
     */
    private void handleSelect(View v, FolderModel model)
    {
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkbox_select);
        if (!mFolderId.equals(model.mFolderId))
        {
            mFolderId = model.mFolderId;
            if (mCallback != null)
            {
                mCallback
                        .onNotifyData(mFolderId,model.mTitle);
            }
        }
        checkBox.setChecked(true);
        hide();
    }

    private class FolderAdapter extends RecyclerView.Adapter<FolderHolder>
    {

        @Override
        public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View rootView = ((LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_folder, parent, false);
            return new FolderHolder(rootView);
        }

        @Override
        public void onBindViewHolder(FolderHolder holder, int position)
        {
            if (mFolderModels != null && mFolderModels.size() > position)
            {
                final FolderModel model = mFolderModels.get(position);
                holder.mSelect.setChecked(model.mFolderId.equals(mFolderId));
                holder.mFolderTitle.setText(model.mTitle);
                holder.mFolderCount.setText(String.format("%d张", model.mImgCount));
                if (!TextUtils.isEmpty(model.mTitleImg))
                {
                    Picasso
                            .with(holder.itemView.getContext())
                            .load(new File(model.mTitleImg))
                            .fit()
                            .centerInside()
                            .into(holder.mImgFolder);
                }

                holder.itemView
                        .setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                handleSelect(v, model);
                            }
                        });

                holder.mSelect
                        .setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                handleSelect(v, model);
                            }
                        });
            }
        }

        @Override
        public int getItemCount()
        {
            return mFolderModels.size();
        }
    }

    private class FolderHolder extends RecyclerView.ViewHolder
    {
        ImageView mImgFolder;
        TextView mFolderTitle;
        CheckBox mSelect;
        TextView mFolderCount;

        public FolderHolder(View itemView)
        {
            super(itemView);

            mImgFolder = (ImageView) itemView.findViewById(R.id.img_folder);
            mFolderTitle = (TextView) itemView.findViewById(R.id.text_folder_name);
            mSelect = (CheckBox) itemView.findViewById(R.id.checkbox_select);
            mFolderCount = (TextView) itemView.findViewById(R.id.text_folder_count);
        }
    }

    public interface OnNotifyDataCallback
    {
        void onNotifyData(String folderId, String title);
    }
}
