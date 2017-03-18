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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final String INTENT_EXTRA_TWEET_TEXT="text";
    private static final int REQUEST_GET_IMAGE = 10;

    Validator twitterTextValidator;

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

        twitterTextValidator=new Validator();

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        counterTextView=(TextView)findViewById(R.id.tweet_text_edit_counter);

        editText=(AppCompatEditText)findViewById(R.id.tweet_text_edit);
        if (getIntent()!=null){
            String text=getIntent().getStringExtra(INTENT_EXTRA_TWEET_TEXT);
            if (text!=null){
                Uri data = getIntent().getData();
                if (data != null){
                    String scheme = data.getScheme();
                    if (scheme.equals("https")){
                        String path = data.getPath();
                        if (path.matches(".*status=.+")){
                            text=Pattern.compile("(?<=(.*status=)).+").matcher(path).group();
                        }
                    }
                }
            }
            if (text!=null) {
                editText.setText(text);
                editText.setSelection(text.length());
            }
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onEditTextChanged(s,counterTextView);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        onEditTextChanged(editText.getText(),counterTextView);

        imagesRecyclerView = (RecyclerView) findViewById(R.id.activity_tweet_send_images_recycler_view);
        imagesAdapter = new ImagesAdapter(this);

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
            updateStatusObservable(new StatusUpdate(editText.getText().toString()), imagesAdapter.getImagesList())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            it->{},
                            e->{
                                e.printStackTrace();
                                v.setEnabled(true);
                                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                            },
                            this::finish
                    );
        });

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

        twitterTextValidator = null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    private void onEditTextChanged(CharSequence s,TextView counterTextView){
        int textLength=twitterTextValidator.getTweetLength(s.toString());
        int maxLength=Validator.MAX_TWEET_LENGTH;
        counterTextView.setText(String.valueOf(textLength)+" / "+String.valueOf(maxLength));
        if (textLength>=maxLength){
            counterTextView.setTextColor(Color.RED);
        }
        else{
            counterTextView.setTextColor(Color.GRAY);
        }
    }

    private Observable<Status> updateStatusObservable(StatusUpdate statusUpdate, List<Uri> images){
        return Observable.create(subscriber -> {
            try {
                Twitter twitter = GlobalApplication.twitter;
                if (images != null && images.size() > 0) {
                    long ids[] = new long[images.size()];
                    for (int i = 0; i < images.size(); i++) {
                        Uri uri = images.get(i);
                        InputStream image = getContentResolver().openInputStream(uri);
                        ids[i] = twitter.uploadMedia(uri.getLastPathSegment(), image).getMediaId();
                    }
                    statusUpdate.setMediaIds(ids);
                }
                subscriber.onNext(twitter.updateStatus(statusUpdate));
                subscriber.onCompleted();
            } catch (FileNotFoundException | TwitterException e){
                subscriber.onError(e);
            }
        });
    }

    public static Intent getIntent(Context context, String text){
        return new Intent(context,SendTweetActivity.class).putExtra(INTENT_EXTRA_TWEET_TEXT, text);
    }
}
