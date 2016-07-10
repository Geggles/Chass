package GUI;

import Shared.Color;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;

public class CentralWidget extends QWidget {
    private final Signals signals = Signals.getInstance();
    public final Board alpha;
    public final Board beta;
    public final Board gamma;
    public final Board whitePrison;
    public final Board whiteAirfield;
    public final Board blackPrison;
    public final Board blackAirfield;
    public final Square dragSquare = new Square(this, -1, -1, Color.NONE);

    private final EnumMap<Color, Board> prisons = new EnumMap<Color, Board>(Color.class);
    private final EnumMap<Color, Board> airfields = new EnumMap<Color, Board>(Color.class);

    private final ArrayList<Square> highlightedSquares = new ArrayList<>();
    private final ArrayList<Square> persistentlyHighlightedSquares = new ArrayList<>();
    // square player is hovering (only if that square has a piece that can move)
    private Square selectedSquare = null;
    private Board hoveredBoard = null;
    private boolean dragging = false;
    private Square hoverSquare = null;

    public QWidget choiceWidget = new QWidget(this);

    public CentralWidget(QWidget parent, String objectName) {
        super(parent);
        setObjectName(objectName);
        setLayout(new QGridLayout(this));
        layout().setContentsMargins(0, 0, 0, 0);
        ((QGridLayout)layout()).setSpacing(3);

        alpha = new Board(this, objectName+".alpha", 8, Color.WHITE, 'A');
        beta = new Board(this, objectName+".beta", 8, Color.BLACK, 'B');
        gamma = new Board(this, objectName+".gamma", 8, Color.NONE, 'C');
        whitePrison = new Board(this, objectName+".whitePrison", 4, Color.WHITE, 'P');
        whiteAirfield = new Board(this, objectName+".whiteAirfield", 4, Color.WHITE, 'F');
        blackPrison = new Board(this, objectName+".blackPrison", 4, Color.BLACK, 'P');
        blackAirfield = new Board(this, objectName+".blackAirfield", 4, Color.BLACK, 'F');

        prisons.put(Color.WHITE, whitePrison);
        prisons.put(Color.BLACK, blackPrison);

        airfields.put(Color.WHITE, whiteAirfield);
        airfields.put(Color.BLACK, blackAirfield);

        installEventFilterToSquares(alpha);
        installEventFilterToSquares(beta);
        installEventFilterToSquares(gamma);
        installEventFilterToSquares(whitePrison);
        installEventFilterToSquares(whiteAirfield);
        installEventFilterToSquares(blackPrison);
        installEventFilterToSquares(blackAirfield);

        ((QGridLayout)layout()).addWidget(alpha, 2, 0, 2, 4);
        ((QGridLayout)layout()).addWidget(beta,  2, 4, 2, 4);
        ((QGridLayout)layout()).addWidget(gamma, 0, 2, 2, 4);
        ((QGridLayout)layout()).addWidget(whitePrison  ,0,0,1,2);
        ((QGridLayout)layout()).addWidget(whiteAirfield,1,0,1,2);
        ((QGridLayout)layout()).addWidget(blackPrison  ,0,6,1,2);
        ((QGridLayout)layout()).addWidget(blackAirfield,1,6,1,2);

        dragSquare.hide();
        dragSquare.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents);

        dragSquare.raise();

        setupChoiceWidget();
    }

    private void setupChoiceWidget() {
        // TODO: 08-Jul-16
    }


    public void setDragging(boolean state){
        dragging = state;
    }

    private void installEventFilterToSquares(Board board){
        for (Square[] squareRow: board.squares) {
            for (Square square: squareRow) {
                square.installEventFilter(this);
            }
        }
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

/*    @Override
    protected void mousePressEvent(QMouseEvent event) {
        LinkedList<QWidget> widgetsAt = getWidgetsAt(event.x(), event.y());
        if (widgetsAt == null) return;
        Square dragSquare = null;
        Board dragBoard = null;
        for (QWidget widget :
                widgetsAt) {
            if (widget instanceof Piece){
                dragging = (Piece) widget;
            }else if (widget instanceof Square){
                dragSquare = (Square) widget;
            }else if (widget instanceof Board){
                dragBoard = (Board) widget;
            }
        }
        if (dragSquare != null){
            signals.squareSelected.emit(dragSquare);
        }
        System.out.println("Coords are: "+String.valueOf(new QPoint(event.x(), event.y())));
        System.out.println("Widget is: "+String.valueOf(widgetsAt));
        System.out.println("parent: "+String.valueOf(widgetsAt.get(0).parentWidget()));
    }*/

    public Board getBoard(Character name){
        switch (name){
            case 'A': return  alpha;
            case 'B': return  beta;
            case 'C': return  gamma;
        }
        return null;
    }

    @Override
    protected void mouseMoveEvent(QMouseEvent event) {
    }

    @Override
    public boolean eventFilter(QObject target, QEvent event) {
        Square square;
        switch (event.type()){
            case Enter:
                if (target instanceof Square) {
                    square = (Square) target;
                    Board board = (Board) square.parentWidget();
                    signals.squareSelected.emit(square);
                    if (board != hoveredBoard){
                        hoveredBoard = board;
                        signals.boardSelected.emit(board);
                    }
                    return true;
                }
                break;
            case Leave:
                if (target instanceof Square){
                    square = (Square) target;
                    unHighlightAllSquares();
                    deselectSquare();
                    signals.squareDeselected.emit(square);
                    return true;
                }
                break;
            case MouseButtonPress:
                if (target instanceof Square &&
                        ((QMouseEvent) event).button() == Qt.MouseButton.LeftButton){
                    signals.pieceSelected.emit((Square) target);
                    return true;
                }
                else if (((QMouseEvent) event).button() == Qt.MouseButton.RightButton){
                    signals.moveCanceled.emit();
                    QPoint pos = mapFromGlobal(((QMouseEvent) event).globalPos());
                    QWidget someWidget = childAt(pos);
                    if (someWidget != null) {
                        square = (Square) someWidget.parentWidget();
                        signals.squareSelected.emit(square);
                    }
                    return true;
                }
                break;

            case MouseMove:
                if (target instanceof Square &&
                        dragging) {
                    QPoint pos = mapFromGlobal(((QMouseEvent) event).globalPos());
                    int x = pos.x();
                    int y = pos.y();

                    QWidget someWidget = childAt(pos);
                    if (someWidget != null){
                        square = (Square) someWidget.parentWidget();
                        if (square != hoverSquare) {
                            hoverSquare = square;
                            dragSquare.resize(square.size());
                            //updateHoveredSquares(square);
                        }
                    }

                    //wrapMouseCursor((Board) ((QWidget)target).parentWidget(), x, y);

                    dragSquare.move(
                            x - dragSquare.width() / 2,
                            y - dragSquare.height() / 2);
                    return true;
                } else {
                    if (hoveredBoard != null){
                        signals.boardDeselected.emit(hoveredBoard);
                        hoveredBoard = null;
                    }
                }

                break;
            case MouseButtonRelease:
                if (target instanceof Square &&
                        dragging &&
                        ((QMouseEvent) event).button() == Qt.MouseButton.LeftButton){
                    QWidget someWidget = childAt(mapFromGlobal(((QMouseEvent) event).globalPos()));
                    signals.destinationSelected.emit((Square) someWidget.parentWidget());
                    signals.squareSelected.emit((Square) someWidget.parentWidget());
                    return true;
                }
                break;
        }
        return false;
    }

/*    private void updateHoveredSquares(Square square) {
        int row = square.row;
        int column = square.column;
        alpha.squares[row][column].setHovered(true);
        beta.squares[row][column].setHovered(true);
        gamma.squares[row][column].setHovered(true);

    }*/

    private void wrapMouseCursor(Board board, int x, int y){
        int top = board.y();
        int left = board.x();
        int right = board.x() + board.width();
        int bottom = board.y() + board.height();
        int newX = x;
        int newY = y;

        if (x > right) newX -= board.width();
        else if (x < left) newX += board.width();
        if (board == gamma){
            if (y > bottom) newY -= board.height();
            else if (y < top) newY += board.height();
        }

        QCursor.setPos(mapToGlobal(new QPoint(newX, newY)));
    }

    public void highlightSquare(Square square){
        square.setHighlighted(true);
        if (!highlightedSquares.contains(square)) highlightedSquares.add(square);
    }

    public void unHighlightAllSquares() {
        while (highlightedSquares.size() > 0)
            unHighlightSquare(highlightedSquares.get(0));
    }

    public void unPersistentlyHighlightAllSquares() {
        while (persistentlyHighlightedSquares.size() > 0)
            unPersistentlyHighlightSquare(persistentlyHighlightedSquares.get(0));
    }

    private LinkedList<QWidget> getWidgetsAt(int x, int y){
        LinkedList<QWidget> widgetsAt = new LinkedList<>();
        QWidget child = childAt(x, y);
        while (child != null){
            widgetsAt.add(child);
            child.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents);
            child = childAt(x, y);
        }
        widgetsAt.stream().forEach(widget ->
                widget.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents, false));
        return widgetsAt;
    }

    public Board getPrison(Color color) {
        return prisons.get(color);
    }

    public Board getAirfield(Color color) {
        return airfields.get(color);
    }

    public void unHighlightSquare(Square square) {
        highlightedSquares.remove(square);
        if (!persistentlyHighlightedSquares.contains(square)) square.setHighlighted(false);
    }

    public void persistentlyHighlightSquare(Square square) {
        square.setHighlighted(true);
        if (!persistentlyHighlightedSquares.contains(square))
            persistentlyHighlightedSquares.add(square);
    }

    public void unPersistentlyHighlightSquare(Square square) {
        persistentlyHighlightedSquares.remove(square);
        if (!highlightedSquares.contains(square)) square.setHighlighted(false);
    }

    public boolean isPersistentlyHighlighted(Square square) {
        return persistentlyHighlightedSquares.contains(square);
    }

    public void selectSquare(Square square){
        selectedSquare = square;
        square.setSelected(true);
    }

    public void deselectSquare(){
        if (selectedSquare == null) return;
        selectedSquare.setSelected(false);
        selectedSquare = null;
    }

    public Square getSelectedSquare(){
        return selectedSquare;
    }
}
