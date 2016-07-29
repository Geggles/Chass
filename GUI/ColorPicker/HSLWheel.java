package GUI.ColorPicker;

import com.trolltech.qt.QPair;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;

public class HSLWheel extends QWidget{
    public double lightness = 0.5d;
    public double alpha = 1.0d;
    private QTimer paintTimer = new QTimer(this);
    private boolean fullColors = true;

    public Signal1<QColor> colorSelected = new Signal1<>();

    public HSLWheel(QWidget parent) {
        super(parent);
        setMinimumSize(50, 50);
        paintTimer.timeout.connect(this, "updateColors()");
        paintTimer.setInterval(750);
        paintTimer.setSingleShot(true);
    }

    private void updateColors(){
        fullColors = true;
        update();
    }

    public void setLightness(double lightness){
        this.lightness = lightness;
        waitToUpdate(100);
        update();
    }

    public void setLightness(QColor color){
        this.lightness = color.lightnessF();
        waitToUpdate(100);
        update();
    }

    public void setAlpha(double alpha){
        this.alpha = alpha;
        waitToUpdate(100);
        update();
    }

    public void setAlpha(QColor color){
        this.alpha = color.alphaF();
        waitToUpdate(100);
        update();
    }

    private void waitToUpdate(int ms){
        fullColors = false;
        paintTimer.start(ms);
    }

    private void waitToUpdate(){
        fullColors = false;
        paintTimer.start();
    }

    public boolean isInCirce(int x, int y, int cx, int cy, int r){
        int dx = Math.abs(x-cx);
        if (dx > r) return false;
        int dy = Math.abs(y-cy);
        if (dy > r) return false;
        if (dx+dy <= r) return true;
        return dx*dx + dy*dy <= r*r;
    }

    public QColor getColor(int x, int y){
        int cx = rect().center().x();
        int cy = rect().center().y();
        double maxRadius = width() > height() ? height() / 2 : width() / 2;
        maxRadius -= 5;

        double polRadius;
        double polAngle;
        double hue;
        double saturation;

        polRadius = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
        if (polRadius > maxRadius) polRadius = maxRadius;
        if (polRadius == 0) {
            saturation = 0;
        } else {
            saturation = polRadius / maxRadius;
        }
        polAngle = Math.atan2(y - cy, x - cx);
        if (polAngle < 0) polAngle = polAngle * -1;
        else polAngle = 2 * Math.PI - polAngle;
        if (polAngle == 0) {
            hue = 0;
        } else {
            hue = polAngle / (2 * Math.PI);
        }
        return QColor.fromHslF(hue, saturation, lightness, alpha);
    }

    @Override
    protected void mousePressEvent(QMouseEvent event) {
        int x = event.pos().x();
        int y = event.pos().y();
        colorSelected.emit(getColor(x, y));
    }

    @Override
    protected void mouseMoveEvent(QMouseEvent event) {
        int x = event.pos().x();
        int y = event.pos().y();
        colorSelected.emit(getColor(x, y));
    }

    @Override
    protected void resizeEvent(QResizeEvent event) {
        waitToUpdate();
    }

    @Override
    protected void paintEvent(QPaintEvent event) {
        QStyleOption styleOption = new QStyleOption();
        styleOption.initFrom(this);
        QPainter painter = new QPainter(this);
        style().drawPrimitive(QStyle.PrimitiveElement.PE_Widget, styleOption, painter, this);
        painter.setRenderHint(QPainter.RenderHint.HighQualityAntialiasing);

        QPoint center = rect().center();
        double maxRadius = width() > height() ? height() / 2 : width() / 2;
        maxRadius -= 5;
        int cx = center.x();
        int cy = center.y();
        QPen pen;

        int RESOLUTION;  // 1 is best quality
        if (fullColors) {
            RESOLUTION = 1;
        } else {
            if (maxRadius < 30) RESOLUTION = 1;
            else if (maxRadius < 90) RESOLUTION = 3;
            else if (maxRadius < 160) RESOLUTION = 5;
            else RESOLUTION = 7;
        }

        double polRadius;
        double polAngle;
        double hue;
        double saturation;

        boolean precise = false;

        for (int x = 0; x < width(); x += RESOLUTION) {
            for (int y = 0; y < height(); y += RESOLUTION) {
                polRadius = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));


                if (Math.abs(maxRadius-polRadius) < 0.7071067812*RESOLUTION) precise = true;
                else precise = false;

                if (precise){
                    painter.setRenderHint(QPainter.RenderHint.Antialiasing, true);
                    for (int lx=x-RESOLUTION/2; lx<=x+RESOLUTION/2; lx++){
                        for (int ly=y-RESOLUTION/2; ly<=y+RESOLUTION/2; ly++){
                            if (isInCirce(lx, ly, cx, cy, (int)Math.round(maxRadius))){
                                pen = new QPen(getColor(lx, ly));
                                pen.setWidth(1);
                                painter.setPen(pen);
                                painter.drawPoint(lx, ly);
                            }
                        }
                    }
                    painter.setRenderHint(QPainter.RenderHint.Antialiasing, false);
                    continue;
                }


                if (polRadius >= maxRadius) continue;
                if (polRadius == 0) {
                    saturation = 0;
                } else {
                    saturation = polRadius / maxRadius;
                }
                polAngle = Math.atan2(y - cy, x - cx);
                if (polAngle < 0) polAngle = polAngle * -1;
                else polAngle = 2 * Math.PI - polAngle;
                if (polAngle == 0) {
                    hue = 0;
                } else {
                    hue = polAngle / (2 * Math.PI);
                }
                painter.fillRect(x-RESOLUTION/2, y-RESOLUTION/2, RESOLUTION, RESOLUTION,
                        QColor.fromHslF(hue, saturation, lightness, alpha));
            }
        }
        // Drawing Border

/*        QConicalGradient borderGradient = new QConicalGradient(cx, cy, 0d);
        ArrayList<QPair<Double, QColor>> stops = new ArrayList<>(2);
        stops.add(new QPair<>(0d, QColor.fromHslF(0d, 1, lightness, 1)));
        double STEPS = 30;
        for (double i = STEPS; i < 360; i += STEPS){
            stops.add(new QPair<>(i/360d, QColor.fromHslF(i/360d, 1, lightness, 1)));
        }
        stops.add(new QPair<>(1d, QColor.fromHslF(1d, 1, lightness, 1)));
        borderGradient.setStops(stops);
        pen = new QPen(new QBrush(borderGradient), 1);
        pen.setWidth(fullColors? 2: RESOLUTION+2);
        pen.setWidth(1);
        painter.setPen(pen);
        painter.setRenderHint(QPainter.RenderHint.Antialiasing, true);*/
/*        painter.drawEllipse(
                cx-(int) Math.round(maxRadius)+(RESOLUTION-2)/2,
                cy-(int) Math.round(maxRadius)-(RESOLUTION)/2,
                (int) Math.round(maxRadius)*2-(RESOLUTION-2)/2,
                (int) Math.round(maxRadius)*2-(RESOLUTION-2)/2
        );*/
/*        painter.setRenderHint(QPainter.RenderHint.Antialiasing, true);
        painter.drawEllipse(cx-(int) Math.round(maxRadius)-pen.width(), cy-(int) Math.round(maxRadius)-pen.width(),
                             (int) Math.round(maxRadius)*2, (int) Math.round(maxRadius)*2);*/
        //painter.setRenderHint(QPainter.RenderHint.Antialiasing, false);

        painter.end();
    }
}
