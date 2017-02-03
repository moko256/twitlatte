package com.github.moko256.twicalico;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.twitter.Validator;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2015/11/08.
 *
 * @author moko256
 */
public class SendTweetActivity extends AppCompatActivity {

    Validator twitterTextValidator;

    ActionBar actionBar;
    TextView counterTextView;
    AppCompatEditText editText;
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

        button=(AppCompatButton)findViewById(R.id.tweet_text_submit);
        button.setOnClickListener(v -> {
            v.setEnabled(false);
            updateStatusObservable(new StatusUpdate(editText.getText().toString()))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            it->{},
                            Throwable::printStackTrace,
                            this::finish
                    );
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        button=null;
        editText=null;
        counterTextView=null;
        actionBar=null;

        twitterTextValidator=null;
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

    private Observable<Status> updateStatusObservable(StatusUpdate statusUpdate){
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(GlobalApplication.twitter.updateStatus(statusUpdate));
                subscriber.onCompleted();
            } catch (TwitterException e) {
                subscriber.onError(e);
            }
        });
    }

}
