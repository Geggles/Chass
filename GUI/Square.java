package GUI;

import Miscellaneous.Color;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QWidget;

public class Square extends QWidget {
    private QColor lightColor = (QColor) SettingsManager.getInstance().getValue(
            objectName()+"-lightSquareColor");
    private QColor darkColor = (QColor) SettingsManager.getInstance().getValue(
            objectName()+"-darkSquareColor");
    private final Color color;
    public Square(QWidget parent, Color color) {
        QPalette squarePalette = palette();
        squarePalette.setColor(
                QPalette.ColorGroup.All,
                QPalette.ColorRole.Window,
                color == Color.WHITE ? lightColor: darkColor);
        this.color = color;
    }
    // shouldn't really be necessary, as you can always get this through calculations, but why not..
    public Color getColor(){
        return color;
    }

    /**
     * Set the background color of this square to color
     */
    public void setColor(QColor color){

    }
}
