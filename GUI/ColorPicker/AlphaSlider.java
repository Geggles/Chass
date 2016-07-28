package GUI.ColorPicker;

import com.trolltech.qt.QPair;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;

public class AlphaSlider extends QWidget {
    private QColor baseColor = QColor.fromRgb(0, 0, 0, 1);
    private QTimer paintTimer = new QTimer(this);
    private boolean fullColors = true;

    public Signal1<QColor> colorSelected = new Signal1<>();

    public AlphaSlider(QWidget parent) {
        super(parent);
        setMaximumWidth(50);
        paintTimer.timeout.connect(this, "updateColors()");
        paintTimer.setInterval(500);
        paintTimer.setSingleShot(true);
    }

    public void setBaseColor(QColor color){
        baseColor = color;
        update();
    }

    private void updateColors(){
        fullColors = true;
        update();
    }

    private QColor getColor(int y) {
        if (y>height()) baseColor.setAlphaF(0);
        else if (y<0) baseColor.setAlphaF(1);
        else baseColor.setAlphaF(1 - y/(double)height());
        return baseColor;
    }

    private void selectColor(int y) {
        colorSelected.emit(getColor(y));
    }

    @Override
    protected void mousePressEvent(QMouseEvent event) {
        switch (event.button()){
            case LeftButton:
                int y = event.pos().y();
                selectColor(y);
            case RightButton:
                selectColor(height()/2);
        }
    }

    @Override
    protected void mouseMoveEvent(QMouseEvent event) {
        int y = event.pos().y();
        selectColor(y);
    }

    @Override
    protected void resizeEvent(QResizeEvent event) {
        fullColors = false;
        paintTimer.start();
    }

    @Override
    protected void paintEvent(QPaintEvent event) {
        QStyleOption styleOption = new QStyleOption();
        styleOption.initFrom(this);
        QPainter painter = new QPainter(this);
        painter.setRenderHint(QPainter.RenderHint.Antialiasing);
        style().drawPrimitive(QStyle.PrimitiveElement.PE_Widget, styleOption, painter, this);
////////
        int cx = rect().center().x();
        QLinearGradient gradient = new QLinearGradient(cx, 0, cx, rect().bottom());
        baseColor.setAlphaF(1);
        gradient.setColorAt(0, baseColor);
        baseColor.setAlphaF(0);
        gradient.setColorAt(1, baseColor);
        QBrush brush = new QBrush(gradient);
        painter.fillRect(rect(), brush);
/////////

/*
        QPen pen;

        int RESOLUTION;  // 1 is best quality
        if (fullColors) {
            RESOLUTION = 1;
        } else {
            if (height() < 25) RESOLUTION = 1;
            else if (height() < 50) RESOLUTION = 2;
            else if (height() < 75) RESOLUTION = 3;
            else RESOLUTION = 4;
        }

        for (int y = 0; y < height(); y += RESOLUTION) {
            baseColor.setAlphaF(getAlphaValue(y));
            pen = new QPen(baseColor);
            pen.setWidth(RESOLUTION);
            painter.setPen(pen);
            painter.drawLine(0, y, width(), y);
        }
        // Drawing Border
        //painter.setRenderHint(QPainter.RenderHint.Antialiasing, true);
        //painter.setRenderHint(QPainter.RenderHint.Antialiasing, false);
*/

        painter.end();
    }
}
