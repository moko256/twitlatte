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

package com.github.moko256.twitlatte.view

import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AlertDialog
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject

fun createEditTextsDialog(
        context: Context,
        title: String?,
        cancellable: Boolean,
        vararg contents: DialogContent
): Completable {

    val result = CompletableSubject.create()

    val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setPositiveButton(android.R.string.ok) { _, _ -> result.onComplete() }
            .apply {
                if (cancellable) {
                    setNegativeButton(android.R.string.cancel, null)
                }
                setCancelable(cancellable)
            }
            .create()

    val views: List<EditText> = contents
            .map {
                val editText = EditText(context)
                editText.hint = it.hint
                editText.inputType = it.type
                editText.setText(it.initValue)
                editText.setSelection(it.initValue.length)

                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        it.callback(s.toString())
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
                editText
            }

    dialog.setView(
            views.singleOrNull() ?: LinearLayout(context).apply {
                orientation = VERTICAL
                views.forEach { addView(it) } }
    )

    dialog.show()

    return result.doOnDispose {
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }
}

class DialogContent(
        val hint: String,
        val initValue: String,
        val type: Int,
        val callback: (String) -> Unit
)