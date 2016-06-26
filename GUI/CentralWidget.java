package GUI;

import Miscellaneous.Color;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QWidget;

public class CentralWidget extends QWidget {
    private final Signals signals = Signals.getInstance();
    public final Board alpha;
    public final Board beta;
    public final Board gamma;
    public final Board whitePrison;
    public final Board whiteAirfield;
    public final Board blackPrison;
    public final Board blackAirfield;
    public CentralWidget(QWidget parent, String objectName) {
        super(parent);
        setObjectName(objectName);
        setLayout(new QGridLayout(this));

        alpha = new Board(this, objectName+".alpha", 8, Color.WHITE);
        beta = new Board(this, objectName+".beta", 8, Color.BLACK);
        gamma = new Board(this, objectName+".gamma", 8, Color.NONE);
        whitePrison = new Board(this, objectName+".whitePrison", 4, Color.NONE);
        whiteAirfield = new Board(this, objectName+".whiteAirfield", 4, Color.NONE);
        blackPrison = new Board(this, objectName+".blackPrison", 4, Color.NONE);
        blackAirfield = new Board(this, objectName+".blackAirfield", 4, Color.NONE);

        ((QGridLayout)layout()).addWidget(alpha, 2, 0, 2, 4);
        ((QGridLayout)layout()).addWidget(beta,  2, 4, 2, 4);
        ((QGridLayout)layout()).addWidget(gamma, 0, 2, 2, 4);
        ((QGridLayout)layout()).addWidget(whitePrison  ,0,0,1,2);
        ((QGridLayout)layout()).addWidget(whiteAirfield,1,0,1,2);
        ((QGridLayout)layout()).addWidget(blackPrison  ,0,6,1,2);
        ((QGridLayout)layout()).addWidget(blackAirfield,1,6,1,2);
    }
}
