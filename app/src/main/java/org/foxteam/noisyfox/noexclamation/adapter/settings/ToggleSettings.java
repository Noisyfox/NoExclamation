package org.foxteam.noisyfox.noexclamation.adapter.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public class ToggleSettings extends GlobalKeyedSettings implements Preference.OnPreferenceChangeListener {

    private SwitchPreference mPreference;
    private final String mTitle;
    private final String mPositiveValue;
    private final String mNegativeValue;
    private final State mStateIfNotSet;
    private final String mPositiveSummary;
    private final String mNegativeSummary;
    private final String mPositiveProgress;
    private final String mNegativeProgress;

    public ToggleSettings(String title, String key, String valuePositive, String valueNegative, State stateIfNotSet,
                          String summaryPositive, String summaryNegative,
                          String progressPositive, String progressNegative) {
        super(key);
        mTitle = title;
        mPositiveValue = valuePositive;
        mNegativeValue = valueNegative;
        mStateIfNotSet = stateIfNotSet;
        mPositiveSummary = summaryPositive;
        mNegativeSummary = summaryNegative;
        mPositiveProgress = progressPositive;
        mNegativeProgress = progressNegative;
    }

    @Override
    public Preference onCreatePreference(Context context) {
        mPreference = new SwitchPreference(context);

        mPreference.setTitle(mTitle);

        refreshPreference(null);

        return mPreference;
    }

    public State getState(String currentValue) {
        if (currentValue != null) {
            if (currentValue.equals("null")) {
                return mStateIfNotSet;
            } else if (currentValue.equals(mNegativeValue)) {
                return State.Negative;
            } else if (currentValue.equals(mPositiveValue)) {
                return State.Positive;
            }
        }
        return State.Unknown;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean checked = (boolean) newValue;
        setValueAsync(checked ? mPositiveProgress : mNegativeProgress, checked ? mPositiveValue : mNegativeValue);
        return false;
    }

    public enum State {
        Positive, Negative, Unknown
    }

    @Override
    public void refreshPreference(Bundle value) {
        mPreference.setOnPreferenceChangeListener(null);
        String v = getValue(value);

        State s = getState(v);
        switch (s) {
            case Positive:
                mPreference.setSummary(mPositiveSummary);
                mPreference.setChecked(true);
                break;
            case Negative:
                mPreference.setSummary(mNegativeSummary);
                mPreference.setChecked(false);
                break;
            case Unknown:
                mPreference.setSummary("未知");
                mPreference.setChecked(false);
                break;
        }
        mPreference.setOnPreferenceChangeListener(this);
    }
}
