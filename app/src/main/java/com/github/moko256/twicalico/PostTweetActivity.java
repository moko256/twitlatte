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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.moko256.twicalico.model.base.PostTweetModel;
import com.github.moko256.twicalico.model.impl.PostTweetModelImpl;
import com.github.moko256.twicalico.text.TwitterStringUtils;
import com.twitter.Validator;

import rx.Single;
import rx.subscriptions.CompositeSubscription;
import twitter4j.GeoLocation;

/**
 * Created by moko256 on 2015/11/08.
 *
 * @author moko256
 */
public class PostTweetActivity extends AppCompatActivity {

    private static final String INTENT_EXTRA_IN_REPLY_TO_STATUS_ID = "inReplyToStatusId";
    private static final String INTENT_EXTRA_TWEET_TEXT = "text";
    private static final int REQUEST_GET_IMAGE = 10;

    PostTweetModel model;

    ViewGroup rootViewGroup;

    ActionBar actionBar;
    TextView counterTextView;
    AppCompatEditText editText;
    RecyclerView imagesRecyclerView;
    ImagesAdapter imagesAdapter;
    SwitchCompat possiblySensitiveSwitch;
    SwitchCompat addLocationSwitch;
    TextView locationText;
    AppCompatButton button;

    CompositeSubscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_tweet);

        model = new PostTweetModelImpl(GlobalApplication.twitter, getContentResolver());
        subscription = new CompositeSubscription();

        if (savedInstanceState != null){
            model.setInReplyToStatusId(savedInstanceState.getLong(INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, -1));
        }

        rootViewGroup= findViewById(R.id.activity_tweet_send_layout_root);

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        counterTextView= findViewById(R.id.tweet_text_edit_counter);

        editText= findViewById(R.id.tweet_text_edit);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                model.setTweetText(s.toString());

                counterTextView.setText(String.valueOf(model.getTweetLength())+" / "+String.valueOf(Validator.MAX_TWEET_LENGTH));
                counterTextView.setTextColor(model.isValidTweet()? Color.GRAY: Color.RED);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (getIntent()!=null){
            if (!model.isReply()){
                model.setInReplyToStatusId(getIntent().getLongExtra(
                        INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, -1
                ));
            }

            String text=getIntent().getStringExtra(INTENT_EXTRA_TWEET_TEXT);
            if (text!=null) {
                editText.setText(text);
                editText.setSelection(text.length());
            } else {
                editText.setText("");
            }
        }

        editText.setHint(model.isReply()? R.string.reply: R.string.tweet);

        imagesRecyclerView = findViewById(R.id.activity_tweet_send_images_recycler_view);
        imagesAdapter = new ImagesAdapter(this);
        model.setUriList(imagesAdapter.getImagesList());

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        imagesAdapter.setLimit(4);
        imagesAdapter.setOnAddButtonClickListener(v -> {
            Intent intent;
            if (Build.VERSION.SDK_INT < 19){
                intent = new Intent(Intent.ACTION_GET_CONTENT);
            } else {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
            }
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_GET_IMAGE);
        });
        imagesRecyclerView.setAdapter(imagesAdapter);
        imagesAdapter.notifyDataSetChanged();

        possiblySensitiveSwitch = findViewById(R.id.activity_tweet_possibly_sensitive_switch);
        possiblySensitiveSwitch.setEnabled(imagesAdapter.getImagesList().size() > 0);
        possiblySensitiveSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> model.setPossiblySensitive(isChecked)
        );

        addLocationSwitch = findViewById(R.id.activity_tweet_location_switch);
        addLocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED){
                    subscription.add(
                            getLocation().subscribe(
                                    it -> {
                                        model.setLocation(new GeoLocation(it.getLatitude(), it.getLongitude()));
                                        locationText.setVisibility(View.VISIBLE);
                                        locationText.setText(it.getLatitude() + " " + it.getLongitude());
                                    },
                                    Throwable::printStackTrace
                            )
                    );
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 400);
                }
            } else {
                locationText.setVisibility(View.GONE);
            }
        });

        locationText = findViewById(R.id.activity_tweet_location_result);
        locationText.setVisibility(View.GONE);

        button= findViewById(R.id.tweet_text_submit);
        button.setOnClickListener(v -> {
            v.setEnabled(false);
            model.postTweet()
                    .subscribe(
                            it -> this.finish(),
                            e->{
                                e.printStackTrace();
                                v.setEnabled(true);
                                Snackbar.make(rootViewGroup, TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE).show();
                            }
                    );
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED){
            subscription.add(
                    getLocation().subscribe(
                            it -> {
                                model.setLocation(new GeoLocation(it.getLatitude(), it.getLongitude()));
                                locationText.setVisibility(View.VISIBLE);
                                locationText.setText(it.getLatitude() + " " + it.getLongitude());
                            },
                            Throwable::printStackTrace
                    )
            );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, model.getInReplyToStatusId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GET_IMAGE){
            if (data != null){
                Uri resultUri = data.getData();
                if (resultUri != null){
                    imagesAdapter.getImagesList().add(resultUri);
                    imagesAdapter.notifyItemInserted(imagesAdapter.getImagesList().size());
                    possiblySensitiveSwitch.setEnabled(true);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscription.unsubscribe();
        subscription = null;
        button = null;
        locationText = null;
        addLocationSwitch = null;
        possiblySensitiveSwitch = null;
        imagesAdapter.clearImages();
        imagesAdapter = null;
        imagesRecyclerView = null;
        editText = null;
        counterTextView = null;
        actionBar = null;
        model = null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    private Single<Location> getLocation(){
        return Single.create(singleSubscriber -> {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setCostAllowed(false);
            try {
                locationManager.requestLocationUpdates(
                        locationManager.getBestProvider(criteria, true),
                        0, 0,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                locationManager.removeUpdates(this);
                                singleSubscriber.onSuccess(location);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {}

                            @Override
                            public void onProviderEnabled(String provider) {}

                            @Override
                            public void onProviderDisabled(String provider) {}
                        }
                );
            } catch (SecurityException e){
                singleSubscriber.onError(e);
            }
        });
    }

    public static Intent getIntent(Context context, String text){
        return new Intent(context,PostTweetActivity.class).putExtra(INTENT_EXTRA_TWEET_TEXT, text);
    }

    public static Intent getIntent(Context context, long inReplyToStatusId, String text){
        return PostTweetActivity.getIntent(context, text).putExtra(INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, inReplyToStatusId);
    }
}
