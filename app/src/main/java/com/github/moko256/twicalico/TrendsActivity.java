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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

public class TrendsActivity extends AppCompatActivity {
    ActionBar actionBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        actionBar=getSupportActionBar();
        actionBar.setTitle(getIntent().getStringExtra("query"));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_search_fragment_container, new TrendsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search_toolbar,menu);

        MenuItem searchMenu=menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) searchMenu.getActionView();

        searchMenu.expandActionView();
        searchView.onActionViewExpanded();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                searchView.clearFocus();
                searchView.setQuery("", false);
                startActivity(SearchResultActivity.getIntent(TrendsActivity.this, searchWord));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(() -> {
            finish();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            actionBar=null;
        }

        @Override
        public boolean onSupportNavigateUp() {
            finish();
            return false;
        }
}
