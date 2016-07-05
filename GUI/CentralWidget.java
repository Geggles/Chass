package GUI;

import Shared.Color;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
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

    private ArrayList<Square> highlightedSquares = new ArrayList<>();
    // square player is hovering (only if that square has a piece that can move)
    private Square selectedSquare = null;
    // piece the player is dragging
    private Piece selectedPiece = null;

    public CentralWidget(QWidget parent, String objectName) {
        super(parent);
        setObjectName(objectName);
        setLayout(new QGridLayout(this));
        layout().setContentsMargins(0, 0, 0, 0);
        ((QGridLayout)layout()).setSpacing(0);

        alpha = new Board(this, objectName+".alpha", 8, Color.WHITE, 'A');
        beta = new Board(this, objectName+".beta", 8, Color.BLACK, 'B');
        gamma = new Board(this, objectName+".gamma", 8, Color.NONE, 'C');
        whitePrison = new Board(this, objectName+".whitePrison", 4, Color.WHITE, 'P');
        whiteAirfield = new Board(this, objectName+".whiteAirfield", 4, Color.WHITE, 'F');
        blackPrison = new Board(this, objectName+".blackPrison", 4, Color.BLACK, 'P');
        blackAirfield = new Board(this, objectName+".blackAirfield", 4, Color.BLACK, 'F');

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
    }

    private void startDragging(){
        Board selectedBoard = (Board) selectedSquare.parentWidget();
        selectedPiece = selectedBoard.getPiece(selectedSquare.row, selectedSquare.column);
        signals.pieceSelected.emit(selectedPiece);
        selectedBoard.layout().removeWidget(selectedPiece);
        selectedPiece.setParent(this);
        selectedPiece.show();
    }

    private void stopDragging(Square destinationSquare){
        signals.destinationSelected.emit(destinationSquare);
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
                    if (selectedPiece == null) {
                        square = (Square) target;
                        signals.squareSelected.emit(square);
                        return true;
                    }
                }
                break;
            case Leave:
                if (selectedPiece == null && target instanceof Square){
                    square = (Square) target;
                    unHighlightAllSquares();
                    square.setSelected(false);
                    selectedSquare = null;
                    signals.squareDeSelected.emit(square);
                    return true;
                }
                break;
            case MouseButtonPress:
                if (((QMouseEvent) event).button() == Qt.MouseButton.LeftButton &&
                        selectedSquare != null){
                    startDragging();
                    return true;
                }
                break;
            case MouseMove:
                //System.out.println(mapFromGlobal(((QMouseEvent)event).globalPos()));
                if (selectedPiece != null){
                    QPoint pos = mapFromGlobal(((QMouseEvent)event).globalPos());
                    int x = pos.x();
                    int y = pos.y();
                    selectedPiece.move(x-selectedPiece.width()/2, y-selectedPiece.height()/2);
                }
                break;
            case MouseButtonRelease:
                if (((QMouseEvent) event).button() == Qt.MouseButton.LeftButton){
                    if(selectedPiece != null){
                        Square destinationSquare = (Square) childAt(mapFromGlobal(
                                ((QMouseEvent) event).globalPos()));
                        stopDragging(destinationSquare);
                    }
                }
        }
        return false;
    }

    public void highlightSquare(Square square){
        square.setHighlighted(true);
        highlightedSquares.add(square);
    }

    private void unHighlightAllSquares() {
        for (Square square: highlightedSquares){
            square.setHighlighted(false);
        }
        highlightedSquares.clear();
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

    public void setSelectedSquare(Square selectedSquare) {
        this.selectedSquare = selectedSquare;
    }

    public void setSelectedPiece(Piece selectedPiece) {
        this.selectedPiece = selectedPiece;
    }
}
