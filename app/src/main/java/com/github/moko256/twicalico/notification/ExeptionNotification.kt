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

package com.github.moko256.twicalico.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.github.moko256.twicalico.R
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/**
 * Created by moko256 on 2017/12/29.
 *
 * @author moko256
 */
class ExceptionNotification {
    fun create (e: Throwable, applicationContext: Context){
        e.printStackTrace()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        e.printStackTrace(printWriter)
        printWriter.flush()
        stringWriter.flush()
        printWriter.close()
        stringWriter.close()
        val inboxStyle = NotificationCompat.InboxStyle(
                NotificationCompat.Builder(applicationContext, "crash_log")
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle("Error : " + e.toString())
                        .setContentText(e.toString())
                        .setWhen(Date().time)
                        .setShowWhen(true)
                        .setColorized(true)
                        .setColor(Color.RED)
                        .setContentIntent(PendingIntent.getActivity(
                                applicationContext,
                                401,
                                Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, stringWriter.toString()),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )))
                .setBigContentTitle("Error : " + e.toString())
                .setSummaryText(applicationContext.getString(R.string.error_occurred))
        val lines = stringWriter.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in lines) {
            inboxStyle.addLine(s)
        }
        NotificationManagerCompat.from(applicationContext).notify(NotificationManagerCompat.IMPORTANCE_HIGH, inboxStyle.build())
    }
}