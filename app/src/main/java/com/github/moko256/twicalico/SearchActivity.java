package com.github.moko256.twicalico;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by moko256 on 2017/01/26.
 *
 * @author moko256
 */

public class SearchActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_search_fragment_container,new SearchFragment())
                    .commit();
        }
    }
}
