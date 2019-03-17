package org.foxteam.noisyfox.noexclamation.adapter;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceCategory;

import org.foxteam.noisyfox.noexclamation.R;
import org.foxteam.noisyfox.noexclamation.adapter.settings.ISettingsItem;

import java.util.ArrayList;
import java.util.List;

import static org.foxteam.noisyfox.noexclamation.App.getStr;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public class CommonConfigProvider implements IConfigProvider, ISettingsItem.OnSettingsChangedListener {

    private final List<ISettingsItem> mAllSettings = new ArrayList<>();
    private final Context mContext;
    private final ITaskExecutor mTaskExecutor;

    public CommonConfigProvider(Context context,
                                ITaskExecutor taskExecutor) {
        mContext = context;
        mTaskExecutor = taskExecutor;
    }

    public void addSettings(ISettingsItem settingsItem) {
        settingsItem.injectTaskExecutor(mTaskExecutor);
        settingsItem.setOnSettingsChangedListener(this);
        mAllSettings.add(settingsItem);
    }

    @Override
    public void buildSettingsSection(PreferenceCategory category) {
        for (ISettingsItem s : mAllSettings) {
            category.addPreference(s.onCreatePreference(mContext));
        }
    }

    @Override
    public void refreshStatus() {
        mTaskExecutor.runTask(getStr(R.string.status_refresh), new ITaskExecutor.TaskRunnable() {
            @Override
            public Bundle run() {
                Bundle result = new Bundle();
                for (ISettingsItem s : mAllSettings) {
                    s.reloadValue(result);
                }
                return result;
            }
        }, new ITaskExecutor.AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                for (ISettingsItem s : mAllSettings) {
                    s.refreshPreference(result);
                }
            }
        });
    }

    @Override
    public void resetToGoogle() {
        mTaskExecutor.runTask(getStr(R.string.status_reset), new ITaskExecutor.TaskRunnable() {
            @Override
            public Bundle run() {
                for (ISettingsItem s : mAllSettings) {
                    s.resetToGoogle();
                }
                return null;
            }
        }, new ITaskExecutor.AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                refreshStatus();
            }
        });
    }

    @Override
    public void setToNoisyfox() {
        mTaskExecutor.runTask(getStr(R.string.status_setNoisyFox), new ITaskExecutor.TaskRunnable() {
            @Override
            public Bundle run() {
                for (ISettingsItem s : mAllSettings) {
                    s.setToNoisyfox();
                }
                return null;
            }
        }, new ITaskExecutor.AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                refreshStatus();
            }
        });
    }

    @Override
    public void onSettingsChanged(ISettingsItem settingsItem) {
        refreshStatus();
    }
}
