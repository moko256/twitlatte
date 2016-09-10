package com.moko256.twitterviewer256;

import android.support.annotation.MenuRes;
import android.view.MenuItem;

/**
 * Created by moko256 on 2016/09/08.
 *
 * @author moko256
 */
public interface HasMenuInterface {
    @MenuRes
    int getMenuResourceId();

    boolean onItemSelected(MenuItem item);

}
