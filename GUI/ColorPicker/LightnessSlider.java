package GUI.ColorPicker;

import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

public class LightnessSlider extends QWidget {
    private QColor baseColor = QColor.fromRgb(255, 0, 0, 255);
    private QTimer paintTimer = new QTimer(this);
    private boolean fullColors = true;

    public Signal1<QColor> colorSelected = new Signal1<>();

    public LightnessSlider(QWidget parent) {
        super(parent);
        setMaximumWidth(50);
        paintTimer.timeout.connect(this, "updateColors()");
        paintTimer.setInterval(500);
        paintTimer.setSingleShot(true);
    }

    public void setBaseColor(QColor color){
        baseColor = color.toHsl();
        update();
    }

    private void updateColors(){
        fullColors = true;
        update();
    }

    private QColor getColor(int y) {
        double hue = baseColor.hslHueF();
        double saturation = baseColor.hslSaturationF();
        double alpha = baseColor.alphaF();
        if (y>height()) baseColor.setHslF(hue, saturation, 0, alpha);
        else if (y<0) baseColor.setHslF(hue, saturation, 1, alpha);
        else baseColor.setHslF(hue, saturation, 1 - y/(double)height(), alpha);
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
        painter.setRenderHint(QPainter.RenderHint.HighQualityAntialiasing);
        style().drawPrimitive(QStyle.PrimitiveElement.PE_Widget, styleOption, painter, this);

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
            pen = new QPen(getColor(y));
            pen.setWidth(RESOLUTION);
            painter.setPen(pen);
            painter.drawLine(0, y, width(), y);
        }
        // Drawing Border
        //painter.setRenderHint(QPainter.RenderHint.Antialiasing, true);
        //painter.setRenderHint(QPainter.RenderHint.Antialiasing, false);

        painter.end();
    }
}
