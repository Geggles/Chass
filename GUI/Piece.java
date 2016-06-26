package GUI;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QGraphicsScene;
import com.trolltech.qt.gui.QGraphicsView;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.svg.QGraphicsSvgItem;

public class Piece extends QGraphicsView {
    public Piece(QWidget parent) {
        super(parent);
        setScene(new QGraphicsScene(this));
        setStyleSheet("border-style: none; background: transparent");  // necessary?
        setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);  //necessary?
        setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);  //necessary?
    }

    public void setIcon(String svgFilePath){
        QGraphicsSvgItem icon = new QGraphicsSvgItem(svgFilePath);
        scene().addItem(icon);
    }

    @Override
    protected void resizeEvent(QResizeEvent event) {
        resetTransform();
        translate(width()/2, height()/2);
        scale(Math.round(height()/27 * 0.65 * 10000.0)/10000.0,
              Math.round(height()/27 * 0.65 * 10000.0)/10000.0);  // *0.65/27 just works...
    }
}
