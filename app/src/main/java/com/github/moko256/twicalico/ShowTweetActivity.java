package com.github.moko256.twicalico;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2016/03/10.
 * This Activity is to show a tweet.
 *
 * @author moko256
 */
public class ShowTweetActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        new AsyncTask<Void,Void,Status>(){
            @Override
            public twitter4j.Status doInBackground(Void... str){
                Long statusId;
                twitter4j.Status status=(twitter4j.Status) getIntent().getSerializableExtra("status");
                if(status==null) {
                    statusId = (Long) getIntent().getSerializableExtra("statusId");
                    if (statusId == null) {
                        Uri data = getIntent().getData();
                        if (data.getScheme().equals("https")||data.getScheme().equals("http")) {
                            statusId = Long.parseLong(data.getPathSegments().get(2), 10);
                        } else if (data.getScheme().equals("twitter")) {
                            statusId = Long.parseLong(data.getQueryParameter("id"), 10);
                        } else {
                            ShowTweetActivity.this.finish();
                        }
                    }
                    try {
                        status = Static.twitter.showStatus(statusId);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
                return status;
            }

            @Override
            public void onPostExecute(twitter4j.Status item){
                if (item == null) {
                    finish();
                    return;
                }
                RequestManager imageRequestManager=Glide.with(ShowTweetActivity.this);

                ImageView userImage=(ImageView) findViewById(R.id.tweet_show_image);

                imageRequestManager.load(item.getUser().getProfileImageURL()).into(userImage);

                ((TextView) findViewById(R.id.tweet_show_user_name)).setText(item.getUser().getName());
                ((TextView) findViewById(R.id.tweet_show_user_id)).setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
                TextView contentText=(TextView) findViewById(R.id.tweet_show_content);
                contentText.setText(TwitterStringUtil.getLinkedSequence(item,ShowTweetActivity.this));
                contentText.setMovementMethod(LinkMovementMethod.getInstance());

                userImage.setOnClickListener(v->{
                    Intent intent = new Intent(ShowTweetActivity.this,ShowUserActivity.class);
                    intent.putExtra("user",item.getUser());
                    startActivity(intent);
                });

                RelativeLayout tweetQuoteTweetLayout=(RelativeLayout) findViewById(R.id.tweet_show_quote_tweet);

                twitter4j.Status quotedStatus=item.getQuotedStatus();
                if(quotedStatus!=null){
                    if (tweetQuoteTweetLayout.getVisibility() != View.VISIBLE) {
                        tweetQuoteTweetLayout.setVisibility(View.VISIBLE);
                    }
                    tweetQuoteTweetLayout.setOnClickListener(v -> {
                        Intent intent = new Intent(ShowTweetActivity.this,ShowTweetActivity.class);
                        intent.putExtra("statusId",(Long)quotedStatus.getId());
                        startActivity(intent);
                    });
                    ((TextView) findViewById(R.id.tweet_show_quote_tweet_user_name)).setText(quotedStatus.getUser().getName());
                    ((TextView) findViewById(R.id.tweet_show_quote_tweet_user_id)).setText(TwitterStringUtil.plusAtMark(quotedStatus.getUser().getScreenName()));
                    ((TextView) findViewById(R.id.tweet_show_quote_tweet_content)).setText(quotedStatus.getText());
                }else{
                    if (tweetQuoteTweetLayout.getVisibility() != View.GONE) {
                        tweetQuoteTweetLayout.setVisibility(View.GONE);
                    }
                }

                MediaEntity mediaEntities[]=item.getMediaEntities();

                TweetImageTableView tableView=(TweetImageTableView) findViewById(R.id.tweet_show_images);
                if(mediaEntities.length!=0){
                    tableView.setVisibility(View.VISIBLE);
                    tableView.setImageNumber(mediaEntities.length);
                    for (int ii = 0; ii < mediaEntities.length; ii++) {
                        ImageView imageView=tableView.getImageView(ii);
                        int finalIi = ii;
                        imageView.setOnClickListener(v-> startActivity(ShowImageActivity.getIntent(ShowTweetActivity.this,mediaEntities, finalIi)));
                        imageRequestManager.load(mediaEntities[ii].getMediaURLHttps()).placeholder(R.drawable.border_frame).centerCrop().into(imageView);
                    }
                }else{
                    tableView.setVisibility(View.GONE);
                }

                ((TextView)findViewById(R.id.tweet_show_timestamp)).setText(DateUtils.formatDateTime(
                        ShowTweetActivity.this,item.getCreatedAt().getTime(),DateUtils.FORMAT_ABBREV_RELATIVE)
                );
                TextView viaText=(TextView)findViewById(R.id.tweet_show_via);
                viaText.setText(Html.fromHtml("via:"+item.getSource()));
                viaText.setMovementMethod(new LinkMovementMethod());

                AppCompatEditText replyText=(AppCompatEditText) findViewById(R.id.tweet_show_tweet_reply_text);
                AppCompatButton replyButton=(AppCompatButton) findViewById(R.id.tweet_show_tweet_reply_button);
                replyButton.setOnClickListener(v -> {
                    StatusUpdate update=new StatusUpdate(TwitterStringUtil.plusAtMark(item.getUser().getScreenName())+" "+replyText.getText().toString());
                    update.inReplyToStatusId(item.getId());
                    replyButton.setEnabled(false);
                    Observable
                            .create(subscriber -> {
                                try {
                                    subscriber.onNext(Static.twitter.updateStatus(update));
                                    subscriber.onCompleted();
                                } catch (TwitterException e) {
                                    subscriber.onError(e);
                                }
                            })
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    it->{},
                                    e->{
                                        e.printStackTrace();
                                        Toast.makeText(ShowTweetActivity.this,"error",Toast.LENGTH_SHORT).show();
                                        replyButton.setEnabled(true);
                                    },
                                    ()-> {
                                        replyText.setText("");
                                        replyButton.setEnabled(true);
                                        Toast.makeText(ShowTweetActivity.this,"completed",Toast.LENGTH_SHORT).show();
                                    }
                            );
                });
            }
        }.execute();

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

}
