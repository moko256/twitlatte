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

package com.github.moko256.twicalico;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import twitter4j.MediaEntity;

/**
 * Created by moko256 on 2016/10/29.
 *
 * @author moko256
 */

public class ImagePagerChildFragment extends Fragment {

    private static String FRAG_MEDIA_ENTITY="media_entity";

    ImageView imageView;

    SimpleExoPlayerView videoPlayView;
    SimpleExoPlayer player;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_image_pager_child, null);

        MediaEntity mediaEntity=(MediaEntity) getArguments().getSerializable(FRAG_MEDIA_ENTITY);

        if (mediaEntity == null) {
            return view;
        }

        showSystemUI();

        switch (mediaEntity.getType()){
            case "video":
                String videoPath="";
                for(MediaEntity.Variant variant : mediaEntity.getVideoVariants()){
                    if(variant.getContentType().equals("application/x-mpegURL")){
                        videoPath=variant.getUrl();
                    }
                }

                videoPlayView = view.findViewById(R.id.fragment_image_pager_video);
                videoPlayView.setVisibility(View.VISIBLE);
                videoPlayView.setControllerVisibilityListener(visibility -> {
                    if (visibility != View.VISIBLE){
                        hideSystemUI();
                    }
                });

                getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                    if (visibility == View.SYSTEM_UI_FLAG_VISIBLE){
                        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                        videoPlayView.showController();
                    }
                });

                player = ExoPlayerFactory.newSimpleInstance(
                        getContext(),
                        new DefaultTrackSelector(
                                new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter())
                        ),
                        new DefaultLoadControl()
                );
                if (savedInstanceState != null){
                    player.seekTo(savedInstanceState.getLong("video_time", 0));
                }
                videoPlayView.setPlayer(player);
                player.prepare(
                        new HlsMediaSource(
                                Uri.parse(videoPath),
                                new DefaultDataSourceFactory(
                                        getContext(),
                                        getResources().getText(R.string.app_name).toString()
                                ),
                                HlsMediaSource.DEFAULT_MIN_LOADABLE_RETRY_COUNT,
                                new Handler(),
                                null
                        )
                );
                break;

            case "animated_gif":
                videoPlayView = view.findViewById(R.id.fragment_image_pager_video);
                videoPlayView.setVisibility(View.VISIBLE);
                videoPlayView.setControllerVisibilityListener(visibility -> {
                    if (visibility != View.VISIBLE){
                        hideSystemUI();
                    }
                });

                getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                    if (visibility == View.SYSTEM_UI_FLAG_VISIBLE){
                        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                        videoPlayView.showController();
                    }
                });

                player = ExoPlayerFactory.newSimpleInstance(
                        getContext(),
                        new DefaultTrackSelector(
                                new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter())
                        ),
                        new DefaultLoadControl()
                );
                if (savedInstanceState != null){
                    player.seekTo(savedInstanceState.getLong("video_time", 0));
                }
                videoPlayView.setPlayer(player);
                player.prepare(
                        new LoopingMediaSource(
                                new ExtractorMediaSource(
                                        Uri.parse(mediaEntity.getVideoVariants()[0].getUrl()),
                                        new DefaultDataSourceFactory(
                                                getContext(),
                                                getResources().getText(R.string.app_name).toString()
                                        ),
                                        new DefaultExtractorsFactory(),
                                        new Handler(),
                                        Throwable::printStackTrace
                                )
                        )
                );
                player.setPlayWhenReady(true);

                break;

            case "photo":
            default:
                getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                    if (visibility == View.SYSTEM_UI_FLAG_VISIBLE){
                        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                    }
                });
                imageView= view.findViewById(R.id.fragment_image_pager_image);
                imageView.setVisibility(View.VISIBLE);
                imageView.setOnClickListener(v -> {
                    if (getActivity().getWindow().getDecorView().getSystemUiVisibility() != View.SYSTEM_UI_FLAG_FULLSCREEN){
                        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                        hideSystemUI();
                    }
                });
                Glide.with(this)
                        .load(mediaEntity.getMediaURLHttps()+":large")
                        .fitCenter()
                        .crossFade(0)
                        .thumbnail(Glide.with(this).load(mediaEntity.getMediaURLHttps()+":small").fitCenter())
                        .into(imageView);
                break;
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (player != null){
            outState.putLong("video_time", player.getCurrentPosition());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (player != null){
            player.release();
            player = null;
        }
        videoPlayView = null;
        imageView = null;
    }

    private void hideSystemUI() {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static ImagePagerChildFragment getInstance(MediaEntity entity){
        ImagePagerChildFragment fragment=new ImagePagerChildFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(FRAG_MEDIA_ENTITY,entity);
        fragment.setArguments(bundle);
        return fragment;
    }
}
