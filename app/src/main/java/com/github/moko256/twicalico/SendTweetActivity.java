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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.moko256.twicalico.model.SendTweetModel;
import com.twitter.Validator;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2015/11/08.
 *
 * @author moko256
 */
public class SendTweetActivity extends AppCompatActivity {

    private static final String INTENT_EXTRA_IN_REPLY_TO_STATUS_ID = "inReplyToStatusId";
    private static final String INTENT_EXTRA_TWEET_TEXT = "text";
    private static final int REQUEST_GET_IMAGE = 10;

    SendTweetModel model;

    ViewGroup rootViewGroup;

    ActionBar actionBar;
    TextView counterTextView;
    AppCompatEditText editText;
    RecyclerView imagesRecyclerView;
    ImagesAdapter imagesAdapter;
    AppCompatButton button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_tweet);

        model = new SendTweetModel(GlobalApplication.twitter, getContentResolver());

        if (savedInstanceState != null){
            model.setInReplyToStatusId(savedInstanceState.getLong(INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, -1));
        }

        rootViewGroup=(ViewGroup) findViewById(R.id.activity_tweet_send_layout_root);

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        counterTextView=(TextView)findViewById(R.id.tweet_text_edit_counter);

        editText=(AppCompatEditText)findViewById(R.id.tweet_text_edit);
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
            }
        }

        editText.setHint(model.isReply()? R.string.reply: R.string.tweet);

        imagesRecyclerView = (RecyclerView) findViewById(R.id.activity_tweet_send_images_recycler_view);
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

        button=(AppCompatButton)findViewById(R.id.tweet_text_submit);
        button.setOnClickListener(v -> {
            v.setEnabled(false);
            model.postTweet()
                    .subscribe(
                            it->{},
                            e->{
                                e.printStackTrace();
                                v.setEnabled(true);
                                Snackbar.make(rootViewGroup, e.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
                            },
                            this::finish
                    );
        });

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
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        button = null;
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

    public static Intent getIntent(Context context, String text){
        return new Intent(context,SendTweetActivity.class).putExtra(INTENT_EXTRA_TWEET_TEXT, text);
    }

    public static Intent getIntent(Context context, long inReplyToStatusId, String text){
        return SendTweetActivity.getIntent(context, text).putExtra(INTENT_EXTRA_IN_REPLY_TO_STATUS_ID, inReplyToStatusId);
    }
}
