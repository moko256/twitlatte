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

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.moko256.twitlatte.widget.FragmentPagerAdapter

/**
 * Created by moko256 on 2016/05/28.
 *
 * @author moko256
 */
internal class FollowFollowerTabsPagerAdapter(
        fm: FragmentManager,
        private val context: Context?,
        private val userId: Long
): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> UserFollowsFragment.newInstance(userId)
            1 -> UserFollowersFragment.newInstance(userId)
            else -> null
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val fragment = getFragment(position)
        return if (fragment is ToolbarTitleInterface) {
            context!!.getString(fragment.titleResourceId)
        } else {
            null
        }
    }
}