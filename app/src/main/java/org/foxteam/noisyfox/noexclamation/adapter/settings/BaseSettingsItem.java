package org.foxteam.noisyfox.noexclamation.adapter.settings;

import org.foxteam.noisyfox.noexclamation.adapter.ITaskExecutor;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public abstract class BaseSettingsItem implements ISettingsItem {
    private OnSettingsChangedListener listener;
    protected ITaskExecutor executor;

    @Override
    public void setOnSettingsChangedListener(OnSettingsChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setToNoisyfox() {
    }

    @Override
    public void resetToGoogle() {
    }

    @Override
    public void injectTaskExecutor(ITaskExecutor executor) {
        this.executor = executor;
    }

    protected void notifySettingsChanged() {
        if (listener != null) {
            listener.onSettingsChanged(this);
        }
    }
}
