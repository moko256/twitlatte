package com.github.moko256.twicalico;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.util.TimeSpanConverter;

/**
 * Created by moko256 on 2016/02/11.
 *
 * @author moko256
 */
class StatusesAdapter extends BaseListAdapter<Status,StatusesAdapter.ViewHolder> {

    StatusesAdapter(Context context, ArrayList<Status> data) {
        super(context,data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.layout_tweet, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Status status=data.get(i);

        final Status item = status.isRetweet()?status.getRetweetedStatus():status;

        if (status.isRetweet()){
            if(viewHolder.tweetRetweetUserName.getVisibility()!=View.VISIBLE){
                viewHolder.tweetRetweetUserName.setVisibility(View.VISIBLE);
            }
            viewHolder.tweetRetweetUserName.setText(context.getString(R.string.retweet_by,status.getUser().getName()));
        }
        else{
            if(viewHolder.tweetRetweetUserName.getVisibility()!=View.GONE){
                viewHolder.tweetRetweetUserName.setVisibility(View.GONE);
            }
        }

        imageRequestManager.load(item.getUser().getBiggerProfileImageURL()).into(viewHolder.tweetUserImage);

        viewHolder.tweetUserName.setText(item.getUser().getName());
        viewHolder.tweetUserId.setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
        viewHolder.tweetContext.setText(TwitterStringUtil.getLinkedSequence(item,context));
        viewHolder.tweetContext.setMovementMethod(LinkMovementMethod.getInstance());
        viewHolder.tweetContext.setFocusable(false);

        viewHolder.tweetTimeStampText.setText(viewHolder.timeSpanConverter.toTimeSpanString(item.getCreatedAt().getTime()));
        viewHolder.tweetUserImage.setOnClickListener(v->{
            ViewCompat.setTransitionName(viewHolder.tweetUserImage,"tweet_user_image");
            Intent intent = new Intent(context,ShowUserActivity.class);
            intent.putExtra("user",item.getUser());
            context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, viewHolder.tweetUserImage,"tweet_user_image").toBundle());
        });
        viewHolder.tweetCardView.setOnClickListener(v -> {
            ViewCompat.setTransitionName(viewHolder.tweetUserImage,"tweet_user_image");
            Intent intent = new Intent(context,ShowTweetActivity.class);
            intent.putExtra("status",item);
            context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, viewHolder.tweetUserImage,"tweet_user_image").toBundle());
        });

        Status quotedStatus=item.getQuotedStatus();
        if(quotedStatus!=null){
            if (viewHolder.tweetQuoteTweetLayout.getVisibility() != View.VISIBLE) {
                viewHolder.tweetQuoteTweetLayout.setVisibility(View.VISIBLE);
            }
            viewHolder.tweetQuoteTweetLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context,ShowTweetActivity.class);
                intent.putExtra("statusId",(Long)quotedStatus.getId());
                context.startActivity(intent);
            });
            viewHolder.tweetQuoteTweetUserName.setText(quotedStatus.getUser().getName());
            viewHolder.tweetQuoteTweetUserId.setText(TwitterStringUtil.plusAtMark(quotedStatus.getUser().getScreenName()));
            viewHolder.tweetQuoteTweetContext.setText(quotedStatus.getText());
        }else{
            if (viewHolder.tweetQuoteTweetLayout.getVisibility() != View.GONE) {
                viewHolder.tweetQuoteTweetLayout.setVisibility(View.GONE);
            }
        }

        ExtendedMediaEntity mediaEntities[]=item.getExtendedMediaEntities();

        if (mediaEntities.length!=0){
            viewHolder.tweetImageTableView.setVisibility(View.VISIBLE);
            viewHolder.tweetImageTableView.setImageNumber(mediaEntities.length);
            for (int ii = 0; ii < mediaEntities.length; ii++) {
                ImageView imageView=viewHolder.tweetImageTableView.getImageView(ii);
                int finalIi = ii;
                imageView.setOnClickListener(v-> context.startActivity(ShowImageActivity.getIntent(context,mediaEntities, finalIi)));
                imageRequestManager.load(mediaEntities[ii].getMediaURLHttps()).placeholder(R.drawable.border_frame).centerCrop().into(imageView);
            }
        }
        else{
            viewHolder.tweetImageTableView.setVisibility(View.GONE);
        }

        viewHolder.tweetLikeButton.setOnCheckedChangeListener((compoundButton, b) -> Observable
                .create(subscriber->{
                    try {
                        if(b&&(!item.isFavorited())){
                            subscriber.onNext(Static.twitter.createFavorite(item.getId()));
                        }
                        else if((!b)&&item.isFavorited()){
                            subscriber.onNext(Static.twitter.destroyFavorite(item.getId()));
                        }
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            //data.set(i,(Status)result);
                            //notifyItemChanged(i);
                            Toast.makeText(context,"succeed",Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(context,"error",Toast.LENGTH_SHORT).show();
                        },
                        ()->{}
                )
        );
        viewHolder.tweetLikeButton.setChecked(item.isFavorited());

        viewHolder.tweetRetweetButton.setOnCheckedChangeListener((compoundButton, b) -> Observable
                .create(subscriber->{
                    try {
                        if(b&&(!item.isRetweeted())){
                            subscriber.onNext(Static.twitter.retweetStatus(item.getId()));
                        }
                        else if((!b)&&item.isRetweeted()){
                            subscriber.onNext(Static.twitter.destroyStatus(item.getCurrentUserRetweetId()));
                        }
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            //data.set(i,(Status)result);
                            //notifyItemChanged(i);
                            Toast.makeText(context,"succeed",Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(context,"error",Toast.LENGTH_SHORT).show();
                        },
                        ()->{}
                )
        );
        viewHolder.tweetRetweetButton.setChecked(item.isRetweeted());
        viewHolder.tweetRetweetButton.setEnabled(!item.getUser().isProtected());
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CardView tweetCardView;
        ImageView tweetUserImage;
        TextView tweetRetweetUserName;
        TextView tweetUserName;
        TextView tweetUserId;
        TextView tweetContext;
        TextView tweetTimeStampText;
        RelativeLayout tweetQuoteTweetLayout;
        TextView tweetQuoteTweetUserName;
        TextView tweetQuoteTweetUserId;
        TextView tweetQuoteTweetContext;
        TweetImageTableView tweetImageTableView;
        ToggleButton tweetLikeButton;
        ToggleButton tweetRetweetButton;

        TimeSpanConverter timeSpanConverter;

        ViewHolder(final View itemView) {
            super(itemView);
            tweetCardView=(CardView) itemView.findViewById(R.id.tweet_card_view);
            tweetUserImage=(ImageView) itemView.findViewById(R.id.TLimage);
            tweetRetweetUserName=(TextView) itemView.findViewById(R.id.tweet_retweet_user_name);
            tweetUserId=(TextView) itemView.findViewById(R.id.tweet_user_id);
            tweetUserName=(TextView) itemView.findViewById(R.id.tweet_user_name);
            tweetContext=(TextView) itemView.findViewById(R.id.tweet_content);
            tweetTimeStampText=(TextView) itemView.findViewById(R.id.tweet_time_stamp_text);
            tweetQuoteTweetLayout=(RelativeLayout) itemView.findViewById(R.id.tweet_quote_tweet);
            tweetQuoteTweetUserName=(TextView) itemView.findViewById(R.id.tweet_quote_tweet_user_name);
            tweetQuoteTweetUserId=(TextView) itemView.findViewById(R.id.tweet_quote_tweet_user_id);
            tweetQuoteTweetContext=(TextView) itemView.findViewById(R.id.tweet_quote_tweet_content);
            tweetImageTableView=(TweetImageTableView) itemView.findViewById(R.id.tweet_image_container);
            tweetLikeButton=(ToggleButton) itemView.findViewById(R.id.tweet_content_like_button);
            tweetRetweetButton=(ToggleButton) itemView.findViewById(R.id.tweet_content_retweet_button);

            timeSpanConverter=new TimeSpanConverter();
        }
    }
}
