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

package com.github.moko256.twitlatte

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by moko256 on 2016/11/14.
 *
 * @author moko256
 */

class LicensesActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        val actionBar = supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_white_24dp)
        }

        val licenseTextView = findViewById<TextView>(R.id.license_text)

        if (intent != null) {
            val title = intent.getStringExtra("title")
            if (title != null) {
                actionBar.title = title
            }

            val libName = intent.getStringExtra("library_name")
            if (libName != null) {
                val path: String = when (libName) {
                    "support_v4",
                    "support_v7",
                    "support_design",
                    "support_custom_tabs",
                    "support_arch" -> {
                        "licenses/android_support.txt"
                    }

                    else -> {
                        "licenses/$libName.txt"
                    }
                }

                val builder = StringBuilder()

                assets.open(path)
                        .reader()
                        .useLines { sequence ->
                            sequence.forEach {
                                builder.append(it).append("\n")
                            }
                        }

                licenseTextView.text = builder
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}