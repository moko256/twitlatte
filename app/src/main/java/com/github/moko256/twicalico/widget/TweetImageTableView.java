/*
 * Copyright 2015-2018 The twicalico authors
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

import com.github.moko256.twicalico.GlideApp;
import com.github.moko256.twicalico.GlobalApplication;
import com.github.moko256.twicalico.R;
import com.github.moko256.twicalico.ShowImageActivity;
import com.github.moko256.twicalico.text.TwitterStringUtils;

import jp.wasabeef.glide.transformations.BlurTransformation;
import twitter4j.MediaEntity;

/**
 * Created by moko256 on 2016/06/11.
 *
 * @author moko256
 */
public class TweetImageTableView extends GridLayout {

    private final ImageView imageViews[] = new ImageView[4];
    private final FrameLayout covers[] = new FrameLayout[4];

    private MediaEntity mediaEntities[];

    private boolean isOpen = true;

    /* {row,column,rowSpan,colSpan} */
    private static final int params[][][]={
            {{0,0,2,2},{0,0,0,0},{0,0,0,0},{0,0,0,0}},
            {{0,0,2,1},{0,1,2,1},{0,0,0,0},{0,0,0,0}},
            {{0,0,2,1},{0,1,1,1},{1,1,1,1},{0,0,0,0}},
            {{0,0,1,1},{0,1,1,1},{1,0,1,1},{1,1,1,1}}
    };

    /* {right,bottom} */
    private static final int margins[][][]={
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

        for (int i=0; i < imageViews.length; i++) {
            imageViews[i] = new ImageView(context);
            imageViews[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews[i].setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            covers[i] = new FrameLayout(context);
            covers[i].addView(imageViews[i]);
            covers[i].setOnLongClickListener(v -> TweetImageTableView.this.performLongClick());

            int finalI = i;
            covers[i].setOnClickListener(v -> {
                if (isOpen){
                    getContext().startActivity(ShowImageActivity.getIntent(getContext(),mediaEntities, finalI));
                } else {
                    isOpen = true;
                    updateView();
                }
            });
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
        } else if (widthMode==MeasureSpec.EXACTLY){
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
        if (imageNum > 4){
            imageNum = 4;
        }
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

    public void setMediaEntities(MediaEntity[] mediaEntities, boolean sensitive) {
        this.mediaEntities = mediaEntities;
        isOpen = GlobalApplication.configuration.isTimelineImageLoad && !sensitive;
        updateImageNumber();
        updateView();
    }

    private void updateView(){
        int imageNum = mediaEntities.length;
        if (imageNum > 4){
            imageNum = 4;
        }
        for (int ii = 0; ii < imageNum; ii++) {
            if (isOpen) {
                GlideApp.with(getContext())
                        .load(TwitterStringUtils.convertLargeImageUrl(
                                mediaEntities[ii].getMediaURLHttps()
                        ))
                        .sizeMultiplier(0.3f)
                        .into(imageViews[ii]);
                switch (mediaEntities[ii].getType()) {
                    case "video":
                        covers[ii].setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.player_foreground));
                        break;
                    case "animated_gif":
                        covers[ii].setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.gif_foreground));
                        break;
                    default:
                        covers[ii].setForeground(null);
                        break;
                }
            } else {
                if (GlobalApplication.configuration.isTimelineImageLoad){
                    GlideApp.with(getContext())
                            .load(TwitterStringUtils.convertLargeImageUrl(
                                    mediaEntities[ii].getMediaURLHttps()
                            ))
                            .sizeMultiplier(0.3f)
                            .transform(new BlurTransformation())
                            .into(imageViews[ii]);
                } else {
                    imageViews[ii].setImageResource(R.drawable.border_frame);
                }
            }
        }
    }

    public void clearImages(){
        for (int i = 0; i < 4; i++){
            GlideApp.with(this).clear(imageViews[i]);
        }
    }
}
