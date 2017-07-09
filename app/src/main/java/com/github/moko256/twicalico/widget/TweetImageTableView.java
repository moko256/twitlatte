/*
 * Copyright 2016 The twicalico authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twicalico.widget;

import android.content.Context;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.moko256.twicalico.GlobalApplication;
import com.github.moko256.twicalico.R;
import com.github.moko256.twicalico.ShowImageActivity;

import twitter4j.MediaEntity;

/**
 * Created by moko256 on 2016/06/11.
 *
 * @author moko256
 */
public class TweetImageTableView extends GridLayout {

    private ImageView imageViews[];
    private MediaEntity mediaEntities[];
    private FrameLayout covers[];

    /* {row,column,rowSpan,colSpan} */
    private final int params[][][]={
            {{0,0,2,2},{0,0,0,0},{0,0,0,0},{0,0,0,0}},
            {{0,0,2,1},{0,1,2,1},{0,0,0,0},{0,0,0,0}},
            {{0,0,2,1},{0,1,1,1},{1,1,1,1},{0,0,0,0}},
            {{0,0,1,1},{0,1,1,1},{1,0,1,1},{1,1,1,1}}
    };

    /* {right,bottom} */
    private final int margins[][][]={
            {{0,0},{0,0},{0,0},{0,0}},
            {{4,0},{0,0},{0,0},{0,0}},
            {{4,0},{0,4},{0,0},{0,0}},
            {{4,4},{0,4},{4,0},{0,0}}
    };

    public TweetImageTableView(Context context) {
        this(context, null);
    }

    public TweetImageTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TweetImageTableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setColumnCount(2);
        setRowCount(2);

        imageViews=new ImageView[4];
        covers = new FrameLayout[4];

        for(int i=0;i<imageViews.length;i++){
            imageViews[i] = new ImageView(context);
            imageViews[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews[i].setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            covers[i] = new FrameLayout(context);
            covers[i].addView(imageViews[i]);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int widthSize=MeasureSpec.getSize(widthSpec);
        int heightSize=MeasureSpec.getSize(heightSpec);

        int widthMode=MeasureSpec.getMode(widthSpec);
        int heightMode=MeasureSpec.getMode(heightSpec);

        if (heightMode==MeasureSpec.EXACTLY){
            widthMode=MeasureSpec.EXACTLY;
            widthSize=heightSize/9*16;
        }

        if (widthMode==MeasureSpec.EXACTLY){
            heightMode=MeasureSpec.EXACTLY;
            heightSize=widthSize/16*9;
        }

        int measuredWidthSpec=MeasureSpec.makeMeasureSpec(widthSize,widthMode);
        int measuredHeightSpec=MeasureSpec.makeMeasureSpec(heightSize,heightMode);

        setMeasuredDimension(measuredWidthSpec,measuredHeightSpec);

        super.onMeasure(measuredWidthSpec, measuredHeightSpec);
    }

    private void updateImageNumber(){
        int imageNum = mediaEntities.length;
        removeAllViews();
        for(int i = 0; i < imageNum; i++){
            addView(covers[i]);
        }
        for(int i = 0; i < imageNum; i++){
            float dens = getContext().getResources().getDisplayMetrics().density;
            int param[] = params[imageNum - 1][i];
            int margin[] = margins[imageNum -1][i];
            LayoutParams params = makeGridLayoutParams(param[0],param[1],param[2],param[3]);
            params.setMargins(0, 0, Math.round(dens * margin[0]), Math.round(dens * margin[1]));
            covers[i].setLayoutParams(params);
        }
    }

    private LayoutParams makeGridLayoutParams(int row, int column, int rowSpan, int colSpan){
        LayoutParams params=new LayoutParams(new ViewGroup.LayoutParams(0, 0));
        params.rowSpec=spec(row,rowSpan,1);
        params.columnSpec=spec(column,colSpan,1);
        return params;
    }

    public void setMediaEntities(MediaEntity[] mediaEntities) {
        this.mediaEntities = mediaEntities;
        updateImageNumber();
        for (int ii = 0; ii < mediaEntities.length; ii++) {
            int finalIi = ii;
            covers[ii].setOnClickListener(v-> getContext().startActivity(ShowImageActivity.getIntent(getContext(),mediaEntities, finalIi)));

            if (GlobalApplication.configuration.isTimelineImageLoad()){
                Glide.with(getContext())
                        .load(mediaEntities[ii].getMediaURLHttps()+":small")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageViews[ii]);
                if (mediaEntities[ii].getType().equals("video")){
                    covers[ii].setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.player_foreground));
                } else if (mediaEntities[ii].getType().equals("animated_gif")){
                    covers[ii].setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.gif_foreground));
                } else {
                    covers[ii].setForeground(null);
                }
            } else {
                imageViews[ii].setImageResource(R.drawable.border_frame);
            }
        }
    }

    public void clearImages(){
        for (int i = 0; i < 4; i++){
            Glide.clear(imageViews[i]);
        }
    }
}
