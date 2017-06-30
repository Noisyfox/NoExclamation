package org.foxteam.noisyfox.noexclamation.adapter;

import android.preference.PreferenceCategory;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public interface IConfigProvider {

    void buildSettingsSection(PreferenceCategory category);

    void refreshStatus();

    void resetToGoogle();

    void setToNoisyfox();
}
