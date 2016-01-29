import com.trolltech.qt.gui.*;

public class GuiController implements Controller{
    private QMainWindow mainWin;
    public GuiController(){
    }

    private void setupMenuBar(){
        QMenuBar menuBar = new QMenuBar(mainWin);
        QMenu fileMenu = new QMenu(menuBar);
        menuBar.addMenu(fileMenu);
        mainWin.setMenuBar(menuBar);
    }

    public int startGame(){
        setupGui();
        return 0;
    }
    private void setupGui(){
        String[] args = new String[0];
        QApplication app = new QApplication(args);
        mainWin = new QMainWindow();
        QWidget centralWidget = new QWidget(mainWin);
        mainWin.setCentralWidget(centralWidget);

        setupMenuBar();

        mainWin.show();
        app.exec();
    }
    public int saveGame(){
        return 0;
    }
}
