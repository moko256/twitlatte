package com.moko256.twitterviewer256;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;
import twitter4j.util.TimeSpanConverter;

/**
 * Created by moko256 on 2016/02/11.
 *
 * @author moko256
 */
class TweetListAdapter extends BaseListAdapter<Status,RecyclerView.ViewHolder> {

    private View headerView;

    private int headerCount=0;

    TweetListAdapter(Context context, ArrayList<Status> data) {
        super(context,data);
        headerView=new View(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position==getHeaderCount()-1){
            return 1;
        }
        else{
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i==1){
            headerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderViewHolder(headerView);
        }
        else {
            return new ContentViewHolder(inflater.inflate(R.layout.layout_tweet, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int ii) {
        final int i=ii-getHeaderCount();
        if (viewHolder instanceof HeaderViewHolder){
            onBindHeaderViewHolder((HeaderViewHolder) viewHolder,i);
        }
        else if (viewHolder instanceof ContentViewHolder){
            onBindContentViewHolder((ContentViewHolder) viewHolder,i);
        }
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return getHeaderCount()+data.size();
        } else {
            return getHeaderCount();
        }
    }

    public int getHeaderCount() {
        return headerCount;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        if(getHeaderCount()==0){
            headerCount=1;
            notifyItemRangeInserted(0,1);
        }
        else{
            notifyItemChanged(0);
        }
    }

    private void onBindHeaderViewHolder(HeaderViewHolder headerViewHolder, final int i){

    }

    private void onBindContentViewHolder(ContentViewHolder contentViewHolder,final int i){
        Status status=data.get(i);

        final Status item = status.isRetweet()?status.getRetweetedStatus():status;

        if (status.isRetweet()){
            contentViewHolder.tweetRetweetUserName.setVisibility(View.VISIBLE);
            contentViewHolder.tweetRetweetUserName.setText(context.getString(R.string.retweet_by,status.getUser().getName()));
        }
        else{
            contentViewHolder.tweetRetweetUserName.setVisibility(View.GONE);
        }

        Glide.with(context).load(item.getUser().getBiggerProfileImageURL()).into(contentViewHolder.tweetUserImage);

        contentViewHolder.tweetUserName.setText(item.getUser().getName());
        contentViewHolder.tweetUserId.setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
        contentViewHolder.tweetContext.setText(TwitterStringUtil.getLinkedSequence(item,context));
        contentViewHolder.tweetContext.setMovementMethod(LinkMovementMethod.getInstance());
        contentViewHolder.tweetContext.setFocusable(false);

        contentViewHolder.tweetTimeStampText.setText((new TimeSpanConverter()).toTimeSpanString(item.getCreatedAt().getTime()));
        contentViewHolder.tweetUserImage.setOnClickListener(v->{
            ViewCompat.setTransitionName(contentViewHolder.tweetUserImage,"tweet_user_image");
            Intent intent = new Intent(context,ShowUserActivity.class);
            intent.putExtra("user",item.getUser());
            context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, contentViewHolder.tweetUserImage,"tweet_user_image").toBundle());
        });
        contentViewHolder.tweetCardView.setOnClickListener(v -> {
            ViewCompat.setTransitionName(contentViewHolder.tweetUserImage,"tweet_user_image");
            Intent intent = new Intent(context,ShowTweetActivity.class);
            intent.putExtra("status",item);
            context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, contentViewHolder.tweetUserImage,"tweet_user_image").toBundle());
        });

        ExtendedMediaEntity mediaEntities[]=item.getExtendedMediaEntities();

        if (mediaEntities.length!=0){
            contentViewHolder.tweetImageTableView.setVisibility(View.VISIBLE);
            contentViewHolder.tweetImageTableView.setTwitterMediaEntities(mediaEntities);
        }
        else{
            contentViewHolder.tweetImageTableView.setVisibility(View.GONE);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(final View itemView){
            super(itemView);
        }
    }

    class ContentViewHolder extends RecyclerView.ViewHolder {

        CardView tweetCardView;
        ImageView tweetUserImage;
        TextView tweetRetweetUserName;
        TextView tweetUserName;
        TextView tweetUserId;
        TextView tweetContext;
        TextView tweetTimeStampText;
        TweetImageTableView tweetImageTableView;

        ContentViewHolder(final View itemView) {
            super(itemView);
            tweetCardView=(CardView) itemView.findViewById(R.id.tweet_card_view);
            tweetUserImage=(ImageView) itemView.findViewById(R.id.TLimage);
            tweetRetweetUserName=(TextView) itemView.findViewById(R.id.tweet_retweet_user_name);
            tweetUserId=(TextView) itemView.findViewById(R.id.tweet_user_id);
            tweetUserName=(TextView) itemView.findViewById(R.id.tweet_user_name);
            tweetContext=(TextView) itemView.findViewById(R.id.tweet_content);
            tweetTimeStampText=(TextView) itemView.findViewById(R.id.tweet_time_stamp_text);
            tweetImageTableView=(TweetImageTableView) itemView.findViewById(R.id.tweet_image_container);
        }
    }
}
