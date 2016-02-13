package Game;

import com.google.common.collect.HashBiMap;

import java.util.ArrayList;

public abstract class Board {
    private HashBiMap<Square, Piece> state = HashBiMap.create(32);
    private HashBiMap<Square, int[]> squares = HashBiMap.create(64);
    private static final HashBiMap<int[], String> squareNames = HashBiMap.create(64);
    static
    {
        String names = "abcdefgh";
        for (int row=0; row<8; row++){
            for (int column=0; column<8; column++){
                squareNames.put(
                        new int[] {row, column},
                        names.charAt(column) + Integer.toString(row+1));
            }
        }
    }
    public final Color color;

    public Board(Color color){
        this.color = color;
        setupSquares();
    }

    private void setupSquares(){
        Square square;
        int[] coordinate;
        Color currentColor = Color.BLACK;
        for (int row=0; row<8; row++) {
            for (int column = 0; column < 8; column++) {
                square = new Square(currentColor, this);
                coordinate = new int[] {row, column};
                addSquareAt(coordinate, square);
                currentColor = currentColor.opposite();
            }
        }
    }

    protected void initializeSquares(int size){
        squares = HashBiMap.create(size);
    }

    protected void addSquareAt(int[] coordinates, Square square){
        squares.inverse().put(coordinates, square);
    }

    protected void addCoordinatesOf(int[] coordinates, Square square){
        squares.put(square, coordinates);
    }

    public boolean isUnderAttack(Square square, Color color) {
        Board board = square.board;
        int[] coordinates = board.getCoordinates(square);
        int row = coordinates[0];
        int column = coordinates[1];
        int searchRow;
        int searchColumn;
        int result;

        //Pawn
        int direction = +1;
        if (color == Color.BLACK) direction = -1;
        result = testSquare(row + direction, column - 1, board, color, Value.PAWN);
        if (result == 1) return true;
        result = testSquare(row + direction, column + 1, board, color, Value.PAWN);
        if (result == 1) return true;

        //Bishop
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && ++searchRow != row) {
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (++searchColumn != column && --searchRow != row) {
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && ++searchRow != row) {
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column && --searchRow != row) {
            result = testSquare(searchRow, searchColumn, board, color, Value.BISHOP);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Rook
        searchRow = row;
        searchColumn = column;
        while (++searchRow != row) {
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchRow != row) {
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column) {
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }
        searchRow = row;
        searchColumn = column;
        while (--searchColumn != column) {
            result = testSquare(searchRow, searchColumn, board, color, Value.ROOK);
            if (result == 1) return true;
            if (result <= 0) break;
            result = testSquare(searchRow, searchColumn, board, color, Value.QUEEN);
            if (result == 1) return true;
            if (result <= 0) break;
        }

        //Knight
        if (testSquare(row + 1, column + 2, board, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row + 1, column - 2, board, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row + 2, column + 1, board, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row + 2, column - 1, board, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 1, column + 2, board, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 1, column - 2, board, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 2, column + 1, board, color, Value.KNIGHT) == 1) return true;
        if (testSquare(row - 2, column - 1, board, color, Value.KNIGHT) == 1) return true;

        return false;
    }

    public Square getSquare(Piece piece){
        return state.inverse().get(piece);
    }

    public Square getSquare(int[] coordinates){
        return squares.inverse().get(coordinates);
    }

    public Square getSquare(int row, int column){
        return getSquare(new int[] {row, column});
    }

    public Square getSquare(String squareName){
        return getSquare(getCoordinates(squareName));
    }


    public Piece getPiece(Square square){
        return state.get(square);
    }

    public Piece getPiece(int[] coordinates){
        return getPiece(getSquare(coordinates));
    }

    public Piece getPiece(int row, int column){
        return getPiece(getSquare(row, column));
    }

    public Piece getPiece(String squareName){
        return getPiece(getSquare(squareName));
    }


    public int[] getCoordinates(Square square){
        return squares.get(square);
    }

    public int[] getCoordinates(Piece piece){
        return getCoordinates(state.inverse().get(piece));
    }

    public int[] getCoordinates(int[] coordinates){
        return getCoordinates(getSquare(coordinates));
    }

    public int[] getCoordinates(int row, int column){
        return getCoordinates(getSquare(row, column));
    }

    public static int[] getCoordinates(String squareName){
        return squareNames.inverse().get(squareName);
    }


    public void setPiece(Square square, Piece piece){
        state.put(square, piece);
    }

    public void setPiece(int[] coordinates, Piece piece){
        setPiece(getSquare(coordinates), piece);
    }

    public void setPiece(int row, int column, Piece piece){
        setPiece(getSquare(row, column), piece);
    }

    public void setPiece(String squareName, Piece piece){
        setPiece(getSquare(squareName), piece);
    }


    public void removePiece(Square square){
        state.remove(square);
    }

    public void removePiece(Piece piece){
        removePiece(getSquare(piece));
    }

    public void removePiece(int[] coordinates){
        removePiece(getSquare(coordinates));
    }

    public void removePiece(int row, int column){
        removePiece(getSquare(row, column));
    }

    public void removePiece(String squareName){
        removePiece(getSquare(squareName));
    }


    public Piece popPiece(Square square){
        Piece popped = state.get(square);
        removePiece(square);
        return popped;
    }

    public Piece popPiece(Piece piece){
        return popPiece(getSquare(piece));
    }

    public Piece popPiece(int[] coordinates){
        return popPiece(getSquare(coordinates));
    }

    public Piece popPiece(int row, int column){
        return popPiece(getSquare(row, column));
    }

    public Piece popPiece(String squareName){
        return popPiece(getSquare(squareName));
    }


    public Piece replacePiece(Square replace, Piece with){
        Piece popped = popPiece(replace);
        setPiece(replace, with);
        return popped;
    }

    public Piece replacePiece(Piece replace, Piece with){
        return replacePiece(getSquare(replace), with);
    }

    public Piece replacePiece(int[] coordinates, Piece with){
        return replacePiece(getSquare(coordinates), with);
    }

    public Piece replacePiece(int row, int column, Piece with){
        return replacePiece(getSquare(row, column), with);
    }

    public Piece replacePiece(String squareName, Piece with){
        return replacePiece(getSquare(squareName), with);
    }


    public String getSquareName(Square square){
        return getSquareName(getCoordinates(square));
    }

    public static String getSquareName(int[] coordinates){
        return squareNames.get(coordinates);
    }

    public static String getSquareName(int row, int column){
        return squareNames.get(new int[] {row, column});
    }


    /**
     * Check if square matches arguments
     * return 1 if so
     * return 0 if not
     * return -1 if the is no such square
     * return 2 if there is no piece on the square
     * */
    public int testSquare(Square square, Color color, Value value){
        if (square == null) return -1;
        Piece piece = square.board.getPiece(square);
        if (piece == null) return 2;
        if (piece.getColor() != color) return 0;
        if (piece.value!=null && piece.value != value) return 0;
        return 1;
    }

    public int testSquare(int[] coordinates, Board board, Color color, Value value){
        return testSquare(board.getSquare(coordinates), color, value);
    }

    public int testSquare(int row, int column, Board board, Color color, Value value){
        return testSquare(board.getSquare(row, column), color, value);
    }

    public int testSquare(String squareName, Board board, Color color, Value value){
        return testSquare(board.getSquare(squareName), color, value);
    }


    public ArrayList<Piece> getAllPieces() {
        ArrayList<Piece> res = new ArrayList<>();
        state.forEach((square, piece) -> res.add(piece));
        return res;
    }

    public ArrayList<Piece> getPieces(Value value){
        ArrayList<Piece> result = new ArrayList<>(2);
        getAllPieces().forEach(piece -> {if (piece.value == value) result.add(piece);});
        return result;
    }
}
