package GUI;

import com.trolltech.qt.core.QSettings;

import java.util.List;

public class SettingsManager {
    private static SettingsManager instance = new SettingsManager();
    private Signals signals = Signals.getInstance();
    private QSettings settings;

    public static SettingsManager getInstance() {
        return instance;
    }

    private SettingsManager() {
        settings = new QSettings("config.ini", QSettings.Format.IniFormat);
    }

    public void setValue(String key, Object value){
        settings.setValue(key, value);
        signals.settingSet.emit(key, value);
    }

    public Object getValue(String key){
        return settings.value(key);
    }

    public Object getValue(String key, Object default_){
        return settings.value(key, default_);
    }

    public List<String> allKeys(){
        return settings.allKeys();
    }
}
