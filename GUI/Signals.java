package GUI;

import Game.Value;
import com.trolltech.qt.QSignalEmitter;
/**
* Centralizes signals that can be used to communicate between GUI and Controller.
* */
public class Signals extends QSignalEmitter{
    private static Signals instance = new Signals();

    public static Signals getInstance() {
        return instance;
    }
    public Signal0 saveGame = new Signal0();
    public Signal0 exitApplication = new Signal0();
    // Invoked when the SettingsManager sets a settings. Emits <key, value>.
    public Signal2<String, Object> settingSet = new Signal2<>();
    private Signals(){

    }
}
