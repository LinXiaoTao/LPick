package com.china.leo.lpick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.china.leo.lpick.image.Constances;
import com.china.leo.lpick.image.LPick;
import com.china.leo.lpick.image.model.PickModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 图片选择器使用
 * leo linxiaotao1993@vip.qq.com
 * Created on 16-9-22 上午10:27
 */

public class PickImgSimpleActivity extends AppCompatActivity
{
    @BindView(R.id.recyclerView_list)
    RecyclerView mRecyclerViewList;

    private static final int SPAN_COUNT = 3;
    private static final int REQUEST_CODE = 0;

    private List<PickModel> mPickModelList = new ArrayList<>();
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_pickimg_simple);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        init();
    }

    @OnClick(R.id.btnPickImg)
    public void onClick()
    {
        LPick.getInstance()
                .withPickCount(5)
                .withSpanCount(4)
                .pick(this,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            mPickModelList = data.getParcelableArrayListExtra(Constances.PICK_SOUCRE_KEY);
            mAdapter.notifyDataSetChanged();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // private method
    //
    ///////////////////////////////////////////////////////////////////////////

    private void init()
    {
        initRecyclerView();
    }

    private void initRecyclerView()
    {
        mRecyclerViewList.setLayoutManager(new GridLayoutManager(this,SPAN_COUNT));
        mRecyclerViewList.setAdapter(mAdapter = new ImageAdapter());
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder>
    {

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View rootView = ((LayoutInflater)parent.getContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_pick_img,parent,false);
            ImageHolder holder = new ImageHolder(rootView);
            ViewGroup.LayoutParams parsms = holder.mImageView.getLayoutParams();
            parsms.width = parent.getContext()
                    .getResources()
                    .getDisplayMetrics()
                    .widthPixels / 3;
            parsms.height = parsms.width;
            return holder;
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position)
        {
            if (mPickModelList.size() > position)
            {
                PickModel model = mPickModelList.get(position);
                Picasso
                        .with(PickImgSimpleActivity.this)
                        .load(new File(model.mImgPath))
                        .fit()
                        .into(holder.mImageView);
            }
        }

        @Override
        public int getItemCount()
        {
            return mPickModelList.size();
        }
    }

    private class ImageHolder extends RecyclerView.ViewHolder
    {

        private ImageView mImageView;

        public ImageHolder(View itemView)
        {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.item_img);
            itemView.findViewById(R.id.item_checkbox).setVisibility(View.GONE);
        }
    }
}
