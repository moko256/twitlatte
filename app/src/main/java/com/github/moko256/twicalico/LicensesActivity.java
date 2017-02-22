package com.github.moko256.twicalico;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by moko256 on 2016/11/14.
 *
 * @author moko256
 */

public class LicensesActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.activity_licenses_list);
        recyclerView.setAdapter(new StringArrayAdapter(this, new String[]{
                "Android Compatibility Library v4",
                "Android Compatibility Library v7",
                "Android Compatibility Library v14",
                "Android Design Support Library",
                "Glide",
                "OkHttp",
                "Twitter4J",
                "twitter-text",
                "RxJava",
                "RxAndroid"
        }));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

}