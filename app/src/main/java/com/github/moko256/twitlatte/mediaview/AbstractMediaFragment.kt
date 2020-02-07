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

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.Context.DOWNLOAD_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.*
import android.view.View.*
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.moko256.latte.client.base.CLIENT_TYPE_NOTHING
import com.github.moko256.latte.client.base.entity.Media
import com.github.moko256.twitlatte.R
import com.github.moko256.twitlatte.text.TwitterStringUtils
import com.github.moko256.twitlatte.widget.FlingToCloseLayout

/**
 * Created by moko256 on 2018/10/07.
 *
 * @author moko256
 */
private const val FRAG_MEDIA_ENTITY = "media_entity"
private const val FRAG_CLIENT_TYPE = "client_type"

private const val REQUEST_CODE_PERMISSION_STORAGE = 1

abstract class AbstractMediaFragment : Fragment() {

    protected lateinit var media: Media

    protected var clientType: Int = CLIENT_TYPE_NOTHING

    fun setMediaToArg(media: Media, clientType: Int) {
        val bundle = Bundle()
        bundle.putSerializable(FRAG_MEDIA_ENTITY, media)
        bundle.putInt(FRAG_CLIENT_TYPE, clientType)

        arguments = bundle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        showSystemUI()
        arguments?.let { arguments ->
            media = arguments.getSerializable(FRAG_MEDIA_ENTITY) as Media
            clientType = arguments.getInt(FRAG_CLIENT_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(returnLayoutId(), container, false) as FlingToCloseLayout

        view.onClose = {
            activity?.finish()
        }
        view.onTouchedListener = {
            if (!isShowingSystemUI()) {
                showSystemUI()
            }
        }

        return view
    }

    protected fun showActionbar() {
        activity?.actionBar?.show()
    }

    protected fun setSystemUIVisibilityListener(l: (Int) -> Unit) {
        activity?.window?.decorView?.setOnSystemUiVisibilityChangeListener(l)
    }

    protected fun hideSystemUI() {
        activity?.window?.let {
            it.addFlags(FLAG_FULLSCREEN)

            it.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_FULLSCREEN
                    or SYSTEM_UI_FLAG_IMMERSIVE)
        }
    }

    protected fun showSystemUI() {
        activity?.window?.let {
            it.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            it.clearFlags(FLAG_FULLSCREEN)
        }
    }

    protected fun isShowingSystemUI(): Boolean {
        return activity?.window?.decorView?.systemUiVisibility?.and(SYSTEM_UI_FLAG_FULLSCREEN) == 0
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (grantResults.firstOrNull() == PERMISSION_GRANTED) {
                startDownload()
            } else {
                Toast.makeText(context, R.string.permission_denied, LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(returnMenuId(), menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_download) {
            context?.let {
                if (ContextCompat.checkSelfPermission(it, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION_STORAGE)
                } else {
                    startDownload()
                }
            }
            true
        } else {
            false
        }
    }

    private fun startDownload() {
        val path = media.downloadVideoUrl
                ?: TwitterStringUtils.convertOriginalImageUrl(clientType, media.originalUrl)

        val manager: DownloadManager = activity?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        val uri = Uri.parse(path)
        val request = DownloadManager.Request(uri)
        val lastPathSegment = uri.lastPathSegment?.split(":")?.first()
        request.setDestinationInExternalPublicDir(
                DIRECTORY_DOWNLOADS,
                "/" + getString(R.string.app_name) + "/" + lastPathSegment
        )
        request.setTitle(lastPathSegment)
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        manager.enqueue(request)
    }


    @LayoutRes
    protected abstract fun returnLayoutId(): Int

    @MenuRes
    protected abstract fun returnMenuId(): Int

}