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

package com.github.moko256.twitlatte.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import com.github.moko256.latte.client.base.entity.Post
import com.github.moko256.twitlatte.database.CachedIdListSQLiteOpenHelper
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.model.base.ListModel
import com.github.moko256.twitlatte.model.base.StatusActionModel
import com.github.moko256.twitlatte.model.impl.ListModelImpl
import com.github.moko256.twitlatte.model.impl.StatusActionModelImpl
import com.github.moko256.twitlatte.repository.server.base.ListServerRepository

/**
 * Created by moko256 on 2018/07/13.
 *
 * @author moko256
 */
class ListViewModel(
        app: Application,
        client: Client,
        bundle: Bundle,
        repo: ListRepository
) : AndroidViewModel(app) {
    val listModel: ListModel

    init {

        repo.onCreate(client, bundle)

        listModel = ListModelImpl(
                repo,
                client,
                CachedIdListSQLiteOpenHelper(
                        app.applicationContext,
                        client.accessToken,
                        repo.name()
                )
        )
    }

    val statusActionModel: StatusActionModel = StatusActionModelImpl(
            client.apiClient,
            client.postCache
    )

    override fun onCleared() {
        listModel.close()
    }

    abstract class ListRepository : ListServerRepository<Post> {
        protected lateinit var client: Client

        @CallSuper
        open fun onCreate(client: Client, bundle: Bundle) {
            this.client = client
        }

        abstract fun name(): String
    }
}