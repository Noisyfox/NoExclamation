package org.foxteam.noisyfox.noexclamation.adapter.settings;

import android.os.Bundle;

import org.foxteam.noisyfox.noexclamation.Utils;
import org.foxteam.noisyfox.noexclamation.adapter.ITaskExecutor;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public abstract class GlobalKeyedSettings extends BaseSettingsItem {
    protected final String mKey;

    protected GlobalKeyedSettings(String key) {
        mKey = key;
    }

    @Override
    public void reloadValue(Bundle valueOut) {
        valueOut.putString(mKey, Utils.cmdExecSu("settings get global " + mKey));
    }

    protected String getValue(Bundle value) {
        String v = null;
        if (value != null) {
            v = value.getString(mKey);
        }

        return v;
    }

    protected void setValue(String value) {
        Utils.cmdExecSu(String.format("settings put global %s %s", mKey, value));
    }

    protected void removeValue() {
        Utils.cmdExecSu("settings delete global " + mKey);
    }

    protected void setValueAsync(String msg, final String value) {
        executor.runTask(msg, new ITaskExecutor.TaskRunnable() {
            @Override
            public Bundle run() {
                setValue(value);
                return null;
            }
        }, new ITaskExecutor.AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                notifySettingsChanged();
            }
        });
    }

    protected void removeValueAsync(String msg) {
        executor.runTask(msg, new ITaskExecutor.TaskRunnable() {
            @Override
            public Bundle run() {
                removeValue();
                return null;
            }
        }, new ITaskExecutor.AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                notifySettingsChanged();
            }
        });
    }

}
