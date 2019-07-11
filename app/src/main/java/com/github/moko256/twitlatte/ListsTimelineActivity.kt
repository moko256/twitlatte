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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.moko256.latte.client.base.entity.ListEntry

/**
 * Created by moko256 on 2019/01/02.
 *
 * @author moko256
 */

class ListsTimelineActivity : AppCompatActivity(), BaseListFragment.GetViewForSnackBar {

    private var listId = -1L

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent

        listId = intent?.getLongExtra("listId", -1) ?: -1

        supportActionBar?.let {
            it.title = intent?.getCharSequenceExtra("title") ?: ""
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp)
        }

        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, ListsTimelineFragment().apply { arguments = intent.extras })
                .commit()
    }

    override fun getViewForSnackBar(): View {
        return findViewById(android.R.id.content)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        fun getIntent(context: Context, entry: ListEntry): Intent {
            return Intent(context, ListsTimelineActivity::class.java)
                    .apply {
                        putExtra("listId", entry.listId)
                        putExtra("title", entry.title)
                    }
        }
    }
}