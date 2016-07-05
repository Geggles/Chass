package GUI;

import com.trolltech.qt.core.QSettings;

import java.util.HashMap;
import java.util.List;

public class SettingsManager {
    private static SettingsManager instance = new SettingsManager();
    private HashMap<String, Object> buffer = new HashMap<>();
    private Signals signals = Signals.getInstance();
    private QSettings settings;

    public static SettingsManager getInstance() {
        return instance;
    }

    private SettingsManager() {
        settings = new QSettings("config.ini", QSettings.Format.IniFormat);
    }

    /**
     * Save value directly to disk, also update buffer and emit settingStored signal.
     * @param key
     * @param value
     */
    public void storeValue(String key, Object value){
        settings.setValue(key, value);
        setValue(key, value);
        signals.settingStored.emit(key, value);
    }

    public Object loadValue(String key){
        return settings.value(key);
    }

    public List<String> allKeys(){
        return settings.allKeys();
    }

    /**
     * Set value to be stored in memory, only write to disk when flushed.
     */
    public void setValue(String key, Object value){
        buffer.put(key, value);
        signals.settingSet.emit(key, value);
    }

    public Object getValue(String key){
        return buffer.get(key);
    }

    /**
     * Save all buffered settings onto disk. Bypass signal emitting and buffer setting.
     */
    public void flush(){
        buffer.entrySet().stream().forEach(
                entry -> settings.setValue(entry.getKey(), entry.getValue()));
    }

    /**
     * Load all settings from disk into buffer.
     */
    public void loadIntoBuffer(){
        settings.allKeys().forEach(key -> setValue(key, settings.value(key)));
    }
}
