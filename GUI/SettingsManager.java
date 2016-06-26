package GUI;

import com.trolltech.qt.core.QSettings;

public class SettingsManager {
    private static SettingsManager instance = new SettingsManager();
    private QSettings settings;

    public static SettingsManager getInstance() {
        return instance;
    }

    private SettingsManager() {
        settings = new QSettings("config.ini", QSettings.Format.IniFormat);
    }

    public void setValue(String key, Object value){
        settings.setValue(key, value);
    }

    public Object getValue(String key){
        return settings.value(key);
    }

    public Object getValue(String key, Object default_){
        return settings.value(key, default_);
    }
}
