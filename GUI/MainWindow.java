package GUI;

import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QMainWindow;

public class MainWindow extends QMainWindow{
    private Signals signals = Signals.getInstance();
    public MainWindow(String objectName) {
        super();
        setObjectName(objectName);
    }

    @Override
    protected void closeEvent(QCloseEvent event) {
        signals.exitApplication.emit();
    }


}
