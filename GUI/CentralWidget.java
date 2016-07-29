package GUI;

import Shared.Color;
import Shared.Value;
import com.trolltech.qt.core.*;
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

    private final ArrayList<Square> ghostSquares = new ArrayList<>();  // slightly transparent
    private final ArrayList<Square> highlightedSquares = new ArrayList<>();
    private final ArrayList<Square> persistentlyHighlightedSquares = new ArrayList<>();
    // square player is hovering (only if that square has a piece that can move)
    private Square selectedSquare = null;
    private Board hoveredBoard = null;
    private boolean dragging = false;
    private Square hoverSquare = null;

    public boolean selectPromotingPiece = false;
    public Square queenOption = new Square(this, 0, 0, Color.NONE);
    public Square rookOption = new Square(this, 0, 1, Color.NONE);
    public Square bishopOption = new Square(this, 0, 2, Color.NONE);
    public Square knightOption = new Square(this, 0, 3, Color.NONE);

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

        ((QGridLayout)layout()).addWidget(alpha,         2, 0, 2, 4);
        ((QGridLayout)layout()).addWidget(beta,          2, 4, 2, 4);
        ((QGridLayout)layout()).addWidget(gamma,         0, 2, 2, 4);
        ((QGridLayout)layout()).addWidget(whitePrison  , 0, 0, 1, 2);
        ((QGridLayout)layout()).addWidget(whiteAirfield, 1, 0, 1, 2);
        ((QGridLayout)layout()).addWidget(blackPrison  , 0, 6, 1, 2);
        ((QGridLayout)layout()).addWidget(blackAirfield, 1, 6, 1, 2);

        queenOption.installEventFilter(this);
        rookOption.installEventFilter(this);
        bishopOption.installEventFilter(this);
        knightOption.installEventFilter(this);

        queenOption.setCursor(new QCursor(Qt.CursorShape.PointingHandCursor));
        rookOption.setCursor(new QCursor(Qt.CursorShape.PointingHandCursor));
        bishopOption.setCursor(new QCursor(Qt.CursorShape.PointingHandCursor));
        knightOption.setCursor(new QCursor(Qt.CursorShape.PointingHandCursor));

        queenOption.hide();
        rookOption.hide();
        bishopOption.hide();
        knightOption.hide();

        dragSquare.hide();
        dragSquare.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents);

        dragSquare.raise();
    }

    public boolean isDragging(){
        return dragging;
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
        } else if (height() > width()){
            resize(width(), width());
        }
        adjustPromotionWidgets();
    }

    private void adjustPromotionWidgets(){
        QSize squareButtonSize = alpha.squares[0][0].size().multiply(2);
        int margin = 0;//Math.round(squareButtonSize.width()*0.08f);
        int x = mapFromParent(pos()).x()+width()/2 - margin/2 - margin - 2*squareButtonSize.width();
        //int y = mapFromParent(pos()).y() + height()/2 - squareButtonSize.height()/2;  // center
        int y = mapFromParent(pos()).y() + height()/2 - squareButtonSize.height();

        queenOption.resize(squareButtonSize);
        rookOption.resize(squareButtonSize);
        bishopOption.resize(squareButtonSize);
        knightOption.resize(squareButtonSize);

        queenOption.move(x, y);
        rookOption.move(x+squareButtonSize.width()+margin, y);
        bishopOption.move(x+2*squareButtonSize.width()+2*margin, y);
        knightOption.move(x+3*squareButtonSize.width()+3*margin, y);
    }

    public Board getBoard(Character name){
        switch (name){
            case 'A': return  alpha;
            case 'B': return  beta;
            case 'C': return  gamma;
        }
        return null;
    }

    @Override
    public boolean eventFilter(QObject target, QEvent event) {
        Square square;
        switch (event.type()){
            case Enter:
                if (target instanceof Square) {
                    if (!selectPromotingPiece) {
                        square = (Square) target;
                        Board board = (Board) square.parentWidget();
                        signals.squareSelected.emit(square);
                        if (board != hoveredBoard) {
                            hoveredBoard = board;
                        }
                        return true;
                    } else if (((Square) target).parentWidget() == this){
                        // promotionSquare
                        signals.promotionEnter.emit(
                                new Value[]{Value.QUEEN, Value.ROOK, Value.BISHOP, Value.KNIGHT}
                                        [((Square) target).column]);
                        return true;
                    }
                }
                break;
            case Leave:
                if (target instanceof Square){
                    if (!selectPromotingPiece) {
                        square = (Square) target;
                        unHighlightAllSquares();
                        signals.squareDeselected.emit(square);
                        return true;
                    } else if (((Square) target).parentWidget() == this) {
                        // promotionSquare
                        signals.promotionLeave.emit();
                        return true;
                    }
                }
                break;
            case MouseButtonPress:
                if (target instanceof Square &&
                        ((QMouseEvent) event).button() == Qt.MouseButton.LeftButton){
                    if (!selectPromotingPiece) {
                        signals.pieceSelected.emit((Square) target);
                        return true;
                    } else if (((Square) target).parentWidget() == this) {
                        // promotionSquare
                        signals.promotionSelected.emit(
                                new Value[]{Value.QUEEN, Value.ROOK, Value.BISHOP, Value.KNIGHT}
                                        [((Square) target).column]);
                        return true;
                    }
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

    public void deghostifySquare(Square square){
        if (!ghostSquares.contains(square)) return;
        ghostSquares.remove(square);
        square.setSp00ky(false);
    }

    public void deghostifyAllSquares(){
        ghostSquares.forEach(square -> square.setSp00ky(false));
        ghostSquares.clear();
    }

    public void ghostifySquare(Square square){
        if (ghostSquares.contains(square)) return;
        ghostSquares.add(square);
        square.setSp00ky(true);
    }

    public Board getHoveredBoard() {
        return hoveredBoard;
    }

    @Override
    protected void mousePressEvent(QMouseEvent mouseEvent) {
        switch (mouseEvent.type()) {
            case MouseButtonPress:
                if (mouseEvent.button() == Qt.MouseButton.RightButton)
                    signals.moveCanceled.emit();
                break;
        }
    }

    @Override
    protected void moveEvent(QMoveEvent moveEvent) {
        adjustPromotionWidgets();
    }


}
