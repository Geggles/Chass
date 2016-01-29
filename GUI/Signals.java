package GUI;

import com.trolltech.qt.QSignalEmitter;
/**
* Centralizes signals that can be used to communicate between GUI and Controller.
* */
public class Signals extends QSignalEmitter{
    public Signal0 saveGame = new Signal0();
    public Signal0 exitApplication = new Signal0();
}
