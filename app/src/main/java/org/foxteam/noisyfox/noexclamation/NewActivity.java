package org.foxteam.noisyfox.noexclamation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import org.foxteam.noisyfox.noexclamation.adapter.ConfigAdapterFactory;
import org.foxteam.noisyfox.noexclamation.adapter.IConfigProvider;
import org.foxteam.noisyfox.noexclamation.adapter.ITaskExecutor;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public class NewActivity extends PreferenceActivity implements ITaskExecutor {

    private IConfigProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_pref);

        PreferenceCategory category = (PreferenceCategory) findPreference("cat_detail_settings");

        provider = ConfigAdapterFactory.createProvider(this, this);
        provider.buildSettingsSection(category);

        findPreference("refresh").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                provider.refreshStatus();
                return true;
            }
        });
        findPreference("set_noisyfox").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                askForConfirm("即将把服务器地址设置为 noisyfox.io，是否继续？", new Runnable() {
                    @Override
                    public void run() {
                        provider.setToNoisyfox();
                    }
                });
                return true;
            }
        });
        findPreference("set_google").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                askForConfirm("即将把服务器地址重置为默认值，是否继续？", new Runnable() {
                    @Override
                    public void run() {
                        provider.resetToGoogle();
                    }
                });
                return true;
            }
        });

        runTask("获取root权限", new TaskRunnable() {
            @Override
            public Bundle run() {
                String result = Utils.cmdExecSu("id");
                if (result == null || !result.contains("uid=0")) {
                    return new Bundle();
                }
                return null;
            }
        }, new AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                if (result != null) {
                    Toast.makeText(NewActivity.this, "无法获得root权限！程序将可能无法正常使用！", Toast.LENGTH_LONG).show();
                }
                provider.refreshStatus();
            }
        });
    }

    private final ReentrantLock mSyncRoot = new ReentrantLock();
    private int mTaskCount = 0;
    private ProgressDialog mTaskDialog = null;

    public void runTask(String msg, final TaskRunnable task, final AfterTaskRunnable after) {
        mSyncRoot.lock();
        try {
            mTaskCount++;
            if (mTaskDialog == null) {
                mTaskDialog = new ProgressDialog(this);
                mTaskDialog.setCancelable(false);
                mTaskDialog.setIndeterminate(true);
            }
            mTaskDialog.setMessage(msg);
            mTaskDialog.show();
        } finally {
            mSyncRoot.unlock();
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                Bundle result = null;
                try {
                    result = task.run();
                } finally {
                    final Bundle b = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            after.run(b);
                            mSyncRoot.lock();
                            try {
                                mTaskCount--;
                                if (mTaskCount == 0) {
                                    mTaskDialog.dismiss();
                                }
                            } finally {
                                mSyncRoot.unlock();
                            }
                        }
                    });
                }
            }
        };
        t.start();
    }

    private void askForConfirm(String msg, final Runnable doStuff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doStuff.run();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}
