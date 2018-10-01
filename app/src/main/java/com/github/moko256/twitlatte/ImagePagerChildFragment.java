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

package com.github.moko256.twitlatte;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chuross.flinglayout.FlingLayout;
import com.github.moko256.twitlatte.entity.Media;
import com.github.moko256.twitlatte.exoplayer.AudioAndVideoRenderer;
import com.github.moko256.twitlatte.glide.GlideApp;
import com.github.moko256.twitlatte.glide.GlideRequests;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import kotlin.Unit;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by moko256 on 2016/10/29.
 *
 * @author moko256
 */

public class ImagePagerChildFragment extends Fragment {

    private static final String FRAG_MEDIA_ENTITY = "media_entity";
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 100;

    private Media mediaEntity;

    private PhotoView imageView;

    private PlayerView videoPlayView;
    private SimpleExoPlayer player;

    private DefaultTrackSelector trackSelector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() == null
                || (mediaEntity = (Media) getArguments().getSerializable(FRAG_MEDIA_ENTITY)) == null) {
            requireActivity().finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FlingLayout view = (FlingLayout) inflater.inflate(R.layout.fragment_image_pager_child, container, false);
        view.setDismissListener(() -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
            return Unit.INSTANCE;
        });
        view.setPositionChangeListener((Integer top, Integer left, Float dragRangeRate) -> {
            if (!isShowingSystemUI()) {
                showSystemUI();
            }
            return Unit.INSTANCE;
        });

        Context context = requireContext();
        switch (mediaEntity.getImageType()){
            case "video_multi":
                videoPlayView = view.findViewById(R.id.fragment_image_pager_video);
                videoPlayView.setControllerShowTimeoutMs(1000);
                videoPlayView.setVisibility(View.VISIBLE);
                videoPlayView.setControllerVisibilityListener(visibility -> {
                    if (visibility != View.VISIBLE){
                        hideSystemUI();
                    } else {
                        showSystemUI();
                    }
                });
                videoPlayView.setErrorMessageProvider(
                        throwable -> Pair.create(0, TwitterStringUtils.convertErrorToText(throwable))
                );

                setSystemUIVisibilityListener(visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0){
                        showActionbar();

                        videoPlayView.showController();
                    }
                });

                trackSelector = new DefaultTrackSelector();

                player = ExoPlayerFactory.newSimpleInstance(
                        context,
                        new AudioAndVideoRenderer(context),
                        trackSelector,
                        new DefaultLoadControl()
                );
                videoPlayView.setPlayer(player);
                player.prepare(
                        new HlsMediaSource
                                .Factory(createOkHttpDataSourceFactory())
                                .createMediaSource(Uri.parse(mediaEntity.getOriginalUrl()))
                );

                break;

            case "video_one":
                videoPlayView = view.findViewById(R.id.fragment_image_pager_video);
                videoPlayView.setControllerShowTimeoutMs(1000);
                videoPlayView.setVisibility(View.VISIBLE);
                videoPlayView.setControllerVisibilityListener(visibility -> {
                    if (visibility != View.VISIBLE){
                        hideSystemUI();
                    } else {
                        showSystemUI();
                    }
                });
                videoPlayView.setErrorMessageProvider(
                        throwable -> Pair.create(0, TwitterStringUtils.convertErrorToText(throwable))
                );

                setSystemUIVisibilityListener(visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0){
                        showActionbar();

                        videoPlayView.showController();
                    }
                });

                trackSelector = new DefaultTrackSelector();

                player = ExoPlayerFactory.newSimpleInstance(
                        context,
                        new AudioAndVideoRenderer(context),
                        trackSelector,
                        new DefaultLoadControl()
                );
                videoPlayView.setPlayer(player);
                player.prepare(
                        new ExtractorMediaSource
                                .Factory(createOkHttpDataSourceFactory())
                                .createMediaSource(Uri.parse(mediaEntity.getOriginalUrl()))
                );

                break;

            case "gif":
                videoPlayView = view.findViewById(R.id.fragment_image_pager_video);
                videoPlayView.setVisibility(View.VISIBLE);
                videoPlayView.setControllerShowTimeoutMs(1000);
                videoPlayView.setControllerVisibilityListener(visibility -> {
                    if (visibility != View.VISIBLE){
                        hideSystemUI();
                    } else {
                        showSystemUI();
                    }
                });

                setSystemUIVisibilityListener(visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0){
                        showActionbar();

                        videoPlayView.showController();
                    }
                });

                trackSelector = new DefaultTrackSelector();

                player = ExoPlayerFactory.newSimpleInstance(
                        context,
                        new AudioAndVideoRenderer(context),
                        trackSelector,
                        new DefaultLoadControl()
                );
                videoPlayView.setPlayer(player);
                player.prepare(
                        new LoopingMediaSource(
                                new ExtractorMediaSource.Factory(createOkHttpDataSourceFactory())
                                        .createMediaSource(
                                                Uri.parse(mediaEntity.getOriginalUrl())
                                        )
                        )
                );

                break;

            case "photo":
            default:
                setSystemUIVisibilityListener(visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0){
                        showActionbar();
                    }
                });
                imageView= view.findViewById(R.id.fragment_image_pager_image);
                imageView.setVisibility(View.VISIBLE);
                imageView.setOnClickListener(v -> {
                    if (isShowingSystemUI()){
                        hideSystemUI();
                    } else {
                        showSystemUI();
                    }
                });
                imageView.setOnScaleChangeListener((float scaleFactor, float focusX, float focusY) -> view.setDragEnabled(scaleFactor <= 1F));
                GlideRequests requests = GlideApp.with(this);
                String url = mediaEntity.getOriginalUrl();
                requests
                        .load(TwitterStringUtils.convertLargeImageUrl(url))
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .thumbnail(requests.load(
                                GlobalApplication.preferenceRepository.getString(GlobalApplication.KEY_TIMELINE_IMAGE_LOAD_MODE, "normal").equals("normal")?
                                        TwitterStringUtils.convertSmallImageUrl(url):
                                        TwitterStringUtils.convertThumbImageUrl(url)
                        ))
                        .into(imageView);
                break;
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onStop() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    contentDownload();
                } else {
                    Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_show_image_toolbar, menu);
        menu.findItem(R.id.action_change_selection).setVisible(trackSelector != null);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_selection:
                if (trackSelector.getCurrentMappedTrackInfo() != null) {
                    TrackSelectionView.getDialog(
                            getActivity(),
                            getString(R.string.action_change_quality),
                            trackSelector,
                            0
                    ).first.show();
                } else {
                    Toast.makeText(
                            getContext(),
                            R.string.use_only_movie_loaded,
                            Toast.LENGTH_SHORT
                    ).show();
                }
                return true;
            case R.id.action_download:
                if (getActivity() != null) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_STORAGE);
                    } else {
                        contentDownload();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void contentDownload(){
        String downloadVideoUrl = mediaEntity.getDownloadVideoUrl();
        String path = downloadVideoUrl == null?
                TwitterStringUtils.convertOriginalImageUrl(mediaEntity.getOriginalUrl()):
                downloadVideoUrl;

        DownloadManager manager;

        if (getActivity() != null &&
                (manager = (DownloadManager)getActivity().getSystemService(DOWNLOAD_SERVICE)) != null){
            Uri uri = Uri.parse(path);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            String lastPathSegment = uri.getLastPathSegment();
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/" + getString(R.string.app_name) + "/" + lastPathSegment
            );
            request.setTitle(lastPathSegment);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            manager.enqueue(request);
        }
    }

    private OkHttpDataSourceFactory createOkHttpDataSourceFactory(){
        return new OkHttpDataSourceFactory(
                GlobalApplication.getOkHttpClient(),
                null
        );
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

    private void showActionbar() {
        ActionBar actionBar;
        if (getActivity() != null && (actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar()) != null) {
            actionBar.show();
        }
    }

    private void setSystemUIVisibilityListener(View.OnSystemUiVisibilityChangeListener l){
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(l);
        }
    }

    private void hideSystemUI() {
        if (getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    private void showSystemUI() {
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private boolean isShowingSystemUI() {
        return getActivity() != null &&
                (getActivity().getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
    }

    public static ImagePagerChildFragment getInstance(Media entity){
        ImagePagerChildFragment fragment=new ImagePagerChildFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(FRAG_MEDIA_ENTITY,entity);
        fragment.setArguments(bundle);
        return fragment;
    }
}
