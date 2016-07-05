package GUI;

import Shared.Value;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.svg.QGraphicsSvgItem;

public class Piece extends QGraphicsView {
    private Value value;

    public Piece(QWidget parent) {
        super(parent);
        setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents);
        setScene(new QGraphicsScene(this));
        setStyleSheet("border-style: none; background: transparent");  // necessary?
        setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);  //necessary?
        setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);  //necessary?
        setSizePolicy(
                QSizePolicy.Policy.Ignored,
                QSizePolicy.Policy.Ignored
        );
    }

    public void setIcon(String svgFilePath){
        QGraphicsSvgItem icon = new QGraphicsSvgItem(svgFilePath);
        scene().addItem(icon);
    }

    @Override
    protected void resizeEvent(QResizeEvent event) {
        resetTransform();
        translate(width()/2, height()/2);
        fitInView(scene().itemsBoundingRect(), Qt.AspectRatioMode.KeepAspectRatio);
/*        scale(Math.round(height()/27 * 0.65 * 10000.0)/10000.0,
              Math.round(height()/27 * 0.65 * 10000.0)/10000.0);  // *0.65/27 just works...*/
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
