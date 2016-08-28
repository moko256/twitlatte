package com.moko256.twitterviewer256;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;

/**
 * Created by moko256 on GitHub on 2016/02/11.
 */
public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ArrayList<Status> mData;
    private Context mContext;

    public TweetListAdapter(Context context, ArrayList<Status> data, RecyclerView.RecyclerListener listener) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mData = data;
    }

    @Override
    public TweetListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(mInflater.inflate(R.layout.layout_tweet, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Status status=mData.get(i);

        final Status item = status.isRetweet()?status.getRetweetedStatus():status;

        if (status.isRetweet()){
            viewHolder.tweetRetweetUserName.setVisibility(View.VISIBLE);
            viewHolder.tweetRetweetUserName.setText(mContext.getString(R.string.retweet_by,status.getUser().getName()));
        }
        else{
            viewHolder.tweetRetweetUserName.setVisibility(View.GONE);
        }

        Glide.with(mContext).load(item.getUser().getBiggerProfileImageURL()).into(viewHolder.tweetUserImage);

        viewHolder.tweetUserName.setText(item.getUser().getName());
        viewHolder.tweetUserId.setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
        viewHolder.tweetContext.setText(TwitterStringUtil.getLinkedSequence(item,mContext));
        viewHolder.tweetContext.setMovementMethod(LinkMovementMethod.getInstance());
        viewHolder.tweetUserImage.setOnClickListener(v->{
            ViewCompat.setTransitionName(viewHolder.tweetUserImage,"tweet_user_image");
            Intent intent = new Intent(mContext,ShowUserActivity.class);
            intent.putExtra("user",item.getUser());
            mContext.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext,viewHolder.tweetUserImage,"tweet_user_image").toBundle());
        });
        viewHolder.itemView.setOnClickListener(v -> {
            ViewCompat.setTransitionName(viewHolder.tweetUserImage,"tweet_user_image");
            Intent intent = new Intent(mContext,ShowTweetActivity.class);
            intent.putExtra("status",item);
            mContext.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext,viewHolder.tweetUserImage,"tweet_user_image").toBundle());
        });

        ExtendedMediaEntity mediaEntities[]=item.getExtendedMediaEntities();

        if (mediaEntities.length!=0){
            viewHolder.tweetImageRecyclerView.setVisibility(View.VISIBLE);
            viewHolder.tweetImageRecyclerView.setTwitterMediaEntities(mediaEntities);
        }
        else{
            viewHolder.tweetImageRecyclerView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView tweetUserImage;
        TextView tweetRetweetUserName;
        TextView tweetUserName;
        TextView tweetUserId;
        TextView tweetContext;
        TweetImageTableView tweetImageRecyclerView;

        public ViewHolder(final View itemView) {
            super(itemView);
            tweetUserImage=(ImageView) itemView.findViewById(R.id.TLimage);
            tweetRetweetUserName=(TextView) itemView.findViewById(R.id.tweet_retweet_user_name);
            tweetUserId=(TextView) itemView.findViewById(R.id.tweet_user_id);
            tweetUserName=(TextView) itemView.findViewById(R.id.tweet_user_name);
            tweetContext=(TextView) itemView.findViewById(R.id.tweet_content);
            tweetImageRecyclerView=(TweetImageTableView) itemView.findViewById(R.id.tweet_image_container);
        }
    }
}
