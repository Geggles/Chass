package GUI;

import Shared.Color;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.svg.QGraphicsSvgItem;

public class Square extends QGraphicsView {
    public final int row;
    public final int column;
    public final Color color;
    public final int[] coordinates;

    private boolean selected = false;
    private boolean highlighted = false;
    private QColor backgroundColor;
    private QColor backgroundHighlightColor;
    private QCursor unselectedCursor;
    private QCursor selectedCursor;

    public Square(QWidget parent, int row, int column, Color color) {
        super(parent);
        this.row = row;
        this.column = column;
        this.color = color;
        this.coordinates = new int[]{row, column};
        setAutoFillBackground(true);
    }

    /**
     * Set the background color of this square to color
     */
    public void setBackgroundColor(QColor color){
        backgroundColor = color;
        applyBackgroundColor();
    }

    /**
     * Set the background color when highlighted of this square to color
     */
    public void setBackgroundHighlightColor(QColor color){
        backgroundHighlightColor = color;
        applyBackgroundColor();
    }

    private void applyBackgroundColor(){
        applyBackgroundColor(isSelected());
    }

    private void applyBackgroundColor(boolean highlighted){
        QPalette palette = palette();
        palette.setColor(QPalette.ColorRole.Window,
                highlighted? backgroundHighlightColor: backgroundColor);
        setPalette(palette);
    }

    public boolean isSelected(){
        return selected;
    }

    public boolean isHighlighted(){
        return highlighted;
    }

    public void setSelected(boolean state){
        selected = state;
        setCursor(selected? selectedCursor: unselectedCursor);
    }

    public void setHighlighted(boolean state){
        highlighted = state;
        applyBackgroundColor(state);
    }

    public void setUnselectedCursor(QCursor unselectedCursor) {
        this.unselectedCursor = unselectedCursor;
    }

    public void setSelectedCursor(QCursor selectedCursor) {
        this.selectedCursor = selectedCursor;
    }

    public void displayPiece(QGraphicsItemInterface item){
        QGraphicsScene scene = scene();
        scene.addItem(item);
    }

    @Override
    protected void resizeEvent(QResizeEvent event) {
        resetTransform();
        translate(width()/2, height()/2);
        fitInView(scene().itemsBoundingRect(), Qt.AspectRatioMode.KeepAspectRatio);
        // scale(Math.round(height()/27 * 0.65 * 10000.0)/10000.0,
        // Math.round(height()/27 * 0.65 * 10000.0)/10000.0);  // *0.65/27 just works...
    }
}
