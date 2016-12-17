package org.foxteam.noisyfox.noexclamation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class MyActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private TextView mTv_detection_enabled, mTv_server;
    private EditText mEditText_server;
    private Switch mSwitch_detection_enabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTv_detection_enabled = (TextView) findViewById(R.id.tv_detection_enabled);
        mTv_server = (TextView) findViewById(R.id.tv_server);
        mEditText_server = (EditText) findViewById(R.id.editText_server);
        mSwitch_detection_enabled = (Switch) findViewById(R.id.switch_detection_enabled);

        ((TextView) findViewById(R.id.tv_version)).setText(String.format("版本: %s", getString(R.string.app_version_name)));

        findViewById(R.id.btn_server_save).setOnClickListener(this);
        findViewById(R.id.btn_server_reset_google).setOnClickListener(this);
        findViewById(R.id.btn_server_set_fox).setOnClickListener(this);

        runTask("获取root权限", new TaskRunnable() {
            @Override
            public Bundle run() {
                String result = cmdExecSu("id");
                if (result == null || !result.contains("uid=0")) {
                    return new Bundle();
                }
                return null;
            }
        }, new AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                if(result != null){
                    Toast.makeText(MyActivity.this, "无法获得root权限！程序将可能无法正常使用！", Toast.LENGTH_LONG).show();
                } else {
                    refreshStatus();
                }
            }
        });

    }

    private void refreshStatus() {
        mSwitch_detection_enabled.setOnCheckedChangeListener(null);

        runTask("刷新当前状态", new TaskRunnable() {
            @Override
            public Bundle run() {
                String portalEnabledStr = cmdExecSu("settings get global captive_portal_detection_enabled");
                String serverStr = cmdExecSu("settings get global captive_portal_server");

                Bundle result = new Bundle();
                result.putString("e", portalEnabledStr);
                result.putString("s", serverStr);

                return result;
            }
        }, new AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                String portalEnabledStr = result.getString("e");
                String serverStr = result.getString("s");

                if ("0".equals(portalEnabledStr)) {
                    mTv_detection_enabled.setText("Off");
                    mSwitch_detection_enabled.setChecked(false);
                } else if ("1".equals(portalEnabledStr)) {
                    mTv_detection_enabled.setText("On");
                    mSwitch_detection_enabled.setChecked(true);
                } else {
                    mTv_detection_enabled.setText("未知:" + portalEnabledStr);
                    mSwitch_detection_enabled.setChecked(false);
                }

                mTv_server.setText((serverStr == null || "null".equals(serverStr)) ? "默认" : serverStr);

                mSwitch_detection_enabled.setOnCheckedChangeListener(MyActivity.this);
            }
        });
    }

    private String cmdExecSu(String cmd) {
        Process p = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            p = new ProcessBuilder("su").start();
            writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            InputStream is = p.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            writer.write(cmd);
            writer.write("\n");
            writer.write("exit\n");
            writer.flush();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            p.waitFor();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (p != null) {
                p.destroy();
            }
        }
        return null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        runTask(isChecked ? "开启中" : "关闭中", new TaskRunnable() {
            @Override
            public Bundle run() {
                if (isChecked) {
                    cmdExecSu("settings put global captive_portal_detection_enabled 1");
                } else {
                    cmdExecSu("settings put global captive_portal_detection_enabled 0");
                }

                return null;
            }
        }, new AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                refreshStatus();
            }
        });
    }

    private void setServer(final String server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("即将把服务器地址设置为 " + (server == null ? "默认" : server) + "，是否继续？");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                runTask("设置中", new TaskRunnable() {
                    @Override
                    public Bundle run() {
                        if (server == null) {
                            cmdExecSu("settings delete global captive_portal_server");
                        } else {
                            cmdExecSu("settings put global captive_portal_server " + server);
                        }

                        return null;
                    }
                }, new AfterTaskRunnable() {
                    @Override
                    public void run(Bundle result) {
                        refreshStatus();
                    }
                });
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_server_save:
                setServer(mEditText_server.getText().toString());
                break;
            case R.id.btn_server_reset_google:
                setServer(null);
                break;
            case R.id.btn_server_set_fox:
                setServer("noisyfox.cn");
                break;
        }
    }

    private final ReentrantLock mSyncRoot = new ReentrantLock();
    private int mTaskCount = 0;
    private ProgressDialog mTaskDialog = null;

    private void runTask(String msg, final TaskRunnable task, final AfterTaskRunnable after) {
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

    private interface AfterTaskRunnable {
        void run(Bundle result);
    }

    private interface TaskRunnable {
        Bundle run();
    }
}
