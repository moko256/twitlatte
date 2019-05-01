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
import androidx.appcompat.app.AppCompatActivity
import com.github.moko256.latte.client.base.CLIENT_TYPE_NOTHING
import com.github.moko256.latte.client.base.entity.Media
import com.github.moko256.twitlatte.widget.ViewPager
import java.io.Serializable
import java.util.*

/**
 * Created by moko256 on 2016/06/26.
 *
 * @author moko256
 */
@Suppress("UNCHECKED_CAST")
class ShowMediasActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)

        val mediaEntities = intent.getSerializableExtra(FRAG_MEDIA_ENTITIES) as List<Media>

        val position = intent.getIntExtra(FRAG_POSITION, 0)

        supportActionBar?.let {
            it.title = ""
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp)
        }

        findViewById<ViewPager>(R.id.activity_show_image_view_pager)?.let {
            it.adapter = MediasAdapter(
                    supportFragmentManager,
                    mediaEntities,
                    intent.getIntExtra(FRAG_CLIENT_TYPE, CLIENT_TYPE_NOTHING)
            )
            it.currentItem = position
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val FRAG_MEDIA_ENTITIES = "MediaEntities"
        private const val FRAG_CLIENT_TYPE = "client_type"
        private const val FRAG_POSITION = "position"

        fun getIntent(context: Context, entities: Array<Media>, clientType: Int, position: Int): Intent {
            return Intent(context, ShowMediasActivity::class.java)
                    .putExtra(FRAG_MEDIA_ENTITIES, Arrays.asList(*entities) as Serializable)
                    .putExtra(FRAG_CLIENT_TYPE, clientType)
                    .putExtra(FRAG_POSITION, position)
        }
    }
}
