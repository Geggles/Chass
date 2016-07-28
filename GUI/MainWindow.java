package GUI;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

public class MainWindow extends QMainWindow{
    private Signals signals = Signals.getInstance();
    private final CentralWidget centralWidget;
    public final NavigationButtons navigationButtons;
    public final MoveNavigator moveNavigator;

    public MainWindow(String objectName) {
        super();
        setObjectName(objectName);

        centralWidget = new CentralWidget(this, objectName() + ".centralWidget");
        navigationButtons = new NavigationButtons(this, objectName() + ".navigationButtons");
        moveNavigator = new MoveNavigator(this, objectName() + ".moveNavigator");

        setCentralWidget(centralWidget);
    }

    @Override
    protected void closeEvent(QCloseEvent event) {
        signals.exitApplication.emit();
        super.closeEvent(event);
    }
}
