package GUI;

import Shared.Color;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QObject;
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
    private QBrush backgroundUnhighlightedBrush;
    private QBrush backgroundHighlightedBrush;
    private QCursor unselectedCursor;
    private QCursor selectedCursor;

    public Square(QWidget parent, int row, int column, Color color) {
        super(parent);
        this.row = row;
        this.column = column;
        this.color = color;
        this.coordinates = new int[]{row, column};

        setScene(new QGraphicsScene(this));
        setStyleSheet("border-style: none; background: transparent;");
        setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);  //necessary?
        setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff); //necessary?
    }

    @Override
    protected void mouseMoveEvent(QMouseEvent event) {
        event.ignore();
    }

    @Override
    protected void mouseReleaseEvent(QMouseEvent event) {
        event.ignore();
    }


    /**
     * Set the background color of this square to color
     */
    public void setBackgroundUnhighlightedBrush(QBrush brush){
        backgroundUnhighlightedBrush = brush;
        applyBackgroundColor();
    }

    public QBrush getBackgroundUnhighlightedBrush(){
        return backgroundUnhighlightedBrush;
    }

    /**
     * Set the background color when highlighted of this square to color
     */
    public void setBackgroundHighlightedBrush(QBrush brush){
        backgroundHighlightedBrush = brush;
        applyBackgroundColor();
    }

    public QBrush getBackgroundHighlightedBrush(){
        return backgroundHighlightedBrush;
    }

    public QBrush getBackgroundBrush(){
        return isSelected()? backgroundHighlightedBrush: backgroundUnhighlightedBrush;
    }

    private void applyBackgroundColor(){
        applyBackgroundColor(isSelected());
    }

    private void applyBackgroundColor(boolean highlighted){
/*        QPalette palette = palette();
        palette.setColor(QPalette.ColorRole.Window,
                highlighted? backgroundHighlightColor: backgroundColor);
        setPalette(palette);*/
        setBackgroundBrush(highlighted? backgroundHighlightedBrush : backgroundUnhighlightedBrush);
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
        if (!state){
            setCursor(unselectedCursor);
        }
    }

    public void setUnselectedCursor(QCursor unselectedCursor) {
        this.unselectedCursor = unselectedCursor;
    }

    public void setSelectedCursor(QCursor selectedCursor) {
        this.selectedCursor = selectedCursor;
    }

    public void display(QGraphicsSvgItem item){
        QGraphicsScene scene = scene();
        if (item == null){
            scene.clear();
            return;
        }
        scene.addItem(item);
        resizeContent();
    }

    private void resizeContent(){
        resetTransform();
        translate(width()/2, height()/2);
        fitInView(scene().itemsBoundingRect(), Qt.AspectRatioMode.KeepAspectRatio);
    }

    @Override
    protected void resizeEvent(QResizeEvent event) {
        resizeContent();
        // scale(Math.round(height()/27 * 0.65 * 10000.0)/10000.0,
        // Math.round(height()/27 * 0.65 * 10000.0)/10000.0);  // *0.65/27 just works...
    }

    public void setHovered(boolean state) {
        if (state){

        }
    }
}
