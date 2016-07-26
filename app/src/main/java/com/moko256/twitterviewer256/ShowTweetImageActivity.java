package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by moko256 on GitHub on 2016/06/26.
 */
public class ShowTweetImageActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet_image);

        Glide.with(this).load(getIntent().getStringExtra("TweetImageUrl")).into((ImageView)findViewById(R.id.tweet_image_show_image_view));
    }
}
