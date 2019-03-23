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

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

/**
 * Created by moko256 on 2016/03/27.
 *
 * @author moko256
 */
class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp)

        if (savedInstanceState == null) {
            val fragment = SettingsFragment()

            intent?.also { intent ->
                intent.getStringExtra("title")?.let {
                    actionBar.title = it
                }

                intent.getStringExtra(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT)?.let {
                    val args = Bundle()
                    args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, it)
                    fragment.arguments = args
                }
            }

            supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
        startActivity(
                Intent(this, SettingsActivity::class.java)
                        .putExtra("title", pref.title)
                        .putExtra(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.key)
        )
        return true
    }
}
