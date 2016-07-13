package Game;

import Shared.Color;
import Shared.Value;
import com.google.common.base.Strings;
import com.google.common.collect.HashBiMap;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Board {
    private HashBiMap<Square, Piece> state = HashBiMap.create(32);
    private final Square[][] squares;

    public final Color color;
    public final Character name;

    public Board(Color color, Character name) {
        this.color = color;
        this.name = name;
        this.squares = setupSquares();
        if (color != Color.NONE) {
            setupPieces();
        }
    }

    private Square[][] setupSquares() {
        Square square;
        Color currentColor = Color.BLACK;
        ArrayList<Square[]> allSquares = new ArrayList<>(64);
        ArrayList<Square> squareRow;
        for (int row = 0; row < 8; row++) {
            squareRow = new ArrayList<>(8);
            currentColor = currentColor.opposite();
            for (int column = 0; column < 8; column++) {
                square = new Square(this, currentColor, row, column);
                squareRow.add(square);
                currentColor = currentColor.opposite();
            }
            allSquares.add(squareRow.toArray(new Square[8]));
        }
        return allSquares.toArray(new Square[8][]);
    }

    private void setupPieces() {
        //pawns
        int pawnRow = 6;
        if (color == Color.BLACK)pawnRow = 1;

        for (int column = 0; column < 8; column++) {
            setPiece(pawnRow, column, new Piece(Value.PAWN, color));
        }

        //other pieces
        int baseRow = 7;
        if (color == Color.BLACK) baseRow = 0;

        setPiece(baseRow, 0, new Piece(Value.ROOK, color));
        setPiece(baseRow, 1, new Piece(Value.KNIGHT, color));
        setPiece(baseRow, 2, new Piece(Value.BISHOP, color));
        setPiece(baseRow, 3, new Piece(Value.QUEEN, color));
        setPiece(baseRow, 4, new Piece(Value.KING, color));
        setPiece(baseRow, 5, new Piece(Value.BISHOP, color));
        setPiece(baseRow, 6, new Piece(Value.KNIGHT, color));
        setPiece(baseRow, 7, new Piece(Value.ROOK, color));

    }

    /**
     * Calculate the distance between two squares.
     * <p>
     *     Squares may be of different boards. They will be treated as though being
     *     on the same board.
     *     Positive direction is up right.
     * </p>
     * @param sourceSquare The square to calculate to distance from.
     * @param destinationSquare The square to calculate to distance to.
     * @return The distance between the squares.
     */
    public static int[] calculateDistance(Square sourceSquare, Square destinationSquare){
        return new int[]{
                destinationSquare.row - sourceSquare.row,
                sourceSquare.column - destinationSquare.column
        };
    }

    /**
     * Check whether a piece is pinned
     * @param square The square to be checked for pins
     * @return Whether the piece on the input square is pinned or not.
     */
    public boolean isPinned(Square square) {
        Piece piece = getPiece(square);
        if (piece == null) return false;
        Piece king = getPieces(Value.KING, piece.getColor())[0];
        removePiece(piece);
        boolean result = inCheck();
        setPiece(square, piece);
        return result;
    }
    /**
     * Check whether king is in in check.
     * */
    public boolean inCheck(){
        Square kingSquare = getSquare(getPieces(Value.KING, color)[0]);
        return isUnderAttack(kingSquare, color.opposite());
    }

    public Square[] canGoTo(Square square){
        return canGoTo(getPiece(square));
    }

    /**
     * Calculate all squares a given piece can move to.
     * <p>
     *     Only works in respect to translation and capture.
     *     Except for Pawn and King, the result is equal to attacksSquares.
     * </p>
     * @param piece The piece whose possible moves are to be calculated.
     * @return Array of squares that the given piece can move to. Regardless of validity of move.
     */
    public Square[] canGoTo(Piece piece) {
        ArrayList<Square>result = canGoToAsArrayList(piece);
        return result.toArray(new Square[result.size()]);
    }
    private ArrayList<Square> canGoToAsArrayList(Piece piece) {
        ArrayList<Square> result = new ArrayList<>(1);
        Square testSquare;
        Square square = getSquare(piece);
        Color player = piece.getColor();
        int row = square.row;
        int column = square.column;
        if (piece.value == Value.PAWN) {
            int direction = -1;
            int startRow = 6;
            if (player == Color.BLACK) {
                direction = 1;
                startRow = 1;
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
            int baseRow = 7;
            if (piece.getColor() == Color.BLACK) baseRow = 0;
            if (row == baseRow && column == 4){
                if (!inCheck()){

                    // queen side castling
                    testSquare = getSquare(row, column-1);
                    if (getPiece(testSquare) == null &&
                            !isUnderAttack(testSquare, color.opposite())){
                        testSquare = getSquare(row, column-3);
                        if (getPiece(testSquare) == null &&
                                !isUnderAttack(testSquare, color.opposite())){
                            testSquare = getSquare(row, column-2);
                            if (getPiece(testSquare) == null &&
                                    !isUnderAttack(testSquare, color.opposite())){
                                testSquare = getSquare(row, column-4);
                                Piece testPiece = getPiece(testSquare);
                                if (testPiece != null &&
                                        testPiece.value == Value.ROOK &&
                                        testPiece.getColor() == color)
                                result.add(testSquare);
                            }
                        }
                    }

                    // king side castling
                    testSquare = getSquare(row, column+1);
                    if (getPiece(testSquare) == null &&
                            !isUnderAttack(testSquare, color.opposite())){
                        testSquare = getSquare(row, column+2);
                        if (getPiece(testSquare) == null &&
                                !isUnderAttack(testSquare, color.opposite())){
                            testSquare = getSquare(row, column+3);
                            Piece testPiece = getPiece(testSquare);
                            if (testPiece != null &&
                                    testPiece.value == Value.ROOK &&
                                    testPiece.getColor() == color)
                            result.add(testSquare);
                        }
                    }
                }
            }
        }
        result.addAll(attacksSquaresAsArraylist(piece));
        return result;
    }

    public Square[] attacksSquares(Piece piece){
        ArrayList<Square> result = attacksSquaresAsArraylist(piece);
        return result.toArray(new Square[result.size()]);
    }

    private ArrayList<Square> attacksSquaresAsArraylist(Piece piece) {
        ArrayList<Square> result = new ArrayList<>(0);
        Square square = getSquare(piece);
        Color player = piece.getColor();
        int row = square.row;
        int column = square.column;
        switch (piece.value) {
            case PAWN: {
                int direction = -1;
                if (player == Color.BLACK) direction = 1;
                row += direction;
                addIfValidSquare(result, getSquare(row, column + 1), player);
                addIfValidSquare(result, getSquare(row, column - 1), player);
                break;
            }
            case KNIGHT: {
                addIfValidSquare(result, getSquare(row + 1, column + 2), player);
                addIfValidSquare(result, getSquare(row + 1, column - 2), player);
                addIfValidSquare(result, getSquare(row + 2, column + 1), player);
                addIfValidSquare(result, getSquare(row + 2, column - 1), player);
                addIfValidSquare(result, getSquare(row - 1, column + 2), player);
                addIfValidSquare(result, getSquare(row - 1, column - 2), player);
                addIfValidSquare(result, getSquare(row - 2, column + 1), player);
                addIfValidSquare(result, getSquare(row - 2, column - 1), player);
                break;
            }
            case KING: {
                addIfValidSquare(result, getSquare(row + 1, column - 1), player);
                addIfValidSquare(result, getSquare(row + 1, column), player);
                addIfValidSquare(result, getSquare(row + 1, column + 1), player);
                addIfValidSquare(result, getSquare(row, column - 1), player);
                addIfValidSquare(result, getSquare(row, column + 1), player);
                addIfValidSquare(result, getSquare(row - 1, column - 1), player);
                addIfValidSquare(result, getSquare(row - 1, column), player);
                addIfValidSquare(result, getSquare(row - 1, column + 1), player);
                break;
            }
            default: {
                int searchRow;
                int searchColumn;
                switch (piece.value) {
                    case BISHOP: {
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(++searchColumn) != column && wrapRow(++searchRow) != row&&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(++searchColumn) != column && wrapRow(--searchRow) != row&&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(--searchColumn) != column && wrapRow(++searchRow) != row&&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(--searchColumn) != column && wrapRow(--searchRow) != row&&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        break;
                    }
                    case ROOK: {
                        searchRow = row;
                        searchColumn = column;
                        while (wrapRow(++searchRow) != row &&
                                addIfValidSquare(result,
                                                 getSquare(searchRow, searchColumn),
                                                 player));
                        searchRow = row;
                        searchColumn = column;
                        while (wrapRow(--searchRow) != row &&
                                addIfValidSquare(result,
                                                 getSquare(searchRow, searchColumn),
                                                 player));
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(--searchColumn) != column &&
                                addIfValidSquare(result,
                                                 getSquare(searchRow, searchColumn),
                                                 player))
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(++searchColumn) != column &&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        break;
                    }
                    case QUEEN: {
                        // diagonal bottom right
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(++searchColumn) != column && ++searchRow != row &&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        // diagonal top right
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(++searchColumn) != column && --searchRow != row &&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        // diagonal bottom left
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(--searchColumn) != column && ++searchRow != row &&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        // diagonal top left
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(--searchColumn) != column && --searchRow != row &&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        // straight bottom
                        searchRow = row;
                        searchColumn = column;
                        while (wrapRow(++searchRow) != row &&
                                addIfValidSquare(result,
                                                 getSquare(searchRow, searchColumn),
                                             player));
                        // straight top
                        searchRow = row;
                        searchColumn = column;
                        while (wrapRow(--searchRow) != row &&
                                addIfValidSquare(result,
                                                 getSquare(searchRow, searchColumn),
                                             player));
                        // straight left
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(--searchColumn) != column &&
                            addIfValidSquare(result,
                                             getSquare(searchRow, searchColumn),
                                             player));
                        // straight right
                        searchRow = row;
                        searchColumn = column;
                        while (wrapColumn(++searchColumn) != column &&
                                addIfValidSquare(result,
                                        getSquare(searchRow, searchColumn),
                                        player));
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean addIfValidSquare(ArrayList<Square> result, Square square, Color player) {
        if (square == null) return false;
        Piece piece = getPiece(square);
        if (piece != null) {
            if (piece.getColor() != player &&
                    !result.contains(square)) result.add(square);
            return false;  // to stop loop
        }
        if (!result.contains(square)) result.add(square);
        return true;
    }

    public boolean isUnderAttack(Square square, Color color) {
        Board board = square.board;
        int row = square.row;
        int column = square.column;
        int searchRow;
        int searchColumn;
        int result;

        int direction = +1;
        if (color == Color.BLACK) direction = -1;
        result = testSquare(row + direction, column - 1, color, Value.PAWN);
        if (result == 1) return true;
        result = testSquare(row + direction, column + 1, color, Value.PAWN);
        if (result == 1) return true;
        searchRow = row;
        searchColumn = column;

        while (++searchColumn != column && ++searchRow != row) {
            searchColumn = wrapColumn(searchColumn);
            searchRow = wrapRow(searchRow);
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            //if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && --searchRow != row) {
            searchColumn = wrapColumn(searchColumn);
            searchRow = wrapRow(searchRow);
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            //if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && ++searchRow != row) {
            searchColumn = wrapColumn(searchColumn);
            searchRow = wrapRow(searchRow);
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            //if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && --searchRow != row) {
            searchColumn = wrapColumn(searchColumn);
            searchRow = wrapRow(searchRow);
            result = testSquare(searchRow, searchColumn, color, Value.BISHOP);
            if (result == 1) return true;
            //if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Rook
        searchRow = row;
        searchColumn = column;
        while (++searchRow != row) {
            searchRow = wrapRow(searchRow);
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            //if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchRow != row) {
            searchRow = wrapRow(searchRow);
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            //if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column) {
            searchColumn = wrapColumn(searchColumn);
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            //if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column) {
            searchColumn = wrapColumn(searchColumn);
            result = testSquare(searchRow, searchColumn, color, Value.ROOK);
            if (result == 1) return true;
            //if (result <= 0) break;
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
        return testSquare(row - 2, column - 1, color, Value.KNIGHT) == 1;

    }

    public Square getSquare(Piece piece) {
        return state.inverse().get(piece);
    }

    public Square getSquare(int[] coordinates) {
        int row = coordinates[0];
        int column = coordinates[1];
        return getSquare(row, column);
    }
    public Square getSquare(int row, int column) {
        row = wrapRow(row);
        column = wrapColumn(column);
        try {
            return squares[row][column];
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public Square getSquare(String squareName) {
        return getSquare(getCoordinates(squareName));
    }


    public Piece getPiece(Square square) {
        return state.get(square);
    }

    public Piece getPiece(int[] coordinates) {
        return getPiece(getSquare(coordinates));
    }

    public Piece getPiece(int row, int column) {
        return getPiece(getSquare(row, column));
    }

    public Piece getPiece(String squareName) {
        return getPiece(getSquare(squareName));
    }

    public int[] getCoordinates(Piece piece) {
        return state.inverse().get(piece).coordinates;
    }

    public static int[] getCoordinates(String squareName) {
        squareName = squareName.toLowerCase();
        int row = 8-Integer.parseInt(squareName.substring(1));
        int column = 0;
        switch (squareName.charAt(0)){
            case 'a':
                column = 0;
                break;
            case 'b':
                column = 1;
                break;
            case 'c':
                column = 2;
                break;
            case 'd':
                column = 3;
                break;
            case 'e':
                column = 4;
                break;
            case 'f':
                column = 5;
                break;
            case 'g':
                column = 6;
                break;
            case 'h':
                column = 7;
                break;
        }
        return new int[]{row, column};
    }


    public void setPiece(Square square, Piece piece) {
        state.put(square, piece);
    }

    public void setPiece(int[] coordinates, Piece piece) {
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

    public void removePiece(int[] coordinates) {
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

    public Piece popPiece(int[] coordinates) {
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

    public Piece replacePiece(int[] coordinates, Piece with) {
        return replacePiece(getSquare(coordinates), with);
    }

    public Piece replacePiece(int row, int column, Piece with) {
        return replacePiece(getSquare(row, column), with);
    }

    public Piece replacePiece(String squareName, Piece with) {
        return replacePiece(getSquare(squareName), with);
    }


    public static String getSquareName(Square square) {
        return getSquareName(square.coordinates);
    }

    public static String getSquareName(int[] coordinates) {
        int row = coordinates[0];
        int column = coordinates[1];
        return getSquareName(row, column);
    }

    public static String getSquareName(int row, int column) {
        return "abcdefgh".substring(column, column+1) + Integer.toString(8-row);
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

    public int testSquare(int[] coordinates, Color color, Value value) {
        return testSquare(getSquare(coordinates[0], coordinates[1]), color, value);
    }

    public int testSquare(int row, int column, Color color, Value value) {
        return testSquare(getSquare(row, column), color, value);
    }

    public int testSquare(String squareName, Color color, Value value) {
        return testSquare(getSquare(squareName), color, value);
    }


    public Piece[] getPieces(@Nullable Value value, @Nullable Color player) {
        List<Piece> pieceList =
                state
                .values()
                .stream()
                .filter(piece -> piece != null)
                .collect(Collectors.toList());
        if (value == null && player == null) {
            //trivial case
            return pieceList.toArray(new Piece[pieceList.size()]);
        }
        return  (pieceList
                .stream()
                .filter(piece -> this.testPiece(piece, value, player))
                .toArray(size -> new Piece[size]));
    }

    private boolean testPiece(Piece piece, @Nullable Value value, @Nullable Color player){
        return !(value != null && piece.value != value) &&
               !(player != null && piece.getColor() != player);
    }

    public int wrapRow(int row){
        if (color == Color.NONE) {
            row %= 8;
            if (row<0) row+= 8;  // stupid java % operator not working like in python
        }
        return row;
    }

    public int wrapColumn(int column){
        column %= 8;
        if (column<0) column += 8;
        return column;
    }

}
