package org.foxteam.noisyfox.noexclamation.adapter.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public class TextSettings extends GlobalKeyedSettings implements Preference.OnPreferenceChangeListener {
    private EditTextPreference mPreference;
    private final String mTitle;
    private final String mDefaultValue;

    public TextSettings(String title, String key, String defaultValue) {
        super(key);
        mTitle = title;
        mDefaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    @Override
    public Preference onCreatePreference(Context context) {
        mPreference = new EditTextPreference(context);
        mPreference.setTitle(mTitle);
        mPreference.setDialogTitle(mTitle);

        refreshPreference(null);

        return mPreference;
    }

    @Override
    public void refreshPreference(Bundle value) {
        mPreference.setOnPreferenceChangeListener(null);

        String v = getValue(value);
        if (v == null || "null".equals(v)) {
            mPreference.setSummary("默认");
            mPreference.setText(mDefaultValue);
        } else {
            mPreference.setSummary(v);
            mPreference.setText(v);
        }

        mPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setValueAsync("设置中", (String) newValue);
        return false;
    }

    @Override
    public void setToNoisyfox() {
        setValue(getDefaultValue());
    }

    @Override
    public void resetToGoogle() {
        removeValue();
    }
}
