/*
 * Copyright (C) 2017 Android Open Source Illusion Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aosip.owlsnest.statusbar;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.List;
import java.util.ArrayList;

import com.aosip.owlsnest.preference.CustomSeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class BatteryCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String BATTERY_TILE_STYLE = "battery_tile_style";
    private static final String STATUS_BAR_CHARGE_COLOR = "status_bar_charge_color";
    private static final String FORCE_CHARGE_BATTERY_TEXT = "force_charge_battery_text";
    private static final String TEXT_CHARGING_SYMBOL = "text_charging_symbol";

    private static final int STATUS_BAR_BATTERY_STYLE_PORTRAIT = 0;
    private static final int STATUS_BAR_BATTERY_STYLE_LANDSCAPE = 5;
    private static final int STATUS_BAR_BATTERY_STYLE_CIRLCE = 2;
    private static final int STATUS_BAR_BATTERY_STYLE_BIGCIRCLE = 7;
    private static final int STATUS_BAR_BATTERY_STYLE_SOLID = 8;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;
    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;

    private int mBatteryTileStyleValue;
    private int mStatusBarBatteryValue;
    private int mStatusBarBatteryShowPercentValue;
    private int mTextChargingSymbolValue;

    private ListPreference mBatteryTileStyle;
    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;
    private ListPreference mTextChargingSymbol;
    private SwitchPreference mForceChargeBatteryText;
    private ColorPickerPreference mChargeColor;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aosip_battery);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        mForceChargeBatteryText = (SwitchPreference) findPreference(FORCE_CHARGE_BATTERY_TEXT);
        mForceChargeBatteryText.setChecked((Settings.Secure.getInt(resolver,
            Settings.Secure.FORCE_CHARGE_BATTERY_TEXT, 1) == 1));
        mForceChargeBatteryText.setOnPreferenceChangeListener(this);

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryValue = Settings.Secure.getInt(resolver,
            Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(Integer.toString(mStatusBarBatteryValue));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int chargeColor = Settings.Secure.getInt(resolver,
            Settings.Secure.STATUS_BAR_CHARGE_COLOR, Color.WHITE);
        mChargeColor = (ColorPickerPreference) findPreference("status_bar_charge_color");
        mChargeColor.setNewPreviewColor(chargeColor);
        mChargeColor.setOnPreferenceChangeListener(this);

        mStatusBarBatteryShowPercent =
            (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        mStatusBarBatteryShowPercentValue = Settings.Secure.getInt(resolver,
            Settings.Secure.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(Integer.toString(mStatusBarBatteryShowPercentValue));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

        mTextChargingSymbol = (ListPreference) findPreference(TEXT_CHARGING_SYMBOL);
        mTextChargingSymbolValue = Settings.Secure.getInt(resolver,
            Settings.Secure.TEXT_CHARGING_SYMBOL, 0);
        mTextChargingSymbol.setValue(Integer.toString(mTextChargingSymbolValue));
        mTextChargingSymbol.setSummary(mTextChargingSymbol.getEntry());
        mTextChargingSymbol.setOnPreferenceChangeListener(this);

        mBatteryTileStyle = (ListPreference) findPreference(BATTERY_TILE_STYLE);
        mBatteryTileStyleValue = Settings.Secure.getInt(resolver,
            Settings.Secure.BATTERY_TILE_STYLE, 0);
        mBatteryTileStyle.setValue(Integer.toString(mBatteryTileStyleValue));
        mBatteryTileStyle.setSummary(mBatteryTileStyle.getEntry());
        mBatteryTileStyle.setOnPreferenceChangeListener(this);

        enableStatusBarBatteryDependents();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBattery) {
            mStatusBarBatteryValue = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            mStatusBarBattery.setSummary(
                mStatusBarBattery.getEntries()[index]);
            Settings.Secure.putInt(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, mStatusBarBatteryValue);
            enableStatusBarBatteryDependents();
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            mStatusBarBatteryShowPercentValue = Integer.valueOf((String) newValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
            mStatusBarBatteryShowPercent.setSummary(
                mStatusBarBatteryShowPercent.getEntries()[index]);
            Settings.Secure.putInt(resolver,
                Settings.Secure.STATUS_BAR_SHOW_BATTERY_PERCENT, mStatusBarBatteryShowPercentValue);
            enableStatusBarBatteryDependents();
            return true;
        } else if  (preference == mForceChargeBatteryText) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.Secure.putInt(resolver,
                Settings.Secure.FORCE_CHARGE_BATTERY_TEXT, checked ? 1:0);
            //enableStatusBarBatteryDependents();
            return true;
        } else if (preference.equals(mChargeColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putInt(resolver,
                Settings.Secure.STATUS_BAR_CHARGE_COLOR, color);
            return true;
        } else if (preference == mTextChargingSymbol) {
            mTextChargingSymbolValue = Integer.valueOf((String) newValue);
            int index = mTextChargingSymbol.findIndexOfValue((String) newValue);
            mTextChargingSymbol.setSummary(
                mTextChargingSymbol.getEntries()[index]);
            Settings.Secure.putInt(resolver,
                Settings.Secure.TEXT_CHARGING_SYMBOL, mTextChargingSymbolValue);
            return true;
        } else if (preference == mBatteryTileStyle) {
            mBatteryTileStyleValue = Integer.valueOf((String) newValue);
            int index = mBatteryTileStyle.findIndexOfValue((String) newValue);
            mBatteryTileStyle.setSummary(
                mBatteryTileStyle.getEntries()[index]);
            Settings.Secure.putInt(resolver,
                Settings.Secure.BATTERY_TILE_STYLE, mBatteryTileStyleValue);
            return true;
        }
        return false;
    }

    private void enableStatusBarBatteryDependents() {
        if (mStatusBarBatteryValue == STATUS_BAR_BATTERY_STYLE_HIDDEN) {
            mStatusBarBatteryShowPercent.setEnabled(false);
            mForceChargeBatteryText.setEnabled(false);
            mChargeColor.setEnabled(false);
            mTextChargingSymbol.setEnabled(false);
        } else if (mStatusBarBatteryValue == STATUS_BAR_BATTERY_STYLE_TEXT) {
            mStatusBarBatteryShowPercent.setEnabled(false);
            mForceChargeBatteryText.setEnabled(false);
            mChargeColor.setEnabled(false);
            mTextChargingSymbol.setEnabled(true);
        } else if (mStatusBarBatteryValue == STATUS_BAR_BATTERY_STYLE_PORTRAIT) {
            mStatusBarBatteryShowPercent.setEnabled(true);
            mChargeColor.setEnabled(true);
            mForceChargeBatteryText.setEnabled(mStatusBarBatteryShowPercentValue == 2 ? false : true);
            //relying on the mForceChargeBatteryText isChecked state is glitchy
            //you need to click it twice to update the mTextChargingSymbol setEnabled state
            //then the mForceChargeBatteryText isChecked state is incorrectly taken inverted
            //so till a fix let's keep mTextChargingSymbol enabled by default
            //mTextChargingSymbol.setEnabled((mStatusBarBatteryShowPercentValue == 0 && !mForceChargeBatteryText.isChecked())
            //|| (mStatusBarBatteryShowPercentValue == 1 && !mForceChargeBatteryText.isChecked()) ? false : true);
            mTextChargingSymbol.setEnabled(true);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
            mChargeColor.setEnabled(true);
            mForceChargeBatteryText.setEnabled(mStatusBarBatteryShowPercentValue == 2 ? false : true);
            //mTextChargingSymbol.setEnabled((mStatusBarBatteryShowPercentValue == 0 && !mForceChargeBatteryText.isChecked())
            //|| (mStatusBarBatteryShowPercentValue == 1 && !mForceChargeBatteryText.isChecked()) ? false : true);
            mTextChargingSymbol.setEnabled(true);
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.aosip_battery;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
