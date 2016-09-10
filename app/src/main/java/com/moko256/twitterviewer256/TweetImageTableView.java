package com.moko256.twitterviewer256;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import twitter4j.ExtendedMediaEntity;

/**
 * Created by moko256 on 2016/06/11.
 *
 * @author moko256
 */
public class TweetImageTableView extends android.support.v7.widget.GridLayout {

    public TweetImageTableView(Context context) {
        this(context, null);
    }

    public TweetImageTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TweetImageTableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setTwitterMediaEntities(ExtendedMediaEntity mediaEntities[]){
        View view=null;

        switch (mediaEntities.length){
            case 1:
                view= LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet_image_1,this);
                break;
            case 2:
                view= LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet_image_2,this);
                break;
            case 3:
                view= LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet_image_3,this);
                break;
            case 4:
                view= LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet_image_4,this);
                break;

            case 0:
            default:
                return;

        }

        int ids[]={
                R.id.layout_tweet_image_1,
                R.id.layout_tweet_image_2,
                R.id.layout_tweet_image_3,
                R.id.layout_tweet_image_4
        };

        for(int i=0;i<mediaEntities.length;i++){

            ExtendedMediaEntity mediaEntity=mediaEntities[i];
            String urlText=mediaEntity.getMediaURLHttps()+":orig";

            ImageView imageView=(ImageView) view.findViewById(ids[i]);
            Glide.with(getContext()).load(urlText).centerCrop().into(imageView);
            imageView.setOnClickListener(v->{
                Intent intent=new Intent(getContext(),ShowTweetImageActivity.class);
                intent.putExtra("TweetMediaEntity",mediaEntity);
                getContext().startActivity(intent);
            });
        }

    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int widthSize=MeasureSpec.getSize(widthSpec);
        int heightSize=widthSize/16*9;

        setMeasuredDimension(widthSize,heightSize);

        int widthMode=MeasureSpec.getMode(widthSpec);
        int heightMode=MeasureSpec.getMode(heightSpec);

        int measuredWidthSpec=MeasureSpec.makeMeasureSpec(widthSize,widthMode);
        int measuredHeightSpec=MeasureSpec.makeMeasureSpec(heightSize,heightMode);

        super.onMeasure(measuredWidthSpec, measuredHeightSpec);
    }
}
