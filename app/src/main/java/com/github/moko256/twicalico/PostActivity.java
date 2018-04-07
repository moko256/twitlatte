/*
 * Copyright 2018 The twicalico authors
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
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.moko256.twicalico.model.base.PostTweetModel;
import com.github.moko256.twicalico.model.impl.PostTweetModelCreator;
import com.github.moko256.twicalico.text.TwitterStringUtils;
import com.github.moko256.twicalico.widget.ImageKeyboardEditText;

import java.util.ArrayList;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import twitter4j.GeoLocation;

/**
 * Created by moko256 on 2015/11/08.
 *
 * @author moko256
 */
public class PostActivity extends AppCompatActivity {

    private static final String INTENT_EXTRA_IN_REPLY_TO_STATUS_ID = "inReplyToStatusId";
    private static final String INTENT_EXTRA_TWEET_TEXT = "text";
    private static final String INTENT_EXTRA_IMAGE_URI = "imageUri";
    private static final String OUT_STATE_EXTRA_IMAGE_URI_LIST = "image_uri_list";
    private static final int REQUEST_GET_IMAGE = 10;

    PostTweetModel model;

    boolean isPosting = false;

    ViewGroup rootViewGroup;

    Toolbar toolbar;
    ActionBar actionBar;
    ImageView userIcon;
    TextView counterTextView;
    ImageKeyboardEditText editText;
    RecyclerView imagesRecyclerView;
    AddedImagesAdapter addedImagesAdapter;
    CheckBox isPossiblySensitive;
    CheckBox addLocation;
    TextView locationText;

    CompositeSubscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        model = PostTweetModelCreator.getInstance(GlobalApplication.twitter, getContentResolver());
        subscription = new CompositeSubscription();

        rootViewGroup= findViewById(R.id.activity_tweet_send_layout_root);

        toolbar = findViewById(R.id.activity_tweet_send_toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        userIcon = findViewById(R.id.activity_tweet_send_user_icon);
        GlideApp.with(this)
                .load(GlobalApplication.userCache.get(GlobalApplication.userId)
                        .get400x400ProfileImageURLHttps()
                )
                .circleCrop()
                .into(userIcon);

        counterTextView= findViewById(R.id.tweet_text_edit_counter);

        editText= findViewById(R.id.tweet_text_edit);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                model.setTweetText(s.toString());

                counterTextView.setText(String.valueOf(model.getTweetLength())+" / "+String.valueOf(model.getMaxTweetLength()));
                counterTextView.setTextColor(model.isValidTweet()? Color.GRAY: Color.RED);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        editText.setImageAddedListener(imageUri -> {
            if (model.getUriList().size() < model.getUriListSizeLimit()) {
                addedImagesAdapter.getImagesList().add(imageUri);
                model.getUriList().add(imageUri);
                addedImagesAdapter.notifyItemChanged(addedImagesAdapter.getImagesList().size() - 1);
                isPossiblySensitive.setEnabled(true);
                return true;
            } else {
                return false;
            }
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (!isPosting && event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_ENTER){
                isPosting = true;
                model.postTweet()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                it -> PostActivity.this.finish(),
                                e->{
                                    e.printStackTrace();
                                    isPosting = false;
                                    Snackbar.make(rootViewGroup, TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE).show();
                                }
                        );
                return true;
            }
            return false;
        });

        imagesRecyclerView = findViewById(R.id.activity_tweet_send_images_recycler_view);
        addedImagesAdapter = new AddedImagesAdapter(this);

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = Math.round(getResources().getDisplayMetrics().density);
            }
        });

        addedImagesAdapter.setLimit(model.getUriListSizeLimit());
        addedImagesAdapter.setOnAddButtonClickListener(v -> startActivityForResult(
                Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"})
                                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                .setType("*/*"), getString(R.string.add_image)
                ),
                REQUEST_GET_IMAGE
        ));
        addedImagesAdapter.setOnDeleteButtonListener(position -> {
            addedImagesAdapter.getImagesList().remove(position);
            model.getUriList().remove(position);
            addedImagesAdapter.notifyDataSetChanged();
            boolean enabled = model.getUriList().size() > 0;
            isPossiblySensitive.setEnabled(enabled);
            isPossiblySensitive.setChecked(isPossiblySensitive.isChecked() && enabled);
        });
        addedImagesAdapter.setOnImageClickListener(position -> {
            Intent open = new Intent(Intent.ACTION_VIEW)
                    .setData(model.getUriList().get(position))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(open, getString(R.string.open_image)));
        });
        imagesRecyclerView.setAdapter(addedImagesAdapter);

        isPossiblySensitive = findViewById(R.id.activity_tweet_is_possibly_sensitive);
        isPossiblySensitive.setEnabled(addedImagesAdapter.getImagesList().size() > 0);
        isPossiblySensitive.setOnCheckedChangeListener(
                (buttonView, isChecked) -> model.setPossiblySensitive(isChecked)
        );

        addLocation = findViewById(R.id.activity_tweet_add_location);
        addLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED){
                    subscription.add(
                            getLocation().subscribe(
                                    it -> {
                                        model.setLocation(new GeoLocation(it.getLatitude(), it.getLongitude()));
                                        locationText.setVisibility(View.VISIBLE);
                                        locationText.setText(getString(R.string.lat_and_lon, it.getLatitude(), it.getLongitude()));
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

        if (getIntent() != null){
            model.setInReplyToStatusId(getIntent().getLongExtra(
                    INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, -1
            ));

            if (savedInstanceState == null) {
                String text = getIntent().getStringExtra(INTENT_EXTRA_TWEET_TEXT);
                if (text != null) {
                    editText.setText(text);
                    editText.setSelection(text.length());
                } else {
                    editText.setText("");
                }

                ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(INTENT_EXTRA_IMAGE_URI);
                if (uris != null) {
                    addedImagesAdapter.getImagesList().addAll(uris);
                    model.getUriList().addAll(uris);
                    isPossiblySensitive.setEnabled(true);
                }
            }
        }

        editText.setHint(model.isReply()? R.string.reply: R.string.post);
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_post_tweet_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!isPosting && item.getItemId() == R.id.action_send){
            isPosting = true;
            item.setEnabled(false);
            model.postTweet()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            it -> this.finish(),
                            e->{
                                e.printStackTrace();
                                item.setEnabled(true);
                                isPosting = false;
                                Snackbar.make(rootViewGroup, TwitterStringUtils.convertErrorToText(e), Snackbar.LENGTH_INDEFINITE).show();
                            }
                    );
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                                locationText.setText(getString(R.string.lat_and_lon, it.getLatitude(), it.getLongitude()));
                            },
                            Throwable::printStackTrace
                    )
            );
        } else {
            addLocation.setChecked(false);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            Parcelable[] l = savedInstanceState.getParcelableArray(OUT_STATE_EXTRA_IMAGE_URI_LIST);
            if (l != null) {
                for (Parcelable p : l) {
                    Uri uri = (Uri) p;
                    model.getUriList().add(uri);
                    addedImagesAdapter.getImagesList().add(uri);
                }
                isPossiblySensitive.setEnabled(true);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int size = model.getUriList().size();
        if (size > 0){
            Uri[] uris = new Uri[size];
            model.getUriList().toArray(uris);
            outState.putParcelableArray(OUT_STATE_EXTRA_IMAGE_URI_LIST, uris);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GET_IMAGE){
            if (data != null){
                Uri resultUri = data.getData();
                ClipData resultUris = data.getClipData();
                if (resultUri != null) {
                    addedImagesAdapter.getImagesList().add(resultUri);
                    model.getUriList().add(resultUri);
                    addedImagesAdapter.notifyItemChanged(addedImagesAdapter.getImagesList().size() - 1);
                    isPossiblySensitive.setEnabled(true);
                } else if (resultUris != null) {
                    int itemCount = resultUris.getItemCount();

                    for (int i = 0; i < itemCount; i++) {
                        Uri uri = resultUris.getItemAt(i).getUri();
                        addedImagesAdapter.getImagesList().add(uri);
                        model.getUriList().add(uri);
                    }
                    addedImagesAdapter.notifyItemRangeChanged(addedImagesAdapter.getImagesList().size() - itemCount, itemCount);
                    isPossiblySensitive.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void finish() {
        editText.close();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscription.unsubscribe();
        subscription = null;
        locationText = null;
        addLocation = null;
        isPossiblySensitive = null;
        addedImagesAdapter.clearImages();
        addedImagesAdapter = null;
        imagesRecyclerView = null;
        editText = null;
        counterTextView = null;
        userIcon = null;
        actionBar = null;
        toolbar = null;
        model = null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        if (editText.getText().length() > 0) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_discard_post)
                    .setPositiveButton(R.string.do_discard, (dialog, which) -> finish())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return false;
        } else {
            finish();
            return true;
        }
    }

    private Single<Location> getLocation(){
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        final LocationListener[] locationListener = new LocationListener[1];
        Single<Location> single = Single.create(singleSubscriber -> {
            locationListener[0] = new LocationListener() {
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
            };
            try {
                locationManager.requestSingleUpdate(
                        locationManager.getBestProvider(criteria, true), locationListener[0], null
                );
            } catch (SecurityException e){
                singleSubscriber.onError(e);
            }
        });
        single.doOnUnsubscribe(() -> locationManager.removeUpdates(locationListener[0]));
        return single;
    }

    public static Intent getIntent(Context context, String text){
        return new Intent(context,PostActivity.class).putExtra(INTENT_EXTRA_TWEET_TEXT, text);
    }

    public static Intent getIntent(Context context, long inReplyToStatusId, String text){
        return PostActivity.getIntent(context, text).putExtra(INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, inReplyToStatusId);
    }

    public static Intent getIntent(Context context, ArrayList<Uri> imageUri){
        return new Intent(context, PostActivity.class).putExtra(INTENT_EXTRA_IMAGE_URI, imageUri);
    }
}
