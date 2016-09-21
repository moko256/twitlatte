package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by moko256 on 2016/03/11.
 *
 * @author moko256
 */
public class ShowUserActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.show_user_fragment_container, new ShowUserFragment())
                .commit();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

}
