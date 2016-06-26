import com.trolltech.qt.gui.QApplication;

/**
 * Starts the Game
 */
public class Main {
    /*
    * If the first parameter is 'gui', then the game will start with a GUI.
    * */
    public static void main(String[] args){
        Controller controller;
        if (args.length != 0 && args[0].equals("gui")) {
            QApplication app = new QApplication(args);
            controller = new GuiController();
            controller.startGame();
            app.exec();
        }else{
            controller = new ConsoleController();
        }
        // give control to controller
/*        QApplication app = new QApplication(args);
        QWidget x = new QWidget();
        x.setProperty("gameColor", Miscellaneous.Color.WHITE);
        System.out.print(x.property("gameColor"));
        System.out.print(x.property("gameColor")==Miscellaneous.Color.WHITE);*/
    }
}
