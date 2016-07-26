package com.moko256.twitterviewer256;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import twitter4j.ExtendedMediaEntity;

/**
 * Created by moko256 on GitHub on 2016/06/11.
 */
public class TweetImageTableView extends RecyclerView {

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
        setAdapter(new TweetImageAdapter(getContext(),mediaEntities));
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(manager);
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
