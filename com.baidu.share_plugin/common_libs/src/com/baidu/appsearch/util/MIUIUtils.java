package com.baidu.appsearch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import android.os.Environment;

// CHECKSTYLE:OFF
public final class MIUIUtils {

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            String line = prop.getProperty(KEY_MIUI_VERSION_NAME, null);
            if (line != null) {
                if (line.contains("V5") || line.contains("V6")) {
                    return true;
                }
            }
            return false;

            // return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null ||
            // prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
            // || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isMIUIV5OrV6() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            String line = prop.getProperty(KEY_MIUI_VERSION_NAME, null);
            if (line != null) {
                if (line.contains("V5") || line.contains("V6")) {
                    return true;
                }
            }
            return false;

        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isMIUIV6() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            String line = prop.getProperty(KEY_MIUI_VERSION_NAME, null);
            if (line != null) {
                if (line.contains("V6")) {
                    return true;
                }
            }
            return false;

        } catch (final IOException e) {
            return false;
        }
    }

    public static int getMIUIVersion() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            String line = prop.getProperty(KEY_MIUI_VERSION_NAME, null);
            if (line != null) {
                if (line.contains("V")) {
                    int index = line.indexOf("V");
                    return Integer.valueOf(line.substring(index + 1, index + 2)).intValue();
                }
            }
            return 0;

        } catch (final IOException e) {
            return 0;
        }
    }

    static class BuildProperties {

        private final Properties properties;

        private BuildProperties() throws IOException {
            properties = new Properties();
            properties.load(new FileInputStream(new File(Environment
                    .getRootDirectory(), "build.prop")));
        }

        public boolean containsKey(final Object key) {
            return properties.containsKey(key);
        }

        public boolean containsValue(final Object value) {
            return properties.containsValue(value);
        }

        public Set<Entry<Object, Object>> entrySet() {
            return properties.entrySet();
        }

        public String getProperty(final String name) {
            return properties.getProperty(name);
        }

        public String getProperty(final String name, final String defaultValue) {
            return properties.getProperty(name, defaultValue);
        }

        public boolean isEmpty() {
            return properties.isEmpty();
        }

        public Enumeration<Object> keys() {
            return properties.keys();
        }

        public Set<Object> keySet() {
            return properties.keySet();
        }

        public int size() {
            return properties.size();
        }

        public Collection<Object> values() {
            return properties.values();
        }

        public static BuildProperties newInstance() throws IOException {
            return new BuildProperties();
        }

    }
}
// CHECKSTYLE:ON
