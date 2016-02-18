import com.trolltech.qt.QSignalEmitter; import GUI.Signals; import com.trolltech.qt.gui.*; public class GuiController extends QSignalEmitter implements Controller{ /*Emit when file->save is invoked*/ private Signals signals = new Signals(); private Game.Controller gameController; private QMainWindow mainWin; public GuiController(){ String[] args = new String[0]; QApplication app = new QApplication(args); setupGui(); signals.saveGame.connect(this, "saveGame()"); signals.exitApplication.connect(app, "exit()"); app.exec(); } public int startGame(){ gameController = new Game.Controller(); gameController.newGame(); return 0; }
    private void setupGui(){
        mainWin = new QMainWindow();
        QWidget centralWidget = new QWidget(mainWin);
        mainWin.setCentralWidget(centralWidget);

        setupMenuBar();
        mainWin.show();
    }

    private void setupMenuBar(){
        QMenuBar menuBar = new QMenuBar(mainWin);
        QMenu fileMenu = new QMenu("File");
        menuBar.addMenu(fileMenu);
        fileMenu.addAction("Save", signals.saveGame);
        fileMenu.addAction("Exit", signals.exitApplication);
        mainWin.setMenuBar(menuBar);
    }

    public int saveGame(){
        return 0;
    }
}
