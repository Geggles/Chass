package Game;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    private HashBiMap<Square, Piece> state = HashBiMap.create(32);
    private HashBiMap<Square, List<Integer>> squares = HashBiMap.create(64);
    private static final HashBiMap<List<Integer>, String> squareNames = HashBiMap.create(64);

    static {
        String names = "abcdefgh";
        for (int row = 0; row < 8; row++)
            for (int column = 0; column < 8; column++) {
                ArrayList<Integer> coordinates = new ArrayList<>();
                coordinates.add(row);
                coordinates.add(column);
                squareNames.put(coordinates, names.charAt(column) + Integer.toString(row + 1));
            }
    }

    public final Color color;

    public Board(Color color) {
        this.color = color;
        setupSquares();
        if (color != Color.NONE) {
            setupPieces();
        }
    }

    private void setupSquares() {
        Square square;
        int[] coordinate;
        Color currentColor = Color.BLACK;
        for (int row = 0; row < 8; row++)
            for (int column = 0; column < 8; column++) {
                square = new Square(currentColor, this);
                ArrayList<Integer> coordinates = new ArrayList<>();
                coordinates.add(row);
                coordinates.add(column);
                addSquareAt(coordinates, square);
                currentColor = currentColor.opposite();
            }
    }

    private void setupPieces() {
        //pawns
        int row; // row that pawns start out on

        row = 1;
        if (color == Color.BLACK)row = 6;

        for (int column = 0; column < 8; column++) {
            setPiece(row, column, new Piece(Value.PAWN, color));
        }

        //other pieces
        row = 0;
        if (color == Color.BLACK)row = 7;

        setPiece(row, 0, new Piece(Value.ROOK, color));
        setPiece(row, 1, new Piece(Value.KNIGHT, color));
        setPiece(row, 2, new Piece(Value.BISHOP, color));
        setPiece(row, 3, new Piece(Value.QUEEN, color));
        setPiece(row, 4, new Piece(Value.KING, color));
        setPiece(row, 5, new Piece(Value.BISHOP, color));
        setPiece(row, 6, new Piece(Value.KNIGHT, color));
        setPiece(row, 7, new Piece(Value.ROOK, color));

    }

    /**
     * Check whether the piece on 'square' is pinned or not.
     */
    public boolean isPinned(Square square) {
        Piece piece = getPiece(square);
        if (piece == null || piece.getColor() == color) return false;
        for (Piece p :
                getPieces()) {
            if (p.value == Value.KING && p.getColor() != piece.getColor()) {
                Square kingSquare = getSquare(p);
                removePiece(piece);
                boolean result = isUnderAttack(kingSquare, piece.getColor());
                setPiece(square, piece);
                return result;
            }
        }
        throw new IllegalArgumentException("King has fallen over board!");
    }
    /**
     * Check whether king is in check
     * */
    public boolean check(){
        Square kingSquare = getSquare(getPieces(Value.KING)[0]);
        return isUnderAttack(kingSquare, color.opposite());
    }

    private void addSquareAt(List<Integer> coordinates, Square square) {
        squares.inverse().put(coordinates, square);
    }

    /**
     * @param piece
     * @return Array of squares that the given piece can move to. Regardless of validity of move.
     */
    public Square[] canGoTo(Piece piece) {
        ArrayList<Square> result = new ArrayList<>(1);
        Square testSquare;
        Square square = getSquare(piece);
        Color player = piece.getColor();
        ArrayList<Integer> coordinates = (ArrayList<Integer>) getCoordinates(square);
        int row = coordinates.get(0);
        int column = coordinates.get(1);
        if (piece.value == Value.PAWN) {
            int direction = 1;
            int startRow = 1;
            if (player == Color.BLACK) {
                direction = -1;
                startRow = 6;
            }
            testSquare = getSquare(row + direction, column);
            if (getPiece(testSquare) == null){
                result.add(testSquare);
                if (row == startRow){
                    testSquare = getSquare(row + 2*direction, column);
                    if(getPiece(testSquare) == null){
                        result.add(testSquare);
                    }
                }
            }
        }else if (piece.value == Value.KING){
            int baseRow = 0;
            if (piece.getColor() == Color.BLACK) baseRow = 7;
            if (row == baseRow && column == 4){
                if (!check()){
                    testSquare = getSquare(row, column-2);
                    if (getPiece(testSquare) == null &&
                            !isUnderAttack(testSquare, color.opposite()) &&
                            !isUnderAttack(getSquare(row, column-1), color.opposite())){
                        result.add(testSquare);
                    }
                    testSquare = getSquare(row, column+2);
                    if (getPiece(testSquare) == null &&
                            !isUnderAttack(testSquare, color.opposite()) &&
                            !isUnderAttack(getSquare(row, column+1), color.opposite())){
                        result.add(testSquare);
                    }
                }
            }
        }
        return attacksSquares(piece);
    }

    public Square[] attacksSquares(Piece piece) {
        ArrayList<Square> result = new ArrayList<>(0);
        Square square = getSquare(piece);
        Color player = piece.getColor();
        ArrayList<Integer> coordinates = (ArrayList<Integer>) getCoordinates(square);
        int row = coordinates.get(0);
        int column = coordinates.get(1);
        switch (piece.value) {
            case PAWN: {
                int direction = 1;
                if (player == Color.BLACK) direction = -1;
                row += direction;
                result.add(getSquare(row, column + 1));
                result.add(getSquare(row, column - 1));
                break;
            }
            case KNIGHT: {
                result.add(getSquare(row + 1, column + 2));
                result.add(getSquare(row + 1, column - 2));
                result.add(getSquare(row + 2, column + 1));
                result.add(getSquare(row + 2, column - 1));
                result.add(getSquare(row - 1, column + 2));
                result.add(getSquare(row - 1, column - 2));
                result.add(getSquare(row - 2, column + 1));
                result.add(getSquare(row - 2, column - 1));
                break;
            }
            case KING: {
                result.add(getSquare(row + 1, column - 1));
                result.add(getSquare(row + 1, column));
                result.add(getSquare(row + 1, column + 1));
                result.add(getSquare(row, column - 1));
                result.add(getSquare(row, column + 1));
                result.add(getSquare(row - 1, column - 1));
                result.add(getSquare(row - 1, column));
                result.add(getSquare(row - 1, column + 1));
                break;
            }
            default: {
                int searchRow = row;
                int searchColumn = column;
                switch (piece.value) {
                    case BISHOP: {
                        searchRow = row;
                        searchColumn = column;
                        while (++searchColumn != column && ++searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (++searchColumn != column && --searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column && ++searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column && --searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        break;
                    }
                    case ROOK: {
                        searchRow = row;
                        searchColumn = column;
                        while (++searchRow != row) result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchRow != row) result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column)
                            result.add(getSquare(searchRow, searchColumn));
                        break;
                    }
                    case QUEEN: {
                        searchRow = row;
                        searchColumn = column;
                        while (++searchColumn != column && ++searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (++searchColumn != column && --searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column && ++searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column && --searchRow != row)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (++searchRow != row) result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchRow != row) result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column)
                            result.add(getSquare(searchRow, searchColumn));
                        searchRow = row;
                        searchColumn = column;
                        while (--searchColumn != column)
                            result.add(getSquare(searchRow, searchColumn));
                        break;
                    }
                }
                return result.toArray(new Square[result.size()]);
            }
        }
        return result.toArray(new Square[result.size()]);
    }

    public boolean isUnderAttack(Square square, Color color) {
        Board board = square.board;
        ArrayList<Integer> coordinates = (ArrayList<Integer>)board.getCoordinates(square);
        int row = coordinates.get(0);
        int column = coordinates.get(1);
        int searchRow;
        int searchColumn;
        int result; /*Pawn*/
        int direction = +1;
        if (color == Color.BLACK) direction = -1;
        result = testSquare(row + direction, column - 1, color, Value.PAWN);
        if (result == 1) return true;
        result = testSquare(row + direction, column + 1, color, Value.PAWN);
        if (result == 1) return true; /*Bishop*/
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && ++searchRow != row) {
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && --searchRow != row) {
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && ++searchRow != row) {
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && --searchRow != row) {
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Rook
        searchRow = row;
        searchColumn = column;
        while (++searchRow != row) {
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchRow != row) {
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column) {
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column) {
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Knight
        if (testSquare(row + 1, column + 2, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row + 1, column - 2, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row + 2, column + 1, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row + 2, column - 1, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 1, column + 2, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 1, column - 2, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 2, column + 1, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 2, column - 1, color, Value.KNIGHT) == 1) return true;

        return false;
    }

    public Square getSquare(Piece piece) {
        return state.inverse().get(piece);
    }

    public Square getSquare(List<Integer> coordinates) {
        int row = coordinates.get(0);
        int column = coordinates.get(1);
        if (color == Color.NONE) row %= 8;
        column %= 8;
        ArrayList<Integer> newCoordinates = new ArrayList<>();
        newCoordinates.add(row);
        newCoordinates.add(column);
        return squares.inverse().get(newCoordinates);
    }

    public Square getSquare(int row, int column) {
        ArrayList<Integer> coordinates = new ArrayList<>();
        coordinates.add(row);
        coordinates.add(column);
        return getSquare(coordinates);
    }

    public Square getSquare(String squareName) {
        return getSquare(getCoordinates(squareName));
    }


    public Piece getPiece(Square square) {
        return state.get(square);
    }

    public Piece getPiece(List<Integer> coordinates) {
        return getPiece(getSquare(coordinates));
    }

    public Piece getPiece(int row, int column) {
        return getPiece(getSquare(row, column));
    }

    public Piece getPiece(String squareName) {
        return getPiece(getSquare(squareName));
    }


    public List<Integer> getCoordinates(Square square) {
        return squares.get(square);
    }

    public List<Integer> getCoordinates(Piece piece) {
        return getCoordinates(state.inverse().get(piece));
    }

    public List<Integer> getCoordinates(int row, int column) {
        return getCoordinates(getSquare(row, column));
    }

    public static List<Integer> getCoordinates(String squareName) {
        return squareNames.inverse().get(squareName);
    }


    public void setPiece(Square square, Piece piece) {
        state.put(square, piece);
    }

    public void setPiece(List<Integer> coordinates, Piece piece) {
        setPiece(getSquare(coordinates), piece);
    }

    public void setPiece(int row, int column, Piece piece) {
        setPiece(getSquare(row, column), piece);
    }

    public void setPiece(String squareName, Piece piece) {
        setPiece(getSquare(squareName), piece);
    }


    public void removePiece(Square square) {
        state.remove(square);
    }

    public void removePiece(Piece piece) {
        removePiece(getSquare(piece));
    }

    public void removePiece(List<Integer> coordinates) {
        removePiece(getSquare(coordinates));
    }

    public void removePiece(int row, int column) {
        removePiece(getSquare(row, column));
    }

    public void removePiece(String squareName) {
        removePiece(getSquare(squareName));
    }


    public Piece popPiece(Square square) {
        Piece popped = state.get(square);
        removePiece(square);
        return popped;
    }

    public Piece popPiece(Piece piece) {
        return popPiece(getSquare(piece));
    }

    public Piece popPiece(List<Integer> coordinates) {
        return popPiece(getSquare(coordinates));
    }

    public Piece popPiece(int row, int column) {
        return popPiece(getSquare(row, column));
    }

    public Piece popPiece(String squareName) {
        return popPiece(getSquare(squareName));
    }


    public Piece replacePiece(Square replace, Piece with) {
        Piece popped = popPiece(replace);
        setPiece(replace, with);
        return popped;
    }

    public Piece replacePiece(Piece replace, Piece with) {
        return replacePiece(getSquare(replace), with);
    }

    public Piece replacePiece(List<Integer> coordinates, Piece with) {
        return replacePiece(getSquare(coordinates), with);
    }

    public Piece replacePiece(int row, int column, Piece with) {
        return replacePiece(getSquare(row, column), with);
    }

    public Piece replacePiece(String squareName, Piece with) {
        return replacePiece(getSquare(squareName), with);
    }


    public String getSquareName(Square square) {
        return getSquareName(getCoordinates(square));
    }

    public static String getSquareName(List<Integer> coordinates) {
        return squareNames.get(coordinates);
    }

    public static String getSquareName(int row, int column) {
        ArrayList<Integer> coordinates = new ArrayList<>();
        coordinates.add(row);
        coordinates.add(column);
        return squareNames.get(coordinates);
    }


    /**
     * Check if square matches arguments
     * return 1 if so
     * return 0 if not
     * return -1 if the is no such square
     * return 2 if there is no piece on the square
     */
    public int testSquare(Square square, Color color, Value value) {
        if (square == null) return -1;
        Piece piece = square.board.getPiece(square);
        if (piece == null) return 2;
        if (piece.getColor() != color) return 0;
        if (piece.value != null && piece.value != value) return 0;
        return 1;
    }

    public int testSquare(List<Integer> coordinates, Color color, Value value) {
        return testSquare(getSquare(coordinates.get(0), coordinates.get(1)), color, value);
    }

    public int testSquare(int row, int column, Color color, Value value) {
        return testSquare(getSquare(row, column), color, value);
    }

    public int testSquare(String squareName, Color color, Value value) {
        return testSquare(getSquare(squareName), color, value);
    }


    public Piece[] getPieces() {
        return state.inverse().keySet().toArray(new Piece[state.inverse().keySet().size()]);
    }

    public Piece[] getPieces(Value value) {
        return (Arrays.stream(getPieces()).filter(piece -> piece.value == value)
                .toArray(size -> new Piece[size]));
    }

}
