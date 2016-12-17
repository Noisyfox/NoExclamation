package org.foxteam.noisyfox.noexclamation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class MyActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private TextView mTv_detection_enabled, mTv_https_enabled, mTv_server, mTv_url, mTv_url_https;
    private EditText mEditText_server, mEditText_url, mEditText_url_https;
    private Switch mSwitch_detection_enabled, mSwitch_https_enabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTv_detection_enabled = (TextView) findViewById(R.id.tv_detection_enabled);
        mTv_https_enabled = (TextView) findViewById(R.id.tv_https_enabled);
        mTv_server = (TextView) findViewById(R.id.tv_server);
        mTv_url = (TextView) findViewById(R.id.tv_url);
        mTv_url_https = (TextView) findViewById(R.id.tv_url_https);
        mEditText_server = (EditText) findViewById(R.id.editText_server);
        mEditText_url = (EditText) findViewById(R.id.editText_url);
        mEditText_url_https = (EditText) findViewById(R.id.editText_url_https);
        mSwitch_detection_enabled = (Switch) findViewById(R.id.switch_detection_enabled);
        mSwitch_https_enabled = (Switch) findViewById(R.id.switch_https_enabled);

        ((TextView) findViewById(R.id.tv_version)).setText(String.format("版本: %s", getString(R.string.app_version_name)));

        SafeSetOnClickListener(R.id.btn_server_save);
        SafeSetOnClickListener(R.id.btn_url_save);
        SafeSetOnClickListener(R.id.btn_url_https_save);
        SafeSetOnClickListener(R.id.btn_server_reset_google);
        SafeSetOnClickListener(R.id.btn_server_set_fox);

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
                if (result != null) {
                    Toast.makeText(MyActivity.this, "无法获得root权限！程序将可能无法正常使用！", Toast.LENGTH_LONG).show();
                }
                refreshStatus();
            }
        });
    }

    private void SafeSetOnClickListener(int id) {
        View v = findViewById(id);
        if (v != null) {
            v.setOnClickListener(this);
        }
    }

    private void refreshStatus() {
        int ver = Build.VERSION.SDK_INT;
        if (ver < Build.VERSION_CODES.N) {
            refreshStatus_5();
        } else if (ver == Build.VERSION_CODES.N) {
            refreshStatus_710();
        } else {
            refreshStatus_711();
        }
    }

    private void refreshStatus_5() {
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

                updateSwitch(portalEnabledStr, mTv_detection_enabled, mSwitch_detection_enabled);

                updateTextView(serverStr, mTv_server);

                mSwitch_detection_enabled.setOnCheckedChangeListener(MyActivity.this);
            }
        });
    }

    private void refreshStatus_710() {
        mSwitch_detection_enabled.setOnCheckedChangeListener(null);
        mSwitch_https_enabled.setOnCheckedChangeListener(null);

        runTask("刷新当前状态", new TaskRunnable() {
            @Override
            public Bundle run() {
                String portalEnabledStr = cmdExecSu("settings get global captive_portal_detection_enabled");
                String httpsEnabledStr = cmdExecSu("settings get global captive_portal_use_https");
                String serverStr = cmdExecSu("settings get global captive_portal_server");

                Bundle result = new Bundle();
                result.putString("e", portalEnabledStr);
                result.putString("https", httpsEnabledStr);
                result.putString("s", serverStr);

                return result;
            }
        }, new AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                String portalEnabledStr = result.getString("e");
                String httpsEnabledStr = result.getString("https");
                String serverStr = result.getString("s");

                updateSwitch(portalEnabledStr, mTv_detection_enabled, mSwitch_detection_enabled);
                updateSwitch(httpsEnabledStr, mTv_https_enabled, mSwitch_https_enabled);

                updateTextView(serverStr, mTv_server);

                mSwitch_detection_enabled.setOnCheckedChangeListener(MyActivity.this);
                mSwitch_https_enabled.setOnCheckedChangeListener(MyActivity.this);
            }
        });
    }

    private void refreshStatus_711() {
        mSwitch_detection_enabled.setOnCheckedChangeListener(null);
        mSwitch_https_enabled.setOnCheckedChangeListener(null);

        runTask("刷新当前状态", new TaskRunnable() {
            @Override
            public Bundle run() {
                String portalEnabledStr = cmdExecSu("settings get global captive_portal_detection_enabled");
                String httpsEnabledStr = cmdExecSu("settings get global captive_portal_use_https");
                String urlStr = cmdExecSu("settings get global captive_portal_http_url");
                String urlHttpsStr = cmdExecSu("settings get global captive_portal_https_url");

                Bundle result = new Bundle();
                result.putString("e", portalEnabledStr);
                result.putString("https", httpsEnabledStr);
                result.putString("u", urlStr);
                result.putString("us", urlHttpsStr);

                return result;
            }
        }, new AfterTaskRunnable() {
            @Override
            public void run(Bundle result) {
                String portalEnabledStr = result.getString("e");
                String httpsEnabledStr = result.getString("https");
                String urlStr = result.getString("u");
                String urlHttpsStr = result.getString("us");

                updateSwitch(portalEnabledStr, mTv_detection_enabled, mSwitch_detection_enabled);
                updateSwitch(httpsEnabledStr, mTv_https_enabled, mSwitch_https_enabled);

                updateTextView(urlStr, mTv_url);
                updateTextView(urlHttpsStr, mTv_url_https);

                mSwitch_detection_enabled.setOnCheckedChangeListener(MyActivity.this);
                mSwitch_https_enabled.setOnCheckedChangeListener(MyActivity.this);
            }
        });
    }

    private void updateSwitch(String value, TextView tv, Switch sw) {
        if ("0".equals(value)) {
            tv.setText("Off");
            sw.setChecked(false);
        } else if ("1".equals(value) || "null".equals(value)) {
            tv.setText("On");
            sw.setChecked(true);
        } else {
            tv.setText("未知:" + value);
            sw.setChecked(false);
        }
    }

    private void updateTextView(String value, TextView tv) {
        tv.setText((value == null || "null".equals(value)) ? "默认" : value);
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
        switch (buttonView.getId()) {
            case R.id.switch_detection_enabled:
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
                break;

            case R.id.switch_https_enabled:
                runTask(isChecked ? "开启中" : "关闭中", new TaskRunnable() {
                    @Override
                    public Bundle run() {
                        if (isChecked) {
                            cmdExecSu("settings put global captive_portal_use_https 1");
                        } else {
                            cmdExecSu("settings put global captive_portal_use_https 0");
                        }

                        return null;
                    }
                }, new AfterTaskRunnable() {
                    @Override
                    public void run(Bundle result) {
                        refreshStatus();
                    }
                });
                break;
        }
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

    private void setUrl(final String url, final boolean https) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("即将把服务器 URL (" + (https ? "https" : "http") + ") 设置为 " + (url == null ? "默认" : url) + "，是否继续？");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                runTask("设置中", new TaskRunnable() {
                    @Override
                    public Bundle run() {
                        String key = https ? "captive_portal_https_url" : "captive_portal_http_url";
                        if (url == null) {
                            cmdExecSu("settings delete global " + key);
                        } else {
                            cmdExecSu("settings put global " + key + " " + url);
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

    private void setBothUrl(final String http, final String https) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format("即将把服务器 URL 设置为:\n%s\n%s\n是否继续？", http == null ? "HTTP: 默认" : http,
                https == null ? "HTTPS: 默认" : https));
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                runTask("设置中", new TaskRunnable() {
                    @Override
                    public Bundle run() {
                        //String key = https ? "captive_portal_https_url" : "captive_portal_http_url";
                        if (http == null) {
                            cmdExecSu("settings delete global captive_portal_http_url");
                        } else {
                            cmdExecSu("settings put global captive_portal_http_url " + http);
                        }
                        if (https == null) {
                            cmdExecSu("settings delete global captive_portal_https_url");
                        } else {
                            cmdExecSu("settings put global captive_portal_https_url " + https);
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
                setServer(mEditText_server.getText().toString().trim());
                break;
            case R.id.btn_url_save:
                setUrl(mEditText_url.getText().toString().trim(), false);
                break;
            case R.id.btn_url_https_save:
                setUrl(mEditText_url_https.getText().toString().trim(), true);
                break;
            case R.id.btn_server_reset_google:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    setBothUrl(null, null);
                } else {
                    setServer(null);
                }
                break;
            case R.id.btn_server_set_fox:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    setBothUrl("http://noisyfox.cn/generate_204", "https://noisyfox.cn/generate_204");
                } else {
                    setServer("noisyfox.cn");
                }
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
