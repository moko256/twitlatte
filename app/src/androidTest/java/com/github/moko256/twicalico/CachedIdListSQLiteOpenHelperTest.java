/*
 * Copyright 2016 The twicalico authors
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

package com.github.moko256.twicalico;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;

import java.util.ArrayList;

/**
 * Created by kouta on 2017/06/08.
 *
 * @author moko256
 */

public class CachedIdListSQLiteOpenHelperTest extends ApplicationTestCase<Application> {

    public CachedIdListSQLiteOpenHelperTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        CachedIdListSQLiteOpenHelper helper = new CachedIdListSQLiteOpenHelper(new RenamingDelegatingContext(getContext(), ""), "testDatabase");

        long[] input = new long[]{0,1,2};

        helper.addIds(input);

        ArrayList<Long> result1 = helper.getIds();
        for (int i = 0; i < result1.size() ; i++) {
            assertEquals(result1.get(i), Long.valueOf(input[i]));
        }

        long[] insert = new long[]{100, 101};
        helper.insertIds(1,insert);

        ArrayList<Long> result2 = helper.getIds();
        for (int i = 0; i < insert.length; i++) {
            assertEquals(Long.valueOf(insert[i]), result2.get(i + 1));
        }

        helper.deleteIds(input);
        helper.deleteIds(insert);

        assertEquals(helper.getIds().size(), 0);

        helper.setListViewPosition(100);
        assertEquals(helper.getListViewPosition(), 100);

        helper.setListViewPosition(50);
        assertEquals(helper.getListViewPosition(), 50);

        helper.close();
    }
}
