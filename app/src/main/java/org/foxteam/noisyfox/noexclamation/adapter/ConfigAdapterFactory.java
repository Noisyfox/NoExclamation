package org.foxteam.noisyfox.noexclamation.adapter;

import android.content.Context;
import android.provider.Settings;

import org.foxteam.noisyfox.noexclamation.adapter.settings.ListSettings;
import org.foxteam.noisyfox.noexclamation.adapter.settings.TextSettings;
import org.foxteam.noisyfox.noexclamation.adapter.settings.ToggleSettings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public class ConfigAdapterFactory {
    public static IConfigProvider createProvider(Context context,
                                                 ITaskExecutor taskExecutor) {
        CommonConfigProvider provider = new CommonConfigProvider(context, taskExecutor);

        // Check for android 7.1.2 which use 'captive_portal_mode' instead of 'captive_portal_detection_enabled'
        if (isSettingsPresent("captive_portal_mode")) {
            provider.addSettings(new ListSettings("Captive Portal 检测模式",
                    "captive_portal_mode",
                    new String[]{"0", "1", "2"},
                    new String[]{"完全禁用检测", "检测到需要登录则弹框提醒", "检测到需要登录则自动断开此网络并不再自动连接"},
                    "1"));
        } else {
            provider.addSettings(new ToggleSettings("Captive Portal 检测",
                    "captive_portal_detection_enabled", "1", "0", ToggleSettings.State.Positive, "On", "Off", "开启中", "关闭中"));
        }

        // For android 7.0+
        if (isSettingsPresent("captive_portal_use_https")) {
            provider.addSettings(new ToggleSettings("启用 https",
                    "captive_portal_use_https", "1", "0", ToggleSettings.State.Positive, "On", "Off", "开启中", "关闭中"));
        }

        boolean a711 = false;
        if (isSettingsPresent("captive_portal_http_url")) {
            a711 = true;
            provider.addSettings(new TextSettings("服务地址(http)", "captive_portal_http_url", "http://www.noisyfox.io/generate_204"));
        }
        if (isSettingsPresent("captive_portal_https_url")) {
            a711 = true;
            provider.addSettings(new TextSettings("服务地址(https)", "captive_portal_https_url", "https://www.noisyfox.io/generate_204"));
        }
        if (!a711) {
            provider.addSettings(new TextSettings("服务地址", "captive_portal_server", "noisyfox.io"));
        }

        return provider;
    }

    private static boolean isSettingsPresent(String key) {
        Class clazz = Settings.Global.class;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers())) {
                try {
                    String v = (String) field.get(null);
                    if (key.equals(v)) {
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
