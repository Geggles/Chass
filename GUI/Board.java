package GUI;

import Shared.Color;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Board extends QWidget{
    private final Signals signals = Signals.getInstance();

    public final int size;
    public final Color color;
    public final char name;
    public final Square[][] squares;
    private HashMap<Square, Piece> pieces = new HashMap<>(64);
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
        setLayout(new QGridLayout(this));
        layout().setContentsMargins(0, 0, 0, 0);
        ((QGridLayout)layout()).setSpacing(0);
        this.squares = setupSquares();
        if (color != Color.NONE && size == 8) setupPieces();
    }

    private Square[][] setupSquares(){
        Square square;
        ArrayList<Square[]> allSquares = new ArrayList<>(64);
        ArrayList<Square> squareRow;
        SettingSetSignalFilter lightFilter = new SettingSetSignalFilter(
                objectName() + "-" + "lightSquareColor"
        );
        SettingSetSignalFilter darkFilter = new SettingSetSignalFilter(
                objectName() + "-" + "darkSquareColor"
        );
        SettingSetSignalFilter lightHighlightFilter = new SettingSetSignalFilter(
                objectName() + "-" + "lightSquareHighlightColor"
        );
        SettingSetSignalFilter darkHighlightFilter = new SettingSetSignalFilter(
                objectName() + "-" + "darkSquareHighlightColor"
        );
        SettingSetSignalFilter selectedCursorFilter = new SettingSetSignalFilter(
                objectName() + "-" + "selectedCursor"
        );
        SettingSetSignalFilter unselectedCursorFilter = new SettingSetSignalFilter(
                objectName() + "-" + "unselectedCursor"
        );

        Color currentColor = Color.BLACK;

        for (int row = 0; row < size; row++) {
            squareRow = new ArrayList<>(8);
            currentColor = currentColor.opposite();
            for (int column = 0; column < size; column++) {
                square = new Square(this, row, column, color);
                ((QGridLayout)layout()).addWidget(square, row, column, 1, 1);
                (currentColor==Color.WHITE? lightFilter: darkFilter)
                        .addListener(square, "setBackgroundColor", QColor.class);
                (currentColor==Color.WHITE? lightHighlightFilter: darkHighlightFilter)
                        .addListener(square, "setBackgroundHighlightColor", QColor.class);
                selectedCursorFilter.addListener(square, "setSelectedCursor", QCursor.class);
                unselectedCursorFilter.addListener(square, "setUnselectedCursor", QCursor.class);
                squareRow.add(square);
                currentColor = currentColor.opposite();
            }
            allSquares.add(squareRow.toArray(new Square[size]));
        }
        return allSquares.toArray(new Square[size][]);
    }

    private void setupPieces(){
        int baseRow = color == Color.WHITE ? 7: 0;
        int pawnRow = color == Color.WHITE ? 6: 1;

        String colorKey = objectName() + "-" + (color == Color.WHITE ? "white" : "black");

        String pawnKey = colorKey + "Pawn";
        SettingSetSignalFilter pawnFilter = new SettingSetSignalFilter(pawnKey);

        String rookKey = colorKey + "Rook";
        SettingSetSignalFilter rookFilter = new SettingSetSignalFilter(rookKey);

        String bishopKey = colorKey + "Bishop";
        SettingSetSignalFilter bishopFilter = new SettingSetSignalFilter(bishopKey);

        String knightKey = colorKey + "Knight";
        SettingSetSignalFilter knightFilter = new SettingSetSignalFilter(knightKey);

        for (int column=0; column<8; column++){
            addPiece(pawnRow, column, pawnFilter);
        }
        addPiece(baseRow, 0, rookFilter);
        addPiece(baseRow, 1, knightFilter);
        addPiece(baseRow, 2, bishopFilter);
        addPiece(baseRow, 3, "Queen");
        addPiece(baseRow, 4, "King");
        addPiece(baseRow, 5, bishopFilter);
        addPiece(baseRow, 6, knightFilter);
        addPiece(baseRow, 7, rookFilter);
    }

    private void addPiece(int row, int column, SettingSetSignalFilter filter){
        Piece piece = new Piece(null);
        this.pieces.put(squares[row][column], piece);
        ((QGridLayout)layout()).addWidget(piece, row, column, 1, 1);

        filter.addListener(piece, "setIcon", String.class);

    }

    private void addPiece(int row, int column, String pieceName) {
        String settingsKey = objectName() + "-" + (color == Color.WHITE ? "white" : "black") + pieceName;
        SettingSetSignalFilter filter = new SettingSetSignalFilter(settingsKey);

        addPiece(row, column, filter);
    }

    public Piece getPiece(int row, int column){
        return pieces.get(squares[row][column]);
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
}
