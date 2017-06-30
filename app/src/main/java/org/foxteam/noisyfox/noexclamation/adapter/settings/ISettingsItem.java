package org.foxteam.noisyfox.noexclamation.adapter.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;

import org.foxteam.noisyfox.noexclamation.adapter.ITaskExecutor;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public interface ISettingsItem {
    Preference onCreatePreference(Context context);

    void reloadValue(Bundle valueOut);

    void refreshPreference(Bundle value);

    void setToNoisyfox();

    void resetToGoogle();

    void setOnSettingsChangedListener(OnSettingsChangedListener listener);

    void injectTaskExecutor(ITaskExecutor executor);

    interface OnSettingsChangedListener {
        void onSettingsChanged(ISettingsItem settingsItem);
    }
}
