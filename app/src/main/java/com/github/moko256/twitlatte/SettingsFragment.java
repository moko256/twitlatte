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

package com.github.moko256.twitlatte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.AccountsModel;
import com.github.moko256.twitlatte.text.ThemePreferenceConverterKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_ACCOUNT_KEY;
import static com.github.moko256.twitlatte.repository.PreferenceRepositoryKt.KEY_NIGHT_MODE;

/**
 * Created by moko256 on 2016/03/28.
 *
 * @author moko256
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private int eggCount = 3;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        if (rootKey == null){
            AccountsModel accountsModel = GlobalApplicationKt.getAccountsModel(requireActivity());
            List<AccessToken> accessTokens = accountsModel.getAccessTokens();

            CharSequence[] entries = new CharSequence[accessTokens.size() + 1];
            CharSequence[] entryValues = new CharSequence[accessTokens.size() + 1];

            for (int i = 0; i < accessTokens.size(); i++) {
                AccessToken accessToken = accessTokens.get(i);

                entries[i] = TwitterStringUtils.plusAtMark(accessToken.getScreenName(), accessToken.getUrl());
                entryValues[i] = accessToken.getKeyString();
            }

            entries[entries.length-1]=getString(R.string.login_with_another_account);
            entryValues[entryValues.length-1]="-1";

            ListPreference nowAccountList=(ListPreference) findPreference(KEY_ACCOUNT_KEY);
            nowAccountList.setEntries(entries);
            nowAccountList.setEntryValues(entryValues);
            nowAccountList.setDefaultValue(GlobalApplicationKt.preferenceRepository.getString(KEY_ACCOUNT_KEY,"-1"));
            nowAccountList.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        if (newValue.equals("-1")){
                            startActivity(new Intent(getContext(),OAuthActivity.class));
                            return false;
                        } else {
                            AccessToken accessToken = GlobalApplicationKt.getAccountsModel(requireActivity()).get((String) newValue);

                            ((GlobalApplication) requireActivity().getApplication()).initTwitter(accessToken);
                            startActivity(
                                    new Intent(getContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                            );
                            return true;
                        }
                    }
            );

            findPreference("logout").setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.confirm_logout)
                        .setCancelable(true)
                        .setPositiveButton(R.string.do_logout,
                                (dialog, i) -> {
                                    accountsModel.delete(accountsModel.get(
                                            GlobalApplicationKt.preferenceRepository.getString(KEY_ACCOUNT_KEY,"-1")
                                    ));

                                    int point = accountsModel.size() - 1;
                                    if (point != -1) {
                                        AccessToken accessToken = accountsModel.getAccessTokens().get(point);

                                        GlobalApplicationKt.preferenceRepository.putString(
                                                KEY_ACCOUNT_KEY, accessToken.getKeyString()
                                        );

                                        ((GlobalApplication) requireActivity().getApplication()).initTwitter(accessToken);
                                        startActivity(
                                                new Intent(getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                        );
                                    } else {
                                        GlobalApplicationKt.preferenceRepository.putString(
                                                KEY_ACCOUNT_KEY, "-1"
                                        );

                                        ((GlobalApplication) requireActivity().getApplication()).clearTwitter();
                                        startActivity(
                                                new Intent(getContext(), OAuthActivity.class)
                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                        );
                                    }
                                }
                        )
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            });

            ListPreference nowThemeMode=(ListPreference) findPreference(KEY_NIGHT_MODE);
            nowThemeMode.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        AppCompatDelegate.setDefaultNightMode(
                                ThemePreferenceConverterKt.convertToAppCompatNightThemeMode(String.valueOf(newValue))
                        );
                        return true;
                    }
            );

            Preference licenseThisApp=findPreference("license_at_this_app");
            licenseThisApp.setOnPreferenceClickListener(preference -> {
                requireContext().startActivity(
                        new Intent(getContext(), LicensesActivity.class)
                                .putExtra("title", getResources().getText(R.string.app_name))
                                .putExtra("library_name", "twitlatte")
                );
                return true;
            });

            Preference sourceCodeLink=findPreference("source_code_link");
            sourceCodeLink.setOnPreferenceClickListener(preference -> {
                AppCustomTabsKt.launchChromeCustomTabs(requireContext(), "https://github.com/moko256/twitlatte");
                return true;
            });

            Preference version=findPreference("app_version");
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
                        } catch (PatternSyntaxException e){
                            e.printStackTrace();
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
            PreferenceScreen contributor = getPreferenceScreen();
            for (int i = 0, length = contributor.getPreferenceCount(); i < length; i++) {
                Preference name = contributor.getPreference(i);
                String uri = "https://github.com/" + name.getKey();
                name.setSummary(uri);
                name.setOnPreferenceClickListener(preference -> {
                    AppCustomTabsKt.launchChromeCustomTabs(requireContext(), uri);
                    return true;
                });
            }
        }
    }

}