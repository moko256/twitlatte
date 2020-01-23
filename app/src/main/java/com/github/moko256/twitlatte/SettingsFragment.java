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

package com.github.moko256.twitlatte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.ClientModel;
import com.github.moko256.twitlatte.text.ThemePreferenceConverterKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import kotlin.collections.ArraysKt;

import static android.app.Activity.RESULT_OK;
import static com.github.moko256.latte.client.twitter.TwitterApiClientKt.CLIENT_TYPE_TWITTER;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_ACCOUNT_KEY;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_ACCOUNT_KEY_LINK_OPEN;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_NIGHT_MODE;

/**
 * Created by moko256 on 2016/03/28.
 *
 * @author moko256
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_OAUTH_OR_CANCEL = 2;

    private int eggCount = 3;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        if (rootKey == null) {
            FragmentActivity activity = requireActivity();
            ClientModel clientModel = GlobalApplicationKt.getClientsRepository(activity);
            List<AccessToken> accessTokens = clientModel.getAccessTokens();

            CharSequence[] entriesAccountList = new CharSequence[accessTokens.size() + 1];
            CharSequence[] entryValues = new CharSequence[accessTokens.size() + 1];

            for (int i = 0; i < accessTokens.size(); i++) {
                AccessToken accessToken = accessTokens.get(i);

                entriesAccountList[i] = TwitterStringUtils.plusAtMark(accessToken.getScreenName(), accessToken.getUrl());
                entryValues[i] = accessToken.getKeyString();
            }

            entriesAccountList[entriesAccountList.length - 1] = getString(R.string.login_with_another_account);
            entryValues[entryValues.length - 1] = "-1";

            ListPreference nowAccountList = findPreference(KEY_ACCOUNT_KEY);
            nowAccountList.setEntries(entriesAccountList);
            nowAccountList.setEntryValues(entryValues);
            nowAccountList.setDefaultValue(GlobalApplicationKt.preferenceRepository.getString(KEY_ACCOUNT_KEY, "-1"));
            nowAccountList.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        if (newValue.equals("-1")) {
                            startActivityForResult(new Intent(activity, OAuthActivity.class), REQUEST_OAUTH_OR_CANCEL);
                            return false;
                        } else {
                            clientModel.switchCurrentClient(accessTokens.get(ArraysKt.indexOf(entryValues, newValue)));

                            startActivity(
                                    new Intent(activity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                            );
                            return false;
                        }
                    }
            );

            List<AccessToken> accessTokensTwitter = clientModel.getAccessTokensByType(CLIENT_TYPE_TWITTER);

            CharSequence[] entriesLinkOpen = new CharSequence[accessTokensTwitter.size() + 1];
            CharSequence[] entriesLinkOpenValue = new CharSequence[accessTokensTwitter.size() + 1];

            for (int i = 0; i < accessTokensTwitter.size(); i++) {
                AccessToken accessToken = accessTokensTwitter.get(i);

                entriesLinkOpen[i] = TwitterStringUtils.plusAtMark(accessToken.getScreenName(), accessToken.getUrl());
                entriesLinkOpenValue[i] = accessToken.getKeyString();
            }

            entriesLinkOpen[entriesLinkOpen.length - 1] = getString(R.string.not_set);
            entriesLinkOpenValue[entriesLinkOpenValue.length - 1] = "-1";

            ListPreference linkOpenAccountList = findPreference(KEY_ACCOUNT_KEY_LINK_OPEN);
            linkOpenAccountList.setEntries(entriesLinkOpen);
            linkOpenAccountList.setEntryValues(entriesLinkOpenValue);
            linkOpenAccountList.setDefaultValue("-1");

            findPreference("logout").setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.confirm_logout)
                        .setCancelable(true)
                        .setPositiveButton(
                                R.string.do_logout,
                                (dialog, i) -> {
                                    if (clientModel.logoutAndSwitchCurrentClient()) {
                                        startActivity(
                                                new Intent(activity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                        );
                                    } else {
                                        startActivity(new Intent(activity, MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    }
                                }
                        )
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            });

            ListPreference nowThemeMode = findPreference(KEY_NIGHT_MODE);
            nowThemeMode.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        AppCompatDelegate.setDefaultNightMode(
                                ThemePreferenceConverterKt.convertToAppCompatNightThemeMode(String.valueOf(newValue))
                        );
                        ((AppCompatActivity) activity).getDelegate().applyDayNight();
                        return true;
                    }
            );

            Preference licenseThisApp = findPreference("license_at_this_app");
            licenseThisApp.setOnPreferenceClickListener(preference -> {
                startActivity(
                        new Intent(activity, LicensesActivity.class)
                                .putExtra("title", getResources().getText(R.string.app_name))
                                .putExtra("library_name", "twitlatte")
                );
                return true;
            });

            Preference sourceCodeLink = findPreference("source_code_link");
            sourceCodeLink.setOnPreferenceClickListener(preference -> {
                AppCustomTabsKt.launchChromeCustomTabs(activity, "https://github.com/moko256/twitlatte", false);
                return true;
            });

            Preference version = findPreference("app_version");
            version.setSummary(BuildConfig.VERSION_NAME);
            version.setOnPreferenceClickListener(preference -> {
                eggCount--;
                if (eggCount <= 0) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    eggCount = 3;
                }

                return true;
            });
        } else if (rootKey.equals("regexMute")) {
            PreferenceScreen regexMute = getPreferenceScreen();
            for (int i = 0, length = regexMute.getPreferenceCount(); i < length; i++) {
                Preference name = regexMute.getPreference(i);
                if (name instanceof EditTextPreference) {
                    name.setOnPreferenceChangeListener((preference, newValue) -> {
                        try {
                            GlobalApplicationKt.preferenceRepository.updateRegex(preference.getKey(), (String) newValue);
                        } catch (PatternSyntaxException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    });
                }
            }
        } else if (rootKey.equals("license")) {
            PreferenceScreen license = getPreferenceScreen();
            for (int i = 0, length = license.getPreferenceCount(); i < length; i++) {
                license.getPreference(i).setOnPreferenceClickListener(preference -> {
                    startActivity(
                            new Intent(getContext(), LicensesActivity.class)
                                    .putExtra("title", preference.getTitle())
                                    .putExtra("library_name", preference.getKey().substring(12)) // "license_lib_".length
                    );
                    return true;
                });
            }
        } else if (rootKey.equals("contributors")) {
            PreferenceScreen contributorCategory = getPreferenceScreen();
            for (int c = 0, length = contributorCategory.getPreferenceCount(); c < length; c++) {
                PreferenceCategory contributor = (PreferenceCategory) contributorCategory.getPreference(c);
                for (int i = 0, l = contributor.getPreferenceCount(); i < l; i++) {
                    Preference name = contributor.getPreference(i);
                    String uri = "https://github.com/" + name.getKey();
                    name.setSummary(uri);
                    name.setOnPreferenceClickListener(preference -> {
                        AppCustomTabsKt.launchChromeCustomTabs(requireContext(), uri, false);
                        return true;
                    });
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OAUTH_OR_CANCEL) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }
}