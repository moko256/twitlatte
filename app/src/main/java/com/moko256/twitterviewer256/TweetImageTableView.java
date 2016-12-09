package com.moko256.twitterviewer256;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import twitter4j.ExtendedMediaEntity;

/**
 * Created by moko256 on 2016/06/11.
 *
 * @author moko256
 */
public class TweetImageTableView extends GridLayout {

    private ImageView imageViews[];
    private ExtendedMediaEntity mediaEntities[];

    /* {row,column,rowSpan,colSpan} */
    private int params[][][]={
            {{0,0,2,2},{},{},{}},
            {{0,0,2,1},{0,1,2,1},{},{}},
            {{0,0,2,1},{0,1,1,1},{1,1,1,1},{}},
            {{0,0,1,1},{0,1,1,1},{1,0,1,1},{1,1,1,1}}
    };

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
            ImageView imageView;
            imageView=(ImageView) findViewById(ids[i]);
            int finalI = i;
            imageView.setOnClickListener(v-> getContext().startActivity(ShowImageActivity.getIntent(getContext(),mediaEntities,finalI)));
            imageViews[i]=imageView;
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int widthSize=MeasureSpec.getSize(widthSpec);
        int heightSize=MeasureSpec.getSize(heightSpec);

        int widthMode=MeasureSpec.getMode(widthSpec);
        int heightMode=MeasureSpec.getMode(heightSpec);

        if (widthMode!=MeasureSpec.EXACTLY){
            widthMode=MeasureSpec.EXACTLY;
            widthSize=heightSize/9*16;
        }

        if (heightMode!=MeasureSpec.EXACTLY){
            heightMode=MeasureSpec.EXACTLY;
            heightSize=widthSize/16*9;
        }

        int measuredWidthSpec=MeasureSpec.makeMeasureSpec(widthSize,widthMode);
        int measuredHeightSpec=MeasureSpec.makeMeasureSpec(heightSize,heightMode);

        setMeasuredDimension(measuredWidthSpec,measuredHeightSpec);

        super.onMeasure(measuredWidthSpec, measuredHeightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int l=(mediaEntities==null)?0:mediaEntities.length;

        for(int i=0;i<4;i++){

            ImageView imageView=imageViews[i];

            if(i<l){

                int param[]=params[l-1][i];

                imageView.setVisibility(VISIBLE);
                imageView.setLayoutParams(makeGridLayoutParams(param[0],param[1],param[2],param[3]));
            }else{
                imageView.setLayoutParams(makeGridLayoutParams(0,0,0,0));
                imageView.setVisibility(GONE);
            }

        }

        super.onLayout(changed, left, top, right, bottom);
    }

    public void setTwitterMediaEntities(ExtendedMediaEntity mMediaEntities[]){

        mediaEntities=mMediaEntities;

        forceLayout();

        for(int i=0;i<mMediaEntities.length;i++){

            Glide.with(getContext())
                    .load(mMediaEntities[i].getMediaURLHttps())
                    .placeholder(R.drawable.border_frame)
                    .centerCrop()
                    .into(imageViews[i]);

        }

    }

    public void onRecycled(){
        for (ImageView imageView : imageViews){
            Glide.clear(imageView);
        }
    }

    private LayoutParams makeGridLayoutParams(int row, int column, int rowSpan, int colSpan){
        LayoutParams params=new LayoutParams(new ViewGroup.LayoutParams(getMeasuredWidth()/2*colSpan,getMeasuredHeight()/2*rowSpan));
        params.rowSpec=spec(row,rowSpan);
        params.columnSpec=spec(column,colSpan);
        return params;
    }
}
