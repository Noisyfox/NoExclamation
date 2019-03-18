package org.foxteam.noisyfox.noexclamation.adapter.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import org.foxteam.noisyfox.noexclamation.R;

import static org.foxteam.noisyfox.noexclamation.App.getStr;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public class ListSettings extends GlobalKeyedSettings implements Preference.OnPreferenceChangeListener {
    private ListPreference mPreference;
    private final String mTitle;
    private final String[] mValues;
    private final String[] mDisplayValues;
    private final int defaultValueIndex;

    public ListSettings(String title, String key, String values[], String displayValues[], String defaultValue) {
        super(key);
        mTitle = title;
        mValues = values;
        mDisplayValues = displayValues;

        int d = -1;
        for (int i = 0; i < values.length; i++) {
            if (defaultValue.equals(values[i])) {
                d = i;
                break;
            }
        }
        if (d == -1) {
            throw new RuntimeException("default value not in valid values!");
        }
        defaultValueIndex = d;
    }

    @Override
    public Preference onCreatePreference(Context context) {
        mPreference = new ListPreference(context);
        mPreference.setEntryValues(mValues);
        mPreference.setEntries(mDisplayValues);
        mPreference.setTitle(mTitle);

        refreshPreference(null);

        return mPreference;
    }

    @Override
    public void refreshPreference(Bundle value) {
        mPreference.setOnPreferenceChangeListener(null);

        String v = getValue(value);
        if (v == null || "null".equals(v)) {
            mPreference.setValueIndex(defaultValueIndex);
            mPreference.setSummary(v == null ? getStr(R.string.status_unknown) : mDisplayValues[defaultValueIndex]);
        } else {
            mPreference.setValue(v);
            mPreference.setSummary(mPreference.getEntry());
        }

        mPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setValueAsync("设置中", (String) newValue);
        return false;
    }
}
