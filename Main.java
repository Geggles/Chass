import com.trolltech.qt.gui.QApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Starts the Game
 */
public class Main {
    /*
    * If the first parameter is 'gui', then the game will start with a GUI.
    * */
    public static void main(String[] args){
        Controller controller;
        QApplication app = null;

        if (args.length != 0 && args[0].equals("gui")) {
            app = new QApplication(args);
            controller = new GuiController();
        }else{
            controller = new ConsoleController();
        }

        Path savePath = Paths.get(new File("").getAbsolutePath().concat("\\savegames"));
        System.out.println(savePath.toString());
        if (Files.notExists(savePath)){
            try {
                Files.createDirectory(savePath);
            } catch (IOException e){}
        }
        controller.setSavegamePath(savePath);

        if (args.length != 0 && args[0].equals("gui")) {
            app.exec();
        }else{
        }
    }
}
