package GUI;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.*;

public class NavigationButtons extends QWidget {
    public NavigationButtons(QWidget parent, String objectName) {
        super(parent);
        setObjectName(objectName);

        setLayout(new QHBoxLayout(this));
        QPushButton undoButton = new QPushButton(this);
        QPushButton redoButton = new QPushButton(this);
        layout().addWidget(undoButton);
        layout().addWidget(redoButton);

        undoButton.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
        redoButton.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
    }
}
