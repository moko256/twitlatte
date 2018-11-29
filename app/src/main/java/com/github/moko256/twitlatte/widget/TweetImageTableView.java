/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.twitlatte.GlobalApplicationKt;
import com.github.moko256.twitlatte.R;
import com.github.moko256.twitlatte.ShowMediasActivity;
import com.github.moko256.twitlatte.entity.ClientType;
import com.github.moko256.twitlatte.entity.Media;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.glide.GlideRequests;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.gridlayout.widget.GridLayout;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_HIDE_SENSITIVE_MEDIA;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_TIMELINE_IMAGE_LOAD_MODE;

/**
 * Created by moko256 on 2016/06/11.
 *
 * @author moko256
 */
public class TweetImageTableView extends GridLayout {

    private final FrameLayout containers[] = new FrameLayout[4];

    private final ImageView imageViews[] = new ImageView[4];
    private final View foregrounds[] = new View[4];
    private final ImageView playButton[] = new ImageView[4];
    private final ImageView markImage[] = new ImageView[4];

    private Media medias[];

    @ClientType.ClientTypeInt
    private int type = ClientType.NOTHING;

    private boolean isOpen = true;

    private GlideRequests requests;

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

        float dp = context.getResources().getDisplayMetrics().density;
        Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_play_arrow_white_24dp);
        Drawable gifMark = AppCompatResources.getDrawable(context, R.drawable.ic_gif_white_24dp);

        for (int i=0; i < imageViews.length; i++) {
            imageViews[i] = new ImageView(context);
            imageViews[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews[i].setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            foregrounds[i] = new View(context);
            foregrounds[i].setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            foregrounds[i].setBackgroundColor(0x33000000);

            playButton[i] = new ImageView(context);
            int dp48 = Math.round(48 * dp);
            FrameLayout.LayoutParams playButtonParams = new FrameLayout.LayoutParams(dp48,dp48);
            playButtonParams.gravity = Gravity.CENTER;
            playButton[i].setLayoutParams(playButtonParams);
            playButton[i].setImageDrawable(drawable);

            markImage[i] = new ImageView(context);
            FrameLayout.LayoutParams markImageParams = new FrameLayout.LayoutParams(dp48,dp48);
            markImageParams.gravity = Gravity.BOTTOM | Gravity.START;
            markImage[i].setLayoutParams(markImageParams);
            markImage[i].setImageDrawable(gifMark);

            containers[i] = new FrameLayout(context);
            containers[i].addView(imageViews[i]);
            containers[i].addView(foregrounds[i]);
            containers[i].addView(playButton[i]);
            containers[i].addView(markImage[i]);
            containers[i].setOnLongClickListener(v -> TweetImageTableView.this.performLongClick());

            int finalI = i;
            containers[i].setOnClickListener(v -> {
                if (isOpen){
                    getContext().startActivity(ShowMediasActivity.getIntent(getContext(), medias, type, finalI));
                } else {
                    isOpen = true;
                    updateView();
                }
            });

            requests = GlideApp.with(this);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int widthSize=MeasureSpec.getSize(widthSpec);
        int heightSize=MeasureSpec.getSize(heightSpec);

        int widthMode=MeasureSpec.getMode(widthSpec);
        int heightMode=MeasureSpec.getMode(heightSpec);

        if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
            widthMode=MeasureSpec.EXACTLY;
            widthSize=heightSize/9*16;
        } else if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY){
            heightMode=MeasureSpec.EXACTLY;
            heightSize=widthSize/16*9;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(widthSize, widthMode),
                MeasureSpec.makeMeasureSpec(heightSize, heightMode)
        );
    }

    private void updateImageNumber(){
        int imageNum = medias.length;
        if (imageNum > 4){
            imageNum = 4;
        }
        removeAllViews();
        for(int i = 0; i < imageNum; i++){
            addView(containers[i]);
        }
        for(int i = 0; i < imageNum; i++){
            float dens = getContext().getResources().getDisplayMetrics().density;
            int param[] = params[imageNum - 1][i];
            int margin[] = margins[imageNum -1][i];
            LayoutParams params = makeGridLayoutParams(param[0],param[1],param[2],param[3]);
            params.setMargins(0, 0, Math.round(dens * margin[0]), Math.round(dens * margin[1]));
            containers[i].setLayoutParams(params);
        }
    }

    private LayoutParams makeGridLayoutParams(int row, int column, int rowSpan, int colSpan){
        LayoutParams params=new LayoutParams(new ViewGroup.LayoutParams(0, 0));
        params.rowSpec=spec(row,rowSpan,1);
        params.columnSpec=spec(column,colSpan,1);
        return params;
    }

    public void setMediaEntities(
            Media[] mediaEntities,
            @ClientType.ClientTypeInt int type,
            boolean sensitive
    ) {
        this.medias = mediaEntities;
        this.type = type;
        isOpen = !GlobalApplicationKt.preferenceRepository.getString(KEY_TIMELINE_IMAGE_LOAD_MODE, "normal").equals("none")
                && !(sensitive && GlobalApplicationKt.preferenceRepository.getBoolean(KEY_HIDE_SENSITIVE_MEDIA, true));
        updateImageNumber();
        updateView();
    }

    private void updateView(){
        int imageNum = medias.length;
        if (imageNum > 4){
            imageNum = 4;
        }
        for (int ii = 0; ii < imageNum; ii++) {

            String thumbnailUrl = medias[ii].getThumbnailUrl();
            String originalUrl = medias[ii].getOriginalUrl();

            String url = thumbnailUrl == null? originalUrl: thumbnailUrl;
            ImageView imageView = imageViews[ii];

            if (isOpen) {
                requests
                        .load(
                                GlobalApplicationKt.preferenceRepository.getString(KEY_TIMELINE_IMAGE_LOAD_MODE, "normal").equals("normal")?
                                        TwitterStringUtils.convertSmallImageUrl(type, url):
                                        TwitterStringUtils.convertThumbImageUrl(type, url)
                        )
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView);
                switch (medias[ii].getImageType()) {
                    case "video_one":
                    case "video_multi":
                        foregrounds[ii].setVisibility(VISIBLE);
                        playButton[ii].setVisibility(VISIBLE);
                        markImage[ii].setVisibility(GONE);
                        break;
                    case "gif":
                        foregrounds[ii].setVisibility(VISIBLE);
                        playButton[ii].setVisibility(VISIBLE);
                        markImage[ii].setVisibility(VISIBLE);
                        break;
                    default:
                        foregrounds[ii].setVisibility(GONE);
                        playButton[ii].setVisibility(GONE);
                        markImage[ii].setVisibility(GONE);
                        break;
                }
            } else {
                String timelineImageLoadMode = GlobalApplicationKt.preferenceRepository.getString(KEY_TIMELINE_IMAGE_LOAD_MODE, "normal");
                if (!timelineImageLoadMode.equals("none")){
                    requests
                            .load(
                                    timelineImageLoadMode.equals("normal")?
                                            TwitterStringUtils.convertSmallImageUrl(type, url):
                                            TwitterStringUtils.convertThumbImageUrl(type, url)
                            )
                            .transform(new BlurTransformation())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.border_frame);
                }

                foregrounds[ii].setVisibility(GONE);
                playButton[ii].setVisibility(GONE);
                markImage[ii].setVisibility(GONE);
            }
        }
    }

    public void clearImages(){
        for (int i = 0; i < 4; i++){
            requests.clear(imageViews[i]);
        }
    }
}
