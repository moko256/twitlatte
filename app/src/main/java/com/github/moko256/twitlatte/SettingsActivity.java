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

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

/**
 * Created by moko256 on 2016/03/27.
 *
 * @author moko256
 */
public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        if (savedInstanceState == null) {
            PreferenceFragmentCompat fragment=new SettingsFragment();

            if (getIntent()!=null){
                String title = getIntent().getStringExtra("title");
                if (title != null){
                    actionBar.setTitle(title);
                }

                String key = getIntent().getStringExtra(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT);
                if (key != null) {
                    Bundle args=new Bundle();
                    args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, key);
                    fragment.setArguments(args);
                }
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        startActivity(
                new Intent(this, SettingsActivity.class)
                        .putExtra("title", pref.getTitle())
                        .putExtra(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.getKey())
        );
        return true;
    }
}
