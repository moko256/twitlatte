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

package com.github.moko256.twicalico.widget

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.ViewGroup

/**
 * Created by moko256 on 2018/02/25.
 *
 * @author moko256
 */

abstract class FragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private lateinit var viewGroup: ViewGroup

    open fun initAdapter(viewPager: ViewPager) {
        viewGroup = viewPager
        viewPager.adapter = this
    }

    fun getFragment(position: Int): Fragment {
        val item = instantiateItem(viewGroup, position) as Fragment
        finishUpdate(viewGroup)
        return item
    }
}