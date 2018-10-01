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

package com.github.moko256.twitlatte.widget

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat


/**
 * Created by moko256 on 2017/12/20.
 *
 * @author moko256
 */
class ImageKeyboardEditText : AppCompatEditText {

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)


    var imageAddedListener: OnImageAddedListener? = null

    private var permitInputContentInfo: InputContentInfoCompat? = null

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(outAttrs)
        EditorInfoCompat.setContentMimeTypes(outAttrs, arrayOf("image/*", "video/*"))
        return InputConnectionCompat.createWrapper(ic, outAttrs) { inputContentInfo, flags, _ ->
            if (Build.VERSION.SDK_INT >= 25 && flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0) {
                try {
                    inputContentInfo.requestPermission()
                    permitInputContentInfo = inputContentInfo
                } catch (e: Exception) {
                    return@createWrapper false
                }
            }
            val result = imageAddedListener?.onAdded(inputContentInfo.contentUri)
            if (inputContentInfo.linkUri != null && result == true) {
                text?.append(" " + inputContentInfo.linkUri?.toString())
            }
            true
        }
    }

    fun close(){
        permitInputContentInfo?.releasePermission()
        imageAddedListener = null
    }

    interface OnImageAddedListener{
        fun onAdded(imageUri: Uri): Boolean
    }
}