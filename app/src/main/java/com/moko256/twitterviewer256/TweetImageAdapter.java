package com.moko256.twitterviewer256;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import twitter4j.ExtendedMediaEntity;
import twitter4j.MediaEntity;

/**
 * Created by moko256 on GitHub on 2016/02/11.
 */
public class TweetImageAdapter extends RecyclerView.Adapter<TweetImageAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;
    private MediaEntity mMediaEntities[];

    public TweetImageAdapter(Context context, ExtendedMediaEntity mediaEntities[]) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mMediaEntities=mediaEntities;
    }

    @Override
    public TweetImageAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(mInflater.inflate(R.layout.layout_tweet_image, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        MediaEntity mediaEntity=mMediaEntities[i];

        String urlText=mediaEntity.getMediaURL()+":orig";

        Glide.with(mContext).load(urlText).into(viewHolder.tweetImageView);
        viewHolder.tweetImageView.setOnClickListener(v->{
            Intent intent=new Intent(mContext,ShowTweetImageActivity.class);
            intent.putExtra("TweetImageUrl",urlText);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (mMediaEntities != null) {
            return mMediaEntities.length;
        } else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView tweetImageView;

        public ViewHolder(final View itemView) {
            super(itemView);
            tweetImageView=(ImageView) itemView.findViewById(R.id.tweet_image);
        }
    }
}
