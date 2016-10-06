package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.Objects;

import twitter4j.ExtendedMediaEntity;

/**
 * Created by moko256 on 2016/06/26.
 *
 * @author moko256
 */
public class ShowTweetImageActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet_image);

        ExtendedMediaEntity mediaEntity=(ExtendedMediaEntity) getIntent().getSerializableExtra("TweetMediaEntity");
        ImageView imageView;
        VideoView videoView;

        switch (mediaEntity.getType()){
            case "video":
                videoView=(VideoView) findViewById(R.id.tweet_image_show_video_view);
                videoView.setVisibility(View.VISIBLE);
                String videoPath="";
                for(ExtendedMediaEntity.Variant variant : mediaEntity.getVideoVariants()){
                    if(Objects.equals(variant.getContentType(), "application/x-mpegURL")){
                        videoPath=variant.getUrl();
                    }
                }
                videoView.setVideoPath(videoPath);
                videoView.setMediaController(new MediaController(this));
                break;

            case "animated_gif":
                videoView=(VideoView) findViewById(R.id.tweet_image_show_video_view);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(mediaEntity.getVideoVariants()[0].getUrl());
                videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));
                videoView.start();
                break;

            case "photo":
            default:
                imageView=(ImageView)findViewById(R.id.tweet_image_show_image_view);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(mediaEntity.getMediaURLHttps()+":orig").into(imageView);
                break;
        }
    }
}
