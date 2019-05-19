/*
 * Copyright 2015-2019 The twitlatte authors
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
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.moko256.latte.client.base.entity.Emoji;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.model.base.PostStatusModel;
import com.github.moko256.twitlatte.model.impl.PostStatusModelImpl;
import com.github.moko256.twitlatte.rx.LocationSingleBuilder;
import com.github.moko256.twitlatte.rx.VerifyCredentialOnSubscribe;
import com.github.moko256.twitlatte.text.NoSpanInputFilterKt;
import com.github.moko256.twitlatte.view.DpToPxKt;
import com.github.moko256.twitlatte.widget.ImageKeyboardEditText;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.collections.ArraysKt;

import static com.github.moko256.latte.client.mastodon.MastodonApiClientImplKt.CLIENT_TYPE_MASTODON;
import static com.github.moko256.latte.client.twitter.TwitterApiClientImplKt.CLIENT_TYPE_TWITTER;

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
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 400;
    private static final String[] POST_VISIBILITY = {"Public", "Unlisted", "Private", "Direct"};
    private static final long[] POLL_EXPIRED_AT = {
            5 * 60,
            30 * 60,
            60 * 60,
            6 * 60 * 60,
            24 * 60 * 60,
            3 * 24 * 60 * 60,
            7 * 24 * 60 * 60
    };

    private Client client;
    private PostStatusModel model;

    private boolean isPosting = false;

    private MenuItem postButton;

    private ViewGroup rootViewGroup;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ImageView userIcon;
    private TextView counterTextView;
    private ImageKeyboardEditText editText;
    private EditText contentWarningText;
    private CheckBox contentWarningEnabled;
    private RecyclerView imagesRecyclerView;
    private AddedImagesAdapter addedImagesAdapter;
    private RecyclerView emojiInputRecyclerView;
    private EmojiAdapter emojiAdapter;
    private List<Emoji> emojiList = new ArrayList<>();
    private CheckBox isPossiblySensitive;
    private Spinner postVisibility;
    private CheckBox addLocation;
    private TextView locationText;
    private CheckBox addPoll;
    private EditText[] pollsText;
    private Spinner pollsExpiredAt;
    private TextView pollsExpireAtLabel;
    private CheckBox pollsMultiple;
    private CheckBox pollsHideTotals;

    private int COLOR_STABLE;
    private int COLOR_ERROR;

    private CompositeDisposable disposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        TypedValue value = new TypedValue();

        getTheme().resolveAttribute(android.R.attr.textColorTertiary, value, true);
        COLOR_STABLE = ContextCompat.getColor(this, value.resourceId);

        getTheme().resolveAttribute(R.attr.colorError, value, true);
        COLOR_ERROR = ContextCompat.getColor(this, value.resourceId);

        client = GlobalApplicationKt.getClient(this);
        model = new PostStatusModelImpl(
                getContentResolver(),
                client.getApiClient()
        );
        disposable = new CompositeDisposable();

        rootViewGroup= findViewById(R.id.activity_tweet_send_layout_root);

        toolbar = findViewById(R.id.activity_tweet_send_toolbar);
        setSupportActionBar(toolbar);

        actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        userIcon = findViewById(R.id.activity_tweet_send_user_icon);

        disposable.add(
                Single.create(
                        new VerifyCredentialOnSubscribe(client))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> Glide.with(this)
                                        .load(client.getMediaUrlConverter().convertProfileIconLargeUrl(user))
                                        .circleCrop()
                                        .into(userIcon),
                                Throwable::printStackTrace
                        )
        );

        counterTextView= findViewById(R.id.tweet_text_edit_counter);

        editText= findViewById(R.id.tweet_text_edit);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                model.getUpdateStatus().setContext(s.toString());
                updateCounter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        editText.setFilters(NoSpanInputFilterKt.getNoSpanInputFilter());
        editText.setImageAddedListener(imageUri -> {
            if (model.getUriList().size() < model.getUriListSizeLimit()) {
                addedImagesAdapter.addImageAndUpdateView(imageUri);
                model.getUriList().add(imageUri);
                updateCounter();
                isPossiblySensitive.setEnabled(true);
                return true;
            } else {
                return false;
            }
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (!isPosting && event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_ENTER){
                doSend();
                return true;
            }
            return false;
        });

        imagesRecyclerView = findViewById(R.id.activity_tweet_send_images_recycler_view);
        addedImagesAdapter = new AddedImagesAdapter(this);

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(
                    @NonNull Rect outRect,
                    @NonNull View view,
                    @NonNull RecyclerView parent,
                    @NonNull RecyclerView.State state
            ) {
                outRect.right = DpToPxKt.oneDpToPx(getResources().getDisplayMetrics().density);
            }
        });

        addedImagesAdapter.limit = model.getUriListSizeLimit();
        addedImagesAdapter.onAddButtonClickListener = v -> startActivityForResult(
                Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"})
                                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                .setType("*/*"), getString(R.string.add_media)
                ),
                REQUEST_GET_IMAGE
        );
        addedImagesAdapter.onDeleteButtonListener = position -> {
            addedImagesAdapter.removeImageAndUpdateView(position);
            model.getUriList().remove(position);
            boolean enabled = model.getUriList().size() > 0;
            updateCounter();
            isPossiblySensitive.setEnabled(enabled);
            isPossiblySensitive.setChecked(isPossiblySensitive.isChecked() && enabled);
        };
        addedImagesAdapter.onImageClickListener = position -> {
            try {
                Intent open = new Intent(Intent.ACTION_VIEW)
                        .setData(model.getUriList().get(position))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(open, getString(R.string.open_media)));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        };
        imagesRecyclerView.setAdapter(addedImagesAdapter);

        emojiInputRecyclerView = findViewById(R.id.suggestions_of_emoji);

        isPossiblySensitive = findViewById(R.id.activity_tweet_is_possibly_sensitive);
        isPossiblySensitive.setEnabled(addedImagesAdapter.getImagesList().size() > 0);
        isPossiblySensitive.setOnCheckedChangeListener(
                (buttonView, isChecked) -> model.getUpdateStatus().setPossiblySensitive(isChecked)
        );

        postVisibility = findViewById(R.id.activity_tweet_visibility_spinner);
        contentWarningText = findViewById(R.id.tweet_text_warning);
        contentWarningEnabled = findViewById(R.id.activity_tweet_add_content_warning);
        pollsText = new EditText[]{
                findViewById(R.id.edit_poll_0),
                findViewById(R.id.edit_poll_1),
                findViewById(R.id.edit_poll_2),
                findViewById(R.id.edit_poll_3)
        };
        pollsExpiredAt = findViewById(R.id.edit_poll_expired_at);
        pollsExpireAtLabel = findViewById(R.id.edit_poll_expired_at_description);
        pollsMultiple = findViewById(R.id.poll_multiple);
        pollsHideTotals = findViewById(R.id.hide_totals);
        addPoll = findViewById(R.id.activity_add_poll);

        ArraysKt.forEach(pollsText, editText -> {
            editText.setVisibility(View.GONE);
            return Unit.INSTANCE;
        });
        pollsExpiredAt.setVisibility(View.GONE);
        pollsMultiple.setVisibility(View.GONE);
        pollsHideTotals.setVisibility(View.GONE);
        pollsExpireAtLabel.setVisibility(View.GONE);

        if (client.getAccessToken().getClientType() == CLIENT_TYPE_MASTODON) {
            emojiAdapter = new EmojiAdapter(
                    emojiList,
                    this,
                    Glide.with(this),
                    emoji -> {
                        int selectionEnd = editText.getSelectionEnd();
                        String shortCode = emoji.getShortCode();
                        editText.getText()
                                .insert(selectionEnd, ":")
                                .insert(selectionEnd + 1, shortCode)
                                .insert(selectionEnd + 1 + shortCode.length(), ": ");
                        editText.setSelection(selectionEnd + shortCode.length() + 3);
                        return Unit.INSTANCE;
                    },
                    () -> {
                        disposable.add(
                                model.requestCustomEmojis()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                emojis -> {
                                                    emojiList.clear();
                                                    emojiList.addAll(emojis);
                                                    emojiAdapter.notifyDataSetChanged();
                                                },
                                                this::errorNotify
                                        )
                        );
                        return Unit.INSTANCE;
                    }
            );
            contentWarningText.setFilters(NoSpanInputFilterKt.getNoSpanInputFilter());
            emojiInputRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            emojiInputRecyclerView.setAdapter(emojiAdapter);

            contentWarningText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    model.getUpdateStatus().setContentWarning(s.toString());
                    updateCounter();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            contentWarningEnabled.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (isChecked) {
                            contentWarningText.setVisibility(View.VISIBLE);
                            contentWarningText.requestFocus();
                        } else {
                            contentWarningText.setVisibility(View.GONE);
                            model.getUpdateStatus().setContentWarning("");
                        }
                    }
            );
            postVisibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    model.getUpdateStatus().setVisibility(POST_VISIBILITY[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            ArrayList<String> options = new ArrayList<>(4);
            ArraysKt.forEachIndexed(pollsText, (i, editText) -> {
                if (i < 2) {
                    editText.setHint(getString(R.string.poll_n, i + 1));
                } else {
                    editText.setHint(getString(R.string.poll_n_optional, i + 1));
                }
                options.add("");
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        options.set(i, s.toString());
                        model.getUpdateStatus().setPollList(options);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                return Unit.INSTANCE;
            });

            pollsExpiredAt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    model.getUpdateStatus().setPollExpiredSecond(POLL_EXPIRED_AT[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            pollsMultiple.setOnCheckedChangeListener((buttonView, isChecked)
                    -> model.getUpdateStatus().setPollSelectableMultiple(isChecked));
            pollsHideTotals.setOnCheckedChangeListener((buttonView, isChecked)
                    -> model.getUpdateStatus().setPollHideTotalsUntilExpired(isChecked));

            addPoll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    ArraysKt.forEach(pollsText, editText -> {
                        editText.setVisibility(View.VISIBLE);
                        return Unit.INSTANCE;
                    });
                    pollsExpiredAt.setVisibility(View.VISIBLE);
                    pollsMultiple.setVisibility(View.VISIBLE);
                    pollsHideTotals.setVisibility(View.VISIBLE);
                    pollsExpireAtLabel.setVisibility(View.VISIBLE);
                } else {
                    model.getUpdateStatus().setPollList(null);
                    ArraysKt.forEach(pollsText, editText -> {
                        editText.setVisibility(View.GONE);
                        editText.setText("");
                        return Unit.INSTANCE;
                    });
                    pollsExpiredAt.setVisibility(View.GONE);
                    pollsMultiple.setVisibility(View.GONE);
                    pollsHideTotals.setVisibility(View.GONE);
                    pollsExpireAtLabel.setVisibility(View.GONE);
                }
            });
        } else {
            emojiInputRecyclerView.setVisibility(View.GONE);
            contentWarningEnabled.setVisibility(View.GONE);
            postVisibility.setVisibility(View.GONE);
            findViewById(R.id.activity_tweet_visibility_description).setVisibility(View.GONE);
            addPoll.setVisibility(View.GONE);
        }

        addLocation = findViewById(R.id.activity_tweet_add_location);
        if (client.getAccessToken().getClientType() == CLIENT_TYPE_TWITTER) {
            addLocation.setVisibility(View.VISIBLE);
            addLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked){
                    locationText.setVisibility(View.VISIBLE);
                    if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED){
                        updateLocation();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_LOCATION);
                    }
                } else {
                    model.getUpdateStatus().setLocation(null);
                    locationText.setVisibility(View.GONE);
                    locationText.setText("");
                }
            });

        } else {
            addLocation.setVisibility(View.GONE);
        }

        locationText = findViewById(R.id.activity_tweet_location_result);
        locationText.setVisibility(View.GONE);

        if (getIntent() != null){
            model.getUpdateStatus().setInReplyToStatusId(getIntent().getLongExtra(
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
                    updateCounter();
                    isPossiblySensitive.setEnabled(true);
                }
            }
        }

        editText.setHint(model.isReply()? R.string.reply: R.string.post);
    }

    private void doSend() {
        if (model.isValid()) {
            doReallySend();
        } else {
            new AlertDialog
                    .Builder(this)
                    .setMessage(R.string.confirm_send_invalid_post)
                    .setPositiveButton(R.string.post, (dialog, which) -> doReallySend())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    private void doReallySend() {
        isPosting = true;
        postButton.setEnabled(false);
        disposable.add(
                model.post()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::finish,
                                e->{
                                    e.printStackTrace();
                                    postButton.setEnabled(true);
                                    isPosting = false;
                                    errorNotify(e);
                                }
                        )
        );
    }

    @SuppressLint("SetTextI18n")
    private void updateCounter() {
        counterTextView.setText(model.getTweetLength() + " / " + model.getStatusTextLimit());
        counterTextView.setTextColor(
                model.isValid()?
                        COLOR_STABLE:
                        COLOR_ERROR
        );
    }

    private void errorNotify(Throwable e) {
        Snackbar.make(
                rootViewGroup,
                e.getMessage(),
                Snackbar.LENGTH_LONG
        ).show();
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_post_tweet_toolbar, menu);
        postButton = menu.findItem(R.id.action_send);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isPosting && item.getItemId() == R.id.action_send) {
            doSend();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                    updateLocation();
                } else {
                    addLocation.setChecked(false);
                }
            } // else it is prompting permission after activity recreated
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
        if (requestCode == REQUEST_GET_IMAGE){
            if (data != null){
                Uri resultUri = data.getData();
                ClipData resultUris = data.getClipData();
                if (resultUri != null) {
                    addedImagesAdapter.addImageAndUpdateView(resultUri);
                    model.getUriList().add(resultUri);
                    updateCounter();
                    isPossiblySensitive.setEnabled(true);
                } else if (resultUris != null) {
                    int resultSize = resultUris.getItemCount();
                    int limit = model.getUriListSizeLimit() - model.getUriList().size();
                    int itemCount = resultSize <= limit? resultSize: limit;
                    ArrayList<Uri> arrayList = new ArrayList<>(itemCount);

                    for (int i = 0; i < itemCount; i++) {
                        arrayList.add(resultUris.getItemAt(i).getUri());
                    }
                    addedImagesAdapter.addImagesAndUpdateView(arrayList);
                    model.getUriList().addAll(arrayList);
                    updateCounter();
                    isPossiblySensitive.setEnabled(true);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void finish() {
        if (editText != null) {
            editText.close();
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
        disposable = null;
        addPoll = null;
        pollsHideTotals = null;
        pollsMultiple = null;
        pollsExpiredAt = null;
        pollsExpireAtLabel = null;
        pollsText = null;
        locationText = null;
        addLocation = null;
        postVisibility = null;
        isPossiblySensitive = null;
        emojiList = null;
        emojiInputRecyclerView = null;
        emojiAdapter = null;
        addedImagesAdapter.clearImages();
        addedImagesAdapter = null;
        imagesRecyclerView = null;
        contentWarningEnabled = null;
        contentWarningText = null;
        editText = null;
        counterTextView = null;
        userIcon = null;
        actionBar = null;
        toolbar = null;
        postButton = null;
        model = null;
        client = null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        if (editText.getText().length() > 0 || addedImagesAdapter.getImagesList().size() > 0) {
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

    private void updateLocation(){
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        disposable.add(
                new LocationSingleBuilder(Objects.requireNonNull(locationManager))
                        .getSingle()
                        .subscribe(
                                it -> {
                                    model.getUpdateStatus().setLocation(new Pair<>(it.getLatitude(), it.getLongitude()));
                                    locationText.setText(getString(R.string.lat_and_lon, it.getLatitude(), it.getLongitude()));
                                },
                                this::errorNotify
                        )
        );
    }

    public static Intent getIntent(Context context, String text){
        return getIntent(context, -1, text, null);
    }

    public static Intent getIntent(Context context, long inReplyToStatusId, String text){
        return getIntent(context, inReplyToStatusId, text, null);
    }

    public static Intent getIntent(Context context, long inReplyToStatusId, String text, ArrayList<Uri> imageUri){
        Intent intent = new Intent(context, PostActivity.class);
        if (text != null) {
            intent.putExtra(INTENT_EXTRA_TWEET_TEXT, text);
        }
        if (inReplyToStatusId != -1) {
            intent.putExtra(INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, inReplyToStatusId);
        }
        if (imageUri != null) {
            intent.putExtra(INTENT_EXTRA_IMAGE_URI, imageUri);
        }
        return intent;
    }
}
