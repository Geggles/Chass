package GUI;

import Shared.Color;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Board extends QWidget{
    private final Signals signals = Signals.getInstance();

    public final int size;
    public final Color color;
    public final char name;
    public final Square[][] squares;

    public final Square[] extraColumnLeft = new Square[8];
    public final Square[] extraColumnRight = new Square[8];

    private final SettingSetSignalFilter lightFilter;
    private final SettingSetSignalFilter darkFilter;
    private final SettingSetSignalFilter lightHighlightFilter;
    private final SettingSetSignalFilter darkHighlightFilter;
    private final SettingSetSignalFilter selectedCursorFilter;
    private final SettingSetSignalFilter unselectedCursorFilter;

    /**
     * @param size Size of the board. Either 4 or 8.
     * @param color The color of the player that starts out on this board (BLACK/WHITE/NONE).
     */
    public Board(QWidget parent, String objectName, int size, Color color, char name){
        super(parent);
        this.size = size;
        this.color = color;
        this.name = name;
        this.setObjectName(objectName);

        lightFilter = new SettingSetSignalFilter(
                objectName() + "-" + "lightSquareUnhighlightedBrush"
        );
        darkFilter = new SettingSetSignalFilter(
                objectName() + "-" + "darkSquareUnhighlightedBrush"
        );
        lightHighlightFilter = new SettingSetSignalFilter(
                objectName() + "-" + "lightSquareHighlightedBrush"
        );
        darkHighlightFilter = new SettingSetSignalFilter(
                objectName() + "-" + "darkSquareHighlightedBrush"
        );
        selectedCursorFilter = new SettingSetSignalFilter(
                objectName() + "-" + "selectedCursor"
        );
        unselectedCursorFilter = new SettingSetSignalFilter(
                objectName() + "-" + "unselectedCursor"
        );

        setLayout(new QGridLayout(this));
        layout().setContentsMargins(0, 0, 0, 0);
        ((QGridLayout)layout()).setSpacing(0);
        this.squares = setupSquares();
    }

    private Square[][] setupSquares(){
        Square square;
        ArrayList<Square[]> allSquares = new ArrayList<>(64);
        ArrayList<Square> squareRow;

        Color currentColor = Color.BLACK;

        for (int row = 0; row < size; row++) {
            squareRow = new ArrayList<>(8);
            currentColor = currentColor.opposite();
            for (int column = 0; column < size; column++) {
                // placeholder
                square = new Square(this, row, column, Color.NONE);
                ((QGridLayout)layout()).addWidget(square, row, column, 1, 1);
                if (column == 0){
                    square = new Square(this, row, column, color);
                    extraColumnLeft[row] = square;
                    ((QGridLayout)layout()).addWidget(square, row, column, 1, 1);
                } else if (column == 7){
                    square = new Square(this, row, column, color);
                    extraColumnRight[row] = square;
                    ((QGridLayout)layout()).addWidget(square, row, column, 1, 1);
                }
                square = new Square(this, row, column, color);
                ((QGridLayout)layout()).addWidget(square, row, column, 1, 1);
                (currentColor==Color.WHITE? lightFilter: darkFilter)
                        .addListener(square, "setBackgroundUnhighlightedBrush", QBrush.class);
                (currentColor==Color.WHITE? lightHighlightFilter: darkHighlightFilter)
                        .addListener(square, "setBackgroundHighlightedBrush", QBrush.class);
                selectedCursorFilter.addListener(square, "setSelectedCursor", QCursor.class);
                unselectedCursorFilter.addListener(square, "setUnselectedCursor", QCursor.class);
                squareRow.add(square);
                currentColor = currentColor.opposite();
            }
            allSquares.add(squareRow.toArray(new Square[size]));
        }
        return allSquares.toArray(new Square[size][]);
    }

    /**
     * Keep everything square.
     * @param event
     */
    @Override
    protected void resizeEvent(QResizeEvent event) {
        if (height() < width()) {
            resize(height(), height());
        }else {
            resize(width(), width());
        }
    }

    @Override
    protected void enterEvent(QEvent event) {
        signals.boardSelected.emit(this);
        event.ignore();
    }

    @Override
    protected void leaveEvent(QEvent event) {
        signals.boardDeselected.emit(this);
        event.ignore();
    }

    @Override
    protected void wheelEvent(QWheelEvent wheelEvent) {
        signals.boardScrolled.emit(wheelEvent.delta() > 0);
    }
}
