package GUI;

import Shared.Value;
import com.trolltech.qt.QSignalEmitter;

import java.nio.file.Path;

/**
* Centralizes signals that can be used to communicate between GUI and Controller.
* */
public class Signals extends QSignalEmitter{
    private static Signals instance = new Signals();

    public Signal0 saveGame = new Signal0();
    public Signal0 saveGameAs = new Signal0();
    public Signal0 exitApplication = new Signal0();
    public Signal0 moveCanceled = new Signal0();
    public Signal0 loadGame = new Signal0();
    public Signal0 newGame = new Signal0();
    public Signal0 rewindMove = new Signal0();
    public Signal0 repeatMove = new Signal0();
    // Emitted when player hovers over a piece.
    public Signal1<Square> squareSelected = new Signal1<>();
    // Emitted when player hovers over a board.
    public Signal1<Board> boardSelected = new Signal1<>();
    public Signal1<Board> boardDeselected = new Signal1<>();
    public Signal1<Boolean> boardScrolled = new Signal1<>();  // whether shift is positive
    // Emitted when player stops hovering over a piece.
    public Signal1<Square> squareDeselected = new Signal1<>();
    // Emitted when player clicks on piece.
    public Signal1<Square> pieceSelected = new Signal1<>();
    // Emitted when player drops a piece on a square.
    public Signal1<Square> destinationSelected = new Signal1<>();
    // Emitted when the SettingsManager sets a settings. Emits <key, value>.
    public Signal2<String, Object> settingSet = new Signal2<>();
    public Signal2<String, Object> settingStored = new Signal2<>();

    private Signals(){

    }
    public static Signals getInstance() {
        return instance;
    }
}
