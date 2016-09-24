package com.moko256.twitterviewer256;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import twitter4j.ExtendedMediaEntity;

/**
 * Created by moko256 on 2016/06/11.
 *
 * @author moko256
 */
public class TweetImageTableView extends android.support.v7.widget.GridLayout {

    private ImageView imageViews[];

    public TweetImageTableView(Context context) {
        this(context, null);
    }

    public TweetImageTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TweetImageTableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int ids[]={
                R.id.layout_tweet_image_1,
                R.id.layout_tweet_image_2,
                R.id.layout_tweet_image_3,
                R.id.layout_tweet_image_4
        };

        LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet_image_table,this);

        imageViews=new ImageView[4];


        for(int i=0;i<ids.length;i++){
            imageViews[i]=(ImageView) findViewById(ids[i]);
        }
    }

    public void setTwitterMediaEntities(ExtendedMediaEntity mediaEntities[]){

        /* {row,column,rowSpan,colSpan} */
        int params[][][]={
                {{0,0,2,2},{},{},{}},
                {{0,0,2,1},{0,1,2,1},{},{}},
                {{0,0,2,1},{0,1,1,1},{1,1,1,1},{}},
                {{0,0,1,1},{0,1,1,1},{1,0,1,1},{1,1,1,1}}
        };

        for(int i=0;i<4;i++){

            ImageView imageView=imageViews[i];

            if(i<mediaEntities.length){
                imageView.setVisibility(VISIBLE);

                ExtendedMediaEntity mediaEntity=mediaEntities[i];
                String urlText=mediaEntity.getMediaURLHttps()+":orig";

                int param[]=params[mediaEntities.length-1][i];

                imageView.setLayoutParams(makeGridLayoutParams(param[0],param[1],param[2],param[3]));
                imageView.setOnClickListener(v->{
                    Intent intent=new Intent(getContext(),ShowTweetImageActivity.class);
                    intent.putExtra("TweetMediaEntity",mediaEntity);
                    getContext().startActivity(intent);
                });
                Glide.with(getContext()).load(urlText).centerCrop().into(imageView);
            }else{
                imageView.setVisibility(GONE);
            }

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

    private LayoutParams makeGridLayoutParams(int row,int column,int rowSpan,int colSpan){
        LayoutParams params=new LayoutParams();
        params.rowSpec=spec(row,rowSpan,1F);
        params.columnSpec=spec(column,colSpan,1F);
        return params;
    }
}
