package com.github.moko256.twicalico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

/**
 * Created by moko256 on 2016/03/27.
 *
 * @author moko256
 */
public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        if (savedInstanceState == null) {
            PreferenceFragmentCompat fragment=new SettingsFragment();

            if (getIntent()!=null){
                String key = getIntent().getStringExtra(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT);
                if (key != null) {
                    Bundle args=new Bundle();
                    args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, key);
                    fragment.setArguments(args);
                }
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_settings_container,fragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        actionBar=null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        startActivity(new Intent(this, SettingsActivity.class).putExtra(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.getKey()));
        return true;
    }
}
