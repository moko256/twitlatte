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

import android.location.Geocoder
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.moko256.twitlatte.database.CachedTrendsSQLiteOpenHelper
import com.github.moko256.twitlatte.viewmodel.TrendsViewModel
import com.github.moko256.twitlatte.widget.MaterialListTopMarginDecoration

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

class TrendsFragment : BaseListFragment() {
    private lateinit var adapter: TrendsAdapter

    private lateinit var viewModel: TrendsViewModel

    private lateinit var helper: CachedTrendsSQLiteOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(TrendsViewModel::class.java)

        val client = requireActivity().getClient()
        helper = CachedTrendsSQLiteOpenHelper(
                requireContext().applicationContext,
                client!!.accessToken
        )
        viewModel.database = helper
        viewModel.apiClient = client.apiClient
        viewModel.geocoder = Geocoder(context)

        viewModel.trends.observe(this, Observer {
            adapter.data = it
            adapter.notifyDataSetChanged()
            setRefreshing(false)
        })
        viewModel.errors.observe(this, Observer {
            notifyErrorBySnackBar(it).show()
            setRefreshing(false)
        })
        viewModel.load(withoutCache = false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        recyclerView.addItemDecoration(MaterialListTopMarginDecoration(resources))

        adapter = TrendsAdapter(requireContext())
        recyclerView.adapter = adapter

        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        recyclerView.swapAdapter(null, true)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        helper.close()
    }

    override fun onInitializeList() {}

    override fun onUpdateList() {
        setRefreshing(true)
        viewModel.load(withoutCache = true)
    }

    override fun onLoadMoreList() {}

    override fun isInitializedList(): Boolean {
        return viewModel.trends.value?.isEmpty() == false
    }

}