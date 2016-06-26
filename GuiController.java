import GUI.CentralWidget;
import GUI.SettingsManager;
import Miscellaneous.Persistence;
import com.trolltech.qt.QSignalEmitter;
import GUI.Signals;
import com.trolltech.qt.gui.*;

public class GuiController extends QSignalEmitter implements Controller{
    private final Signals signals = Signals.getInstance();
    private final SettingsManager settings = SettingsManager.getInstance();
    private final Game.Controller gameController = new Game.Controller();
    private final QMainWindow mainWin;
    private final CentralWidget centralWidget;
    public GuiController(){
        mainWin = new QMainWindow();
        centralWidget = new CentralWidget(mainWin, "centralWidget");
        mainWin.setCentralWidget(centralWidget);
        setupGui();
        if (settings.allKeys().size()==0) resetSettings();
    }
    private void setupGui(){
        setupMenuBar();
        mainWin.show();
    }

    private void resetSettings(){
        System.out.println("RESETTING SETTINGS!");
        settings.setValue(centralWidget.alpha.objectName()+"-whitePawn",
                "D:\\Documents\\Programs\\Chass\\Pawn.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteRook",
                "D:\\Documents\\Programs\\Chass\\Pawn.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteBishop",
                "D:\\Documents\\Programs\\Chass\\Pawn.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteQueen",
                "D:\\Documents\\Programs\\Chass\\Pawn.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteKnight",
                "D:\\Documents\\Programs\\Chass\\Pawn.svg");
        settings.setValue(centralWidget.alpha.objectName()+"-whiteKing",
                "D:\\Documents\\Programs\\Chass\\Pawn.svg");
    }

    private void setupMenuBar(){
        QMenuBar menuBar = new QMenuBar(mainWin);
        QMenu fileMenu = new QMenu("File");
        menuBar.addMenu(fileMenu);
        fileMenu.addAction("Save", signals.saveGame);
        fileMenu.addAction("Exit", signals.exitApplication);
        mainWin.setMenuBar(menuBar);
    }
    @Override
    public void startGame(){
    }

    public void saveGame(){
        Persistence.saveMoves(gameController.getMoves());
    }
}
