package GUI;

import Game.Color;
import com.trolltech.qt.gui.*;

import java.util.HashMap;
import java.util.Map;

public class Board extends QWidget{
    private QGridLayout layout;
    private final int size;
    private HashMap<int[], QWidget> squares = new HashMap<>(64);
    private HashMap<int[], QGraphicsView> pieces = new HashMap<>(64);
    /**
     * @param signals Reference to the Signals object that contains signals that are can to be emitted by this class
     * @param size Size of the board. Either 4 or 8.
     */
    public Board(QWidget parent, String objectName, Signals signals, int size){
        super(parent);
        this.setObjectName(objectName);
        this.layout = new QGridLayout(this);
        this.size = size;
    }

    private void setupSquares(){
        Color squareColor = Color.WHITE;
        QColor lightColor = (QColor) SettingsManager.getInstance().getValue(
                this.objectName()+"_lightColor");
        QColor darkColor = (QColor) SettingsManager.getInstance().getValue(
                this.objectName()+"_darkColor");
        for (int x=0; x<size; x++){
            for (int y=0; y<size; y++){
                QWidget newSquare = new QWidget(this);
                newSquare.setProperty("squareColor", squareColor);

                QPalette squarePalette = newSquare.palette();
                squarePalette.setColor(
                        QPalette.ColorGroup.All,
                        QPalette.ColorRole.Window,
                        squareColor == Color.WHITE ? lightColor: darkColor);

                squareColor = squareColor.opposite();
                this.squares.put(new int[]{x, y}, newSquare);
                this.layout.addWidget(newSquare, y, x, 1, 1);
            }
        }
    }
}
