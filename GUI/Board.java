package GUI;

import Miscellaneous.Color;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Board extends QWidget{
    private final Signals signals = Signals.getInstance();

    private final int size;
    private final Color color;
    private HashMap<ArrayList<Integer>, Square> squares = new HashMap<>(64);
    private HashMap<ArrayList<Integer>, Piece> pieces = new HashMap<>(64);
    /**
     * @param size Size of the board. Either 4 or 8.
     * @param color The color of the player that starts out on this board (BLACK/WHITE/NONE).
     */
    public Board(QWidget parent, String objectName, int size, Color color){
        super(parent);
        this.setObjectName(objectName);
        setLayout(new QGridLayout(this));
        this.size = size;
        this.color = color;
        setupSquares();
        if (color != Color.NONE) setupPieces();
    }

    private void setupSquares(){
        Color squareColor = Color.WHITE;

        for (int column=0; column<size; column++){
            for (int row=0; row<size; row++){
                Square newSquare = new Square(this, squareColor);
                squareColor = squareColor.opposite();
                ArrayList<Integer> coordinates = new ArrayList<>(2);
                coordinates.add(row);
                coordinates.add(column);
                this.squares.put(coordinates, newSquare);
                ((QGridLayout)layout()).addWidget(newSquare, row, column, 1, 1);
            }
        }
    }

    private void setupPieces(){
        int baseRow = color == Color.WHITE ? 7: 0;
        int pawnRow = color == Color.WHITE ? 6: 1;

        for (int column=0; column<8; column++){
            addPiece("Pawn", pawnRow, column);
        }
        addPiece("Rook", baseRow, 0);
        addPiece("Knight", baseRow, 1);
        addPiece("Bishop", baseRow, 2);
        addPiece("Queen", baseRow, 3);
        addPiece("King", baseRow, 4);
        addPiece("Bishop", baseRow, 5);
        addPiece("Knight", baseRow, 6);
        addPiece("Rook", baseRow, 7);
    }

    private void addPiece(String pieceName, int row, int column){
        Piece piece = new Piece(null);
        ArrayList<Integer> coordinates = new ArrayList<>(2);
        coordinates.add(row);
        coordinates.add(column);
        this.pieces.put(coordinates, piece);
        ((QGridLayout)layout()).addWidget(piece, row, column, 1, 1);

        String svgKey = objectName() + "-" + (color == Color.WHITE ? "white": "black") + pieceName;
        SettingSetSignalFilter filter = new SettingSetSignalFilter(
                piece, "setIcon", svgKey, String.class);
        signals.settingSet.connect(filter, "listen(String, Object)");
    }

    public Piece getPiece(int row, int column){
        ArrayList<Integer> coordinates = new ArrayList<>(2);
        coordinates.add(row);
        coordinates.add(column);
        return getPiece(coordinates);
    }

    public Piece getPiece(ArrayList<Integer> coordinates){
        return pieces.get(coordinates);
    }

    @Override
    protected void mousePressEvent(QMouseEvent arg__1) {
        System.out.print(this);
    }
}
