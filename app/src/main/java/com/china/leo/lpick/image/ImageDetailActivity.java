package com.china.leo.lpick.image;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.leo.lpick.R;
import com.china.leo.lpick.image.model.PickModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoView;

import static com.china.leo.lpick.image.Constances.DETAIL_ACTION;
import static com.china.leo.lpick.image.Constances.MAX_PICK_COUNT;
import static com.china.leo.lpick.image.Constances.MAX_PICK_COUNT_KEY;
import static com.china.leo.lpick.image.Constances.PICK_COUNT_TEXT;
import static com.china.leo.lpick.image.Constances.PICK_SOUCRE_KEY;
import static com.china.leo.lpick.image.Constances.SELECT_INDEX;
import static com.china.leo.lpick.image.Constances.SOURCE_KEY;

/**
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-19 下午2:16
 */

public class ImageDetailActivity extends BaseActivity
{

    @BindView(R.id.btnClose)
    ImageView mBtnClose;
    @BindView(R.id.txtTitle)
    TextView mTxtTitle;
    @BindView(R.id.btnOk)
    TextView mBtnOk;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private DetailAdapter mAdapter;
    private ArrayList<PickModel> mImageSource;
    private ArrayList<PickModel> mPickImage;
    private int mCurrentIndex = 0;
    private boolean mIsChance = false;
    private int mMaxPickCount = MAX_PICK_COUNT;

    /**
     * 打开详情
     */
    public static void openImgDetail(Activity activity, ArrayList<PickModel> sources,
                                     ArrayList<PickModel> pick,
                                     int index,
                                     int maxPickCount,
                                     View shareView)
    {
        Intent intent = new Intent();
        intent.setClass(activity, ImageDetailActivity.class);
        intent.putParcelableArrayListExtra(SOURCE_KEY, sources);
        intent.putParcelableArrayListExtra(PICK_SOUCRE_KEY, pick);
        intent.putExtra(SELECT_INDEX, index);
        intent.putExtra(MAX_PICK_COUNT_KEY, maxPickCount);
        ActivityOptions options = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            options = ActivityOptions.makeSceneTransitionAnimation(activity,
                    shareView,
                    activity.getString(R.string.shared_element_name));
        }
        if (options != null)
            activity.startActivity(intent, options.toBundle());
        else
            activity.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_detail);
        ButterKnife.bind(this);

        init();
    }

    @OnClick({R.id.btnClose, R.id.btnOk})
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btnClose:
                finish();
                break;
            case R.id.btnOk:
                if (!mIsChance)
                    mIsChance = true;
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        if (mIsChance)
        {
            Intent intent = new Intent(DETAIL_ACTION);
            intent.putParcelableArrayListExtra(PICK_SOUCRE_KEY, mPickImage);
            intent.putParcelableArrayListExtra(SOURCE_KEY, mImageSource);
            intent.putExtra(SELECT_INDEX, mCurrentIndex);
            sendLocalBroadcast(intent);
        }
        super.onDestroy();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // private method
    //
    ///////////////////////////////////////////////////////////////////////////

    private void init()
    {
        getExtraData();
        initRecyclerView();
        refreshButton();
    }

    private void getExtraData()
    {
        mImageSource = getIntent().getParcelableArrayListExtra(SOURCE_KEY);
        mPickImage = getIntent().getParcelableArrayListExtra(PICK_SOUCRE_KEY);
        mCurrentIndex = getIntent().getIntExtra(SELECT_INDEX, 0);
        mMaxPickCount = getIntent().getIntExtra(MAX_PICK_COUNT_KEY, MAX_PICK_COUNT);

    }

    private void initRecyclerView()
    {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        final LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter = new DetailAdapter());
        mRecyclerView.scrollToPosition(mCurrentIndex);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    View view = null;
                    //滚动停止
                    if ((view = snapHelper.findSnapView(mRecyclerView
                            .getLayoutManager())).getTag(R.string.tag_data) != null)
                    {
                        mIsChance = true;
                        mCurrentIndex = (int) view.getTag(R.string.tag_data);
                    }
                }
            }
        });
    }


    private class DetailAdapter extends RecyclerView.Adapter<DetailHolder>
    {


        public DetailAdapter()
        {
        }

        @Override
        public DetailHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View rootView = ((LayoutInflater) parent.getContext().getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_image, parent, false);
            DetailHolder holder = new DetailHolder(rootView);
            return holder;
        }

        @Override
        public void onBindViewHolder(final DetailHolder holder, int position)
        {
            if (mImageSource != null && mImageSource.size() > position)
            {
                final PickModel model = mImageSource.get(position);
                if (!TextUtils.isEmpty(model.mImgPath))
                {
                    Picasso.with(holder.mPhotoView.getContext())
                            .load(new File(model.mImgPath))
                            .fit()
                            .centerInside()
                            .into(holder.mPhotoView);
                }
                holder.mCheckBox.setChecked(model.mIsPick);
                holder.mCheckBox.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        handlePickState(holder, model);
                    }
                });

                holder.itemView
                        .setTag(R.string.tag_data, position);
            }
        }

        @Override
        public int getItemCount()
        {
            return mImageSource.size();
        }
    }

    private void handlePickState(DetailHolder holder, PickModel model)
    {
        if (mPickImage.size() >= mMaxPickCount && !model.mIsPick)
        {
            holder.mCheckBox.setChecked(false);
            Snackbar.make(mRecyclerView, "已经达到最大数量了", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        model.mIsPick = !model.mIsPick;
        holder.mCheckBox.setChecked(model.mIsPick);


        if (mPickImage.contains(model) && !model.mIsPick)
        {
            mPickImage.remove(model);
        }
        if (!mPickImage.contains(model) && model.mIsPick)
        {
            mPickImage.add(model);
        }

        refreshButton();
    }

    private void refreshButton()
    {
        if (mPickImage.size() > 0)
            mBtnOk.setText(String.format(PICK_COUNT_TEXT, mPickImage.size(), mMaxPickCount));
        else
            mBtnOk.setText("完成");
    }

    private class DetailHolder extends RecyclerView.ViewHolder
    {
        PhotoView mPhotoView;
        CheckBox mCheckBox;

        public DetailHolder(View itemView)
        {
            super(itemView);
            mPhotoView = (PhotoView) itemView.findViewById(R.id.item_img);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.btnPick);
        }

    }
}
