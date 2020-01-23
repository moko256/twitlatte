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

package com.github.moko256.twitlatte

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.github.moko256.latte.client.twitter.CLIENT_TYPE_TWITTER

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

class TrendsActivity : AppCompatActivity(), BaseListFragment.GetViewForSnackBar {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.let {
            it.title = intent.getStringExtra("query")
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp)
        }

        if (savedInstanceState == null && requireClient().accessToken.clientType == CLIENT_TYPE_TWITTER) {
            supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, TrendsFragment())
                    .commit()
        }
    }

    override fun getViewForSnackBar(): View {
        return findViewById(android.R.id.content)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_search_toolbar, menu)

        val searchMenu = menu.findItem(R.id.action_search)
        val searchView = searchMenu.actionView as SearchView

        searchMenu.expandActionView()
        searchView.onActionViewExpanded()
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchWord: String): Boolean {
                searchView.clearFocus()
                searchView.setQuery("", false)
                startActivity(SearchResultActivity.getIntent(this@TrendsActivity, searchWord))
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchView.setOnCloseListener {
            finish()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}