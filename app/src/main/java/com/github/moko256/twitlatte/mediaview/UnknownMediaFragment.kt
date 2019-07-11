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

package com.github.moko256.twitlatte.mediaview

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import androidx.appcompat.widget.AppCompatTextView
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.intent.launchChromeCustomTabs

/**
 * Created by moko256 on 2018/10/07.
 *
 * @author moko256
 */
class UnknownMediaFragment : AbstractMediaFragment() {

    private lateinit var textView: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSystemUIVisibilityListener { visibility ->
            if (visibility and SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                showActionbar()
            }
        }

        textView = view.findViewById(R.id.fragment_unknown_image_link)
        textView.text = media.originalUrl
        textView.paintFlags = textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG
        textView.setOnClickListener {
            launchChromeCustomTabs(requireContext(), media.originalUrl, false)
        }
    }

    override fun returnLayoutId() = R.layout.fragment_unknown_link_open
    override fun returnMenuId() = R.menu.fragment_image_toolbar

}