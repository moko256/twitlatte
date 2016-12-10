package com.github.moko256.twitlatte;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.Objects;

import twitter4j.ExtendedMediaEntity;

/**
 * Created by moko256 on 2016/10/29.
 *
 * @author moko256
 */

public class ImagePagerChildFragment extends Fragment {

    private static String FRAG_MEDIA_ENTITY="media_entity";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_image_pager_child, null);

        ExtendedMediaEntity mediaEntity=(ExtendedMediaEntity) getArguments().getSerializable(FRAG_MEDIA_ENTITY);

        if (mediaEntity == null) {
            return view;
        }

        ImageView imageView;
        VideoView videoView;

        switch (mediaEntity.getType()){
            case "video":
                videoView=(VideoView) view.findViewById(R.id.fragment_image_pager_video);
                videoView.setVisibility(View.VISIBLE);
                String videoPath="";
                for(ExtendedMediaEntity.Variant variant : mediaEntity.getVideoVariants()){
                    if(Objects.equals(variant.getContentType(), "application/x-mpegURL")){
                        videoPath=variant.getUrl();
                    }
                }
                videoView.setVideoPath(videoPath);
                videoView.setMediaController(new MediaController(getContext()));
                break;

            case "animated_gif":
                videoView=(VideoView) view.findViewById(R.id.fragment_image_pager_video);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(mediaEntity.getVideoVariants()[0].getUrl());
                videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));
                videoView.start();
                break;

            case "photo":
            default:
                imageView=(ImageView) view.findViewById(R.id.fragment_image_pager_image);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(mediaEntity.getMediaURLHttps()+":orig").into(imageView);
                break;
        }
        return view;
    }

    public static ImagePagerChildFragment getInstance(ExtendedMediaEntity entity){
        ImagePagerChildFragment fragment=new ImagePagerChildFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(FRAG_MEDIA_ENTITY,entity);
        fragment.setArguments(bundle);
        return fragment;
    }
}
